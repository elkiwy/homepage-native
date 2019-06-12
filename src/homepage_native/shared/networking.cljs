(ns homepage-native.shared.networking
    (:require [reagent.core :as r :refer [atom]]
              [homepage-native.shared.utils :as utils]))



; ------------------------------------------------------------
; Networking

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




