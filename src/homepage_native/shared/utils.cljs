(ns homepage-native.shared.utils
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))



; ------------------------------------------------------------
; REact object

(def react (js/require "react-native"))

; ------------------------------------------------------------
; Utils

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
