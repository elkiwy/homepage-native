(ns homepage-native.shared.utils
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))




;; -----------------------------------------------------------------------------------------------------
;; Main react object
(def react (js/require "react-native"))

;; -----------------------------------------------------------------------------------------------------
;; Utils

(def dimensions (.-Dimensions react))
(def sw (.-width (.get dimensions "window")))
(def sh (.-height (.get dimensions "window")))

(defn discard-json
    "Takes the subreddits map and outputs the map without the jsons"
    [subreddits]
    (loop [subs (seq subreddits) result []]
        (if (empty? subs)
            (into {} result)
            (recur (rest subs)
                (concat result [[(first (first subs)) {:json ""}]])))))

(defn dissoc-in [m path key]
    (update-in m path dissoc key))

(defn remove-from-vector [coll item]
    (into [] (remove #(= % item) coll)))

(defn urlizeString [s]
    (if (nil? s) nil (clojure.string/replace s " " "%20")))

(defn deurlizeString [s]
    (if (nil? s) nil (clojure.string/replace s "%20" " ")))

(defn index [v pred]
    (ffirst (filter #(pred (second %)) (map-indexed vector v))))
