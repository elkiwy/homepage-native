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


;Favorites
(defn get-categories-names
    "[TO CLEAN] Retrieves the categories vector from a `db`."
    [db]
    (mapv #(utils/deurlizeString (:name %)) (get-in db [:favorites :categories])))

(defn in-vector?
    "Checks if an `item` is inside the vector `v`."
    [v item]
    (not (nil? (some #(= item %) v))))

(defn alert-and-return
    "Shows an alert and returns the `db`."
    [message db] 
    (js/alert message)
    db)

(rf/reg-event-db :favorite2-category-added
    (fn [db [_ name]]
        (cond
            ;Category name already exists
            (in-vector? (get-categories-names db) name)
                (alert-and-return (str "A category named \"" name "\" already exists.") db)
            ;Success case
            :else 
                (let [nam (utils/urlizeString name)
                    categories (get-in db [:favorites :categories])
                    ind (count categories)
                    newCategories (conj categories {:name nam :order (inc ind) :links []})]
                    (update-db-and-save true
                        #(assoc-in db [:favorites :categories] (vec newCategories)))))))

(rf/reg-event-db :favorite2-link-added
    (fn [db [_ category name link]]
        (let [categories (get-in db [:favorites :categories])
              cateIndex (utils/index categories #(= (:name %) category))
              links (get-in categories [cateIndex :links])
              cateLinksNames (mapv #(utils/deurlizeString (:name %)) links)]
            (cond
                ;Duplicate name case
                (in-vector? cateLinksNames name)
                    (alert-and-return (str "A link named \"" name "\" already exists.") db)
                ;Default case
                :else
                    (let [nam (utils/urlizeString name)
                        cat (utils/urlizeString category)
                        lnk (utils/urlizeString link)]
                        (update-db-and-save true
                            #(update-in db [:favorites :categories cateIndex :links]
                                 conj {:name nam :link lnk})))))))

(rf/reg-event-db :favorite2-category-removed
    (fn [db [_ name]]
        (let [nam (utils/urlizeString name)
              categories (get-in db [:favorites :categories])
              newCategories (remove #(= nam (utils/urlizeString (:name %))) categories)]
            (update-db-and-save true
                #(assoc-in db [:favorites :categories] (vec newCategories))))))

(rf/reg-event-db :favorite2-link-removed
    (fn [db [_ category name]]
        (let [cat (utils/urlizeString category)
              nam (utils/urlizeString name)
              categories (get-in db [:favorites :categories])
              cateIndex  (utils/index categories #(= (:name %) cat))]
            (update-db-and-save true
                #(update-in db [:favorites :categories cateIndex :links]
                       (fn [obj] (remove (fn [link] (= (:name link) nam)) obj)))))))

(rf/reg-event-db :favorite2-categories-swapped
    (fn [db [_ cat1 cat2]]
           (let [cat1 (utils/urlizeString cat1)
                 cat2 (utils/urlizeString cat2)
                 categories (get-in db [:favorites :categories])
                 cat1-ind (:order (first (filter #(= (:name %) cat1) categories)))
                 cat2-ind (:order (first (filter #(= (:name %) cat2) categories)))
                 cat1-vec-pos (utils/index categories #(= (:name %) cat1))
                 cat2-vec-pos (utils/index categories #(= (:name %) cat2))
                 categories-new (vec (assoc-in categories     [cat1-vec-pos :order] cat2-ind))
                 categories-new (vec (assoc-in categories-new [cat2-vec-pos :order] cat1-ind))
                 categories-new (vec (sort #(< (:order %1) (:order %2)) categories-new))]
               (update-db-and-save true
                   #(assoc-in db [:favorites :categories] categories-new)))))


; --------------------------------------
; RSS
(rf/reg-event-db :rss-selected-changed
    (fn [db [_ newRss]]
        (update-db-and-save true
            #(assoc-in db [:rss :selected] (utils/urlizeString newRss)))))

(rf/reg-event-db :rss-added
    (fn [db [_ name url]]
        (update-db-and-save true
            #(assoc-in db [:rss :feeds (utils/urlizeString name)] url))))

(rf/reg-event-db :rss-removed
    (fn [db [_ name]]
        (let [feeds (get-in db [:rss :feeds])
              newFeeds (dissoc feeds (utils/urlizeString name))]
            (update-db-and-save true
                #(assoc-in db [:rss :feeds] newFeeds)))))



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


;favs
(rf/reg-sub :favorites ; {:categories [{:name "Social" :order 1 :link [{:name "Facebook" :link "..."}]}]}
    (fn [db _]
        (:favorites db)))

(rf/reg-sub :favorites2-categories ; ["Social" "Relax" "Work"]
    (fn [db _]
        (vec (mapv #(utils/deurlizeString (:name %)) (get-in db [:favorites :categories])))))

(rf/reg-sub :favorites2-category-links ; [{:name "Facebook" :link "..."} {:name "YouTube" :link "..."}]
    (fn [db [_ name]]
        (let [categories (get-in db [:favorites :categories])
              category (first (filter #(= (:name %) (utils/urlizeString name)) categories))]
            (vec (:links category)))))


; --------------------------------------
; RSS
(rf/reg-sub :rss-feeds
    (fn [db _]
        (get-in db [:rss :feeds] {})))

(rf/reg-sub :rss-selected-name
    (fn [db _]
        (let [default (-> (get-in db [:rss :feeds] {}) seq first str)
              name    (get-in db [:rss :selected] default)]
            (utils/deurlizeString name))))

(rf/reg-sub :rss-selected-url
    :<- [:rss-selected-name]
    :<- [:rss-feeds]
    (fn [[name feeds] _]
        (get feeds (utils/urlizeString name) "")))


