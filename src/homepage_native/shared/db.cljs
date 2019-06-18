(ns homepage-native.shared.db
    (:require [reagent.core :as r]
              [re-frame.core :as rf]
              [re-frame.db :as rfdb]
              [homepage-native.shared.networking :as networking]
              [homepage-native.shared.utils :as utils]))


; ------------------------------------------------------------
; Utilities

(def async-storage (.-default (js/require "@react-native-community/async-storage")))


(defn save-state
    "Saves the whole app state into preferences. You can pass an app-db like map to save that instead."
    ([] (save-state @rfdb/app-db))
    ([data]
        (when (not (nil? data))
            (-> (.setItem async-storage "app-db" (.stringify js/JSON (clj->js data)))
                (.then #())))))



(defn getPreference
    "Get the preference through Promise system. It immediatly return the Promise object,
     and then call the callback with the clj object as the first parameter"
    [key & [callback]]
        (let [promise (.getItem async-storage key)]
            (if (nil? callback)
                promise
                (-> promise
                    (.then #(callback (js->clj (.parse js/JSON %) :keywordize-keys true)))))))



(defn load-state
    "Loads the app-db asynchronusly.
     If no app-db is saved it initialize it."
    []
    (getPreference "app-db"
        (fn [data]
            (if (nil? data)
                (rf/dispatch-sync [:initialize])
                (rf/dispatch-sync [:replace-db data true]))
            )))



(defn update-db-and-save
    "Calls the updateFunc, saves the updated db into preferences, and optionally syncs the db remotly"
    [sync updateFunc]
    (let [result (updateFunc)]
        (save-state result)
        (when sync
            (networking/updateConfig result))
        result))



(defn remove-vec
    "Removes a specifuc item from a vector"
    [vec item]
    (into [] (remove #{item} vec)))







; ------------------------------------------------------------
; Events
;init
(rf/reg-event-db :initialize 
    (fn [_ _] {:page-current :Favorites
              :account {:name "" :pass "" :sync false}
              :reddit {:selected "" :subreddits []}
              :favs {}
              :rss-feeds [] ;Vector of Name-Link pairs
              :rss-selected "" ;String
              :rss-data {} ;String - Data map
             })) 

;replace
(rf/reg-event-db :replace-db
    (fn [db [_ new-db full-replace?]]
        (let [cp (:page-current db)]
            (println "got replace-db event")
            (if full-replace?
                (update-db-and-save false #(assoc new-db :page-current (:page-current new-db)))
                (update-db-and-save false #(assoc new-db :page-current cp))))))

;navigation
(rf/reg-event-db :page-changed
    (fn [db [_ newPage]] (update-db-and-save false #(assoc db :page-current newPage))))



;account
(rf/reg-event-db :account-updated
    (fn [db [_ name pass sync]]
        (update-db-and-save true #(assoc db :account {:name name :pass pass :sync sync}))))


;Reddit
(rf/reg-event-db :reddit-selected-changed
    (fn [db [_ newSubreddit]]
        (update-db-and-save false #(assoc-in db [:reddit :selected] newSubreddit))))

(rf/reg-event-db :reddit-added-subreddit
    (fn [db [_ sub]]
        (update-db-and-save true #(update-in db [:reddit :subreddits] conj sub))))

(rf/reg-event-db :reddit-removed-subreddit
    (fn [db [_ sub]]
        (update-db-and-save true #(update-in db [:reddit :subreddits] utils/remove-from-vector sub))))





; ------------------------------------------------------------
;navigation
(rf/reg-sub :page-current
    (fn [db _] (:page-current db)))


;account
(rf/reg-sub :account
    (fn [db _] (:account db)))


;reddit
(rf/reg-sub :reddit-subreddits ;A vector of strings
    (fn [db _] (:subreddits (:reddit db))))

(rf/reg-sub :reddit-selected ;A string
    (fn [db _] (:selected (:reddit db))))

