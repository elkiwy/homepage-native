(ns homepage-native.shared.style
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))



; ------------------------------------------------------------
; Colors
(def col-white "#f8f4f0ff")
(def col-black "#000408e0")
(def col-accent1 (r/atom "#ff0000"))
(def col-accent2 (r/atom "#00ff00"))
(def theme (r/atom :Dark))



; ------------------------------------------------------------
; Styles
(defn style-text [& [col weight size]]
    {:font-family "Fira Code"
     :font-weight (if (nil? weight) "400" weight)
     :color       (if (nil? col)    (if (= @theme :Dark) col-white col-black) col)
     :font-size   (if (nil? size)   16 size)})


(defn style-light-background-and-border [& [col]]
    {:border-color col-black :border-width 0
     :background-color (if (nil? col) (if (= @theme :Dark) col-black col-white) col)
     :overflow "hidden"
     :shadow-color "#000" :shadow-offset {:width 2 :height 2} :shadow-opacity 0.25 :shadow-radius 1 :elevation 1})




