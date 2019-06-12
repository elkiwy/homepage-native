(ns homepage-native.shared.account
    (:require [reagent.core :as r :refer [atom]]
        [re-frame.core :as rf]
        [re-frame.db :as rfdb]
        [homepage-native.shared.utils :as utils]
        [homepage-native.shared.ui :as ui]
        [cljs.reader :as reader]
        [goog.crypt.base64 :as b64]
        ))





; ------------------------------------------------------------
; End points
(def base-url "https://test.elkiwyart.com/homepage-cljs/")
(def getUserConfig-endpoint "getUserConfig.php")
(def addUserConfig-endpoint "addUserConfig.php")
(def agent "32i1n4kbt52of0wdfsd9fj0hfqd0fb20rjekfbsdkba02")


(defn pack-query-parameters [data]
    (loop [params (seq data)
           result "?"]
        (if (empty? params)
            (clojure.string/join "" (drop-last result))
            (recur
                (rest params)
                (str result (name (ffirst params)) "="  (second (first params)) "&" )))))



(defn http-post-json
    "Get json data from an HTTP/GET call to an Url and call the callback after."
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


(defn backend-post-request [endpoint queryParamsData postBodyData callback]
    (let [queryParams           (pack-query-parameters queryParamsData)
          postBodyDataWithAgent (assoc postBodyData :agent agent)]
        (http-post-json (str base-url endpoint queryParams) postBodyDataWithAgent  callback)))



;(defn fetch-rss [rssName rssUrl]
    ;(go (let [response (<! (http/get (str rss-proxy rssUrl) {:with-credentials? false}))]
            ;(rf/dispatch [:rss-fetched-data rssName (:body response)]))))







(defn getConfig [usr psw logAtom]
    (backend-post-request getUserConfig-endpoint {:user usr} {:password psw}
        ;(fn [data] (println "POPPE!" data))
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
                    (when (not (nil? logAtom)) (reset! logAtom (str "Config downloaded failed with code " code))))))
        ))


(defn addConfig [usr psw logAtom targetDb]
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


(defn updateConfig [updated-db]
    (let [{:keys [name pass sync]} (:account updated-db)]
        (when (not (empty? name))
            (addConfig name pass nil updated-db))))


(defn try-download-state []
    (let [account (:account @rfdb/app-db)]
        (when (not (empty? (:name account)))
            (getConfig (:name account) (:pass account) nil))))




(defn account-with-account [account logAtom]
    (fn []
        [ui/view
            [ui/custom-header2 "Manage account"]
            [ui/custom-header2 (str "Logged in as " (:name @account))]

            [ui/custom-button (str "Manually upload to " (:name @account) "'s cloud") {} #(addConfig (:name @account) (:pass @account) logAtom nil)]

            [ui/custom-button "Log out" {} #(do
                                                (rf/dispatch-sync [:replace-db {}])
                                                (rf/dispatch-sync [:initialize])
                                                ;(rf/dispatch-sync [:page-changed :Account])
                                            )]

            ]))




(defn account-without-account [account logAtom]
    (let [usernameAtom (r/atom "")
          passwordAtom (r/atom "")]
        (fn []
            [ui/view {:style {:justifyContent "center"}}
                ;Register
                [ui/custom-header2 "Account"]
                [ui/custom-text-input usernameAtom {:width (* utils/sw 0.8)} "Username"]
                [ui/custom-text-input passwordAtom {:width (* utils/sw 0.8)} "Password" true]

                ;[ui/custom-button "Create an account"  {} #(addConfig @usernameAtom @passwordAtom logAtom nil)]
                [ui/custom-button "Create an account"  {:width (* utils/sw 0.8)} #(addConfig @usernameAtom @passwordAtom logAtom nil)]

                ;Login
                ;[:input {:value "Log in" :on-click #(getConfig @usernameAtom @passwordAtom logAtom)}]
                [ui/custom-button "Log in"  {:width (* utils/sw 0.8)} #(getConfig @usernameAtom @passwordAtom logAtom)]

                [ui/custom-button "Reset data" {} #(do
                                                       (rf/dispatch-sync [:replace-db {}])
                                                       (rf/dispatch-sync [:initialize])
                                                       ;(rf/dispatch-sync [:page-changed :Account])
                                                       (println "ciao!")
                                                    )]
                ])))






(defn main-controller []
    (let [account (rf/subscribe [:account])
          logAtom (r/atom "")]
        (fn []
            [ui/view
                ;(js/alert (str @(rf/subscribe [:account])))
                ;(js/alert (str @rfdb/app-db))

                [ui/custom-header1 "Cloud Sync"]
                (if (not (empty? (:name @account)))
                    [account-with-account account logAtom]
                    [account-without-account account logAtom])
                [ui/view [ui/text @logAtom]]
                ])))
















