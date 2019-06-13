(ns homepage-native.shared.networking
    (:require [reagent.core :as r :refer [atom]]
              [re-frame.core :as rf]
              [re-frame.db :as rfdb]
              [cljs.reader :as reader]
              [goog.crypt.base64 :as b64]
              [homepage-native.shared.utils :as utils]))




; ------------------------------------------------------------
; Generic Networking

(defn http-open-url
    "Open an url inside the default browser"
    [url]
        (.openURL (.-Linking utils/react) url))




(defn http-get-json
    "Get json data from an HTTP/GET call to an Url and call the callback after."
    [url callback]
        (let [args {"method" "GET", "credentials" "include","headers" {"Content-Type" "application/json", "Accept" "application/json"}}
              cljs-args (clj->js args)
              promise (js/fetch url cljs-args)]
            (-> promise
                (.catch (fn [err] (callback nil)))
                (.then #(.json %1))
                (.then #(js->clj %1 :keywordize-keys true))
                (.then (fn [clj-json] (callback clj-json))))))



(defn http-post-json
    "Get json data from an HTTP/POST call to an Url and call the callback after."
    [url body callback]
    (let [args {"method" "POST", "credentials" "include",
                "headers" {"Content-Type" "application/json", "Accept" "application/json"},
                "body" (.stringify js/JSON (clj->js body))}
          cljs-args (clj->js args)
          promise (js/fetch url cljs-args)]
            (-> promise
                (.catch (fn [err] (callback nil)))
                (.then #(.json %1))
                (.then #(js->clj %1 :keywordize-keys true))
                (.then (fn [clj-json] (callback clj-json))))))



(defn pack-query-parameters
    "Creates the string with all the query parameters from a map structure"
    [data]
    (loop [params (seq data) result "?"]
        (if (empty? params)
            (clojure.string/join "" (drop-last result))
            (recur (rest params) (str result (name (ffirst params)) "="  (second (first params)) "&" )))))



; ------------------------------------------------------------
; Backend 
(def base-url "https://test.elkiwyart.com/homepage-cljs/")
(def getUserConfig-endpoint "getUserConfig.php")
(def addUserConfig-endpoint "addUserConfig.php")
(def agent "32i1n4kbt52of0wdfsd9fj0hfqd0fb20rjekfbsdkba02")



(defn backend-post-request
    "Calls the backend with a post request automatically packing the parameters and adding the agent."
    [endpoint queryParamsData postBodyData callback]
    (let [queryParams           (pack-query-parameters queryParamsData)
          postBodyDataWithAgent (assoc postBodyData :agent agent)]
        (http-post-json (str base-url endpoint queryParams) postBodyDataWithAgent  callback)))



(defn getConfig
    "Retrieve the config object from the backend for a user and save it to the preferences."
    [usr psw logAtom]
    (backend-post-request getUserConfig-endpoint {:user usr} {:password psw}
        (fn [responseBody]
            (let [code (:code responseBody)
                  config-b64 (:config (:data responseBody))
                  config-string (b64/decodeString config-b64)
                  config (reader/read-string config-string)]
                (if (= (:code responseBody) 200)
                    (do
                        (when (not (nil? logAtom)) (reset! logAtom (str "Config downloaded successfully")))
                        (rf/dispatch-sync [:replace-db config])
                        (rf/dispatch-sync [:account-updated usr psw false]))
                    (when (not (nil? logAtom)) (reset! logAtom (str "Config downloaded failed with code " code))))))))



(defn addConfig
    "Upload the current app-db to the backend."
    [usr psw logAtom targetDb]
    (let [fullConfig (if (nil? targetDb) @rfdb/app-db targetDb)
          slimConfig (update fullConfig :subreddits utils/discard-json)
          base64     (b64/encodeString (str slimConfig))]
        (backend-post-request addUserConfig-endpoint {:user usr} {:password psw :config base64}
            (fn [responseBody]
                (println "Updated config " (count base64))
                (when (not (nil? logAtom))
                    (if (= (:code responseBody) 200)
                        (reset! logAtom (str "Config uploaded successfully with code " (:code responseBody)))
                        (reset! logAtom (str "Config uploaded failed with code " (:code responseBody)))))))))



(defn updateConfig
    "Uploads the config to the backend, but only if the user is logged in."
    [updated-db]
    (let [{:keys [name pass sync]} (:account updated-db)]
        (when (not (empty? name))
            (addConfig name pass nil updated-db))))



(defn try-download-state
    "If the user is logged in, try to download the app-db"
    []
    (let [account (:account @rfdb/app-db)]
        (when (not (empty? (:name account)))
            (getConfig (:name account) (:pass account) nil))))




;(defn fetch-rss [rssName rssUrl]
    ;(go (let [response (<! (http/get (str rss-proxy rssUrl) {:with-credentials? false}))]
            ;(rf/dispatch [:rss-fetched-data rssName (:body response)]))))




































