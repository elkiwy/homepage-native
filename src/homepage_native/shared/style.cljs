(ns homepage-native.shared.style
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))



; ------------------------------------------------------------
; Colors
(def col-white "#f8f4f0")
(def col-black "#000408e0")
(def col-black-full "#000408")
(def col-dark-gray "#303030")
(def col-medium-gray "#252228")

(let [rand-hue (rand-int 359)]
    (def col-accent1 (r/atom (str "hsl(" rand-hue ", 30%, 70%)")))
    (def col-accent2 (r/atom (str "hsl(" (+ rand-hue 80) ", 30%, 70%)")))
)

(def theme (r/atom :Dark))



; ------------------------------------------------------------
; Styles
(defn style-text
    "A style for any text component that sets font color and default size."
    [& [col weight size]]
    {:font-family "Fira Code"
     :font-weight (if (nil? weight) "400" weight)
     :color       (if (nil? col)    (if (= @theme :Dark) col-white col-black) col)
     :font-size   (if (nil? size)   16 size)})


(defn style-light-background-and-border
    "A style for items having a background view with theme color."
    [& [col]]
    {:border-color col-black :border-width 0
     :background-color (if (nil? col) (if (= @theme :Dark) col-black col-white) col)
     :overflow "hidden"
     :shadow-color "#000" :shadow-offset {:width 2 :height 2} :shadow-opacity 0.25 :shadow-radius 1 :elevation 1})




