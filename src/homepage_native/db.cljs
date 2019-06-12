(ns homepage-native.db
    (:require [reagent.core :as r]
              [re-frame.core :as rf]
              [re-frame.db :as rfdb]
              [homepage-native.shared.utils :as utils]))


; ------------------------------------------------------------
; Utilities

(def async-storage (.-default (js/require "@react-native-community/async-storage")))


(defn save-state
    ([]
        (save-state @rfdb/app-db))
    ([data]
        nil
        (when (not (nil? data))
            (-> (.setItem async-storage "app-db" (.stringify js/JSON (clj->js (update-in data [:subreddits] utils/discard-json))))
                (.then #(js/alert "Saved"))))))



  
(defn getPreference
    "Get the preference through Promise system. It immediatly return the Promise object,
     and then call the callback with the clj object as the first parameter"
    [key & [callback]]
        (let [promise (.getItem async-storage key)]
            (if (nil? callback)
                promise
                (-> promise
                    (.then #(callback (js->clj (.parse js/JSON %) :keywordize-keys true)))))))


(defn debugPreference [key]
    (getPreference key #(js/alert (str %))))



(defn load-state
    "Loads the app-db asynchronusly.
     If no app-db is saved it initialize it."
    []
    (-> (getPreference "app-db"
            (fn [data]
                (if (nil? data)
                    (rf/dispatch-sync [:initialize])
                    (rf/dispatch-sync [:replace-db data true]))))))




(defn update-db-and-save [sync fn]
    (let [result (fn)]
        (save-state result)
        (when (= sync true)
            (homepage-native.shared.account/updateConfig result))
        result))

(defn remove-vec [vec item]
    (into [] (remove #{item} vec)))

; ------------------------------------------------------------
; Events

(rf/reg-event-db :set-greeting 
    (fn [db [_ value]] (assoc db :greeting value)))

(rf/reg-event-db :initialize 
    (fn [_ _] {:page-current :Favorites
              :account {:name "" :pass "" :sync false}
              :subreddits {} :subreddit-selected-name ""
              :favs {}
              :rss-feeds [] ;Vector of Name-Link pairs
              :rss-selected "" ;String
              :rss-data {} ;String - Data map
             })) 

(rf/reg-event-db :replace-db
    (fn [db [_ new-db full-replace?]]
        (let [cp (:page-current db)]
            (if full-replace?
                (update-db-and-save false #(assoc new-db :page-current (:page-current new-db)))
                (update-db-and-save false #(assoc new-db :page-current cp))))))

;account
(rf/reg-event-db :account-updated
    (fn [db [_ name pass sync]]
        (update-db-and-save true #(assoc db :account {:name name :pass pass :sync sync}))))

; ------------------------------------------------------------
; Subscriptions

(rf/reg-sub :get-greeting
    (fn [db _] (:greeting db)))


(rf/reg-sub :account
    (fn [db _] (:account db)))


