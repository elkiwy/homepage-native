(ns homepage-native.shared.controllers.favorites
    (:require [reagent.core :as r :refer [atom]]
        [re-frame.core :as rf]
        [homepage-native.shared.utils :as utils]
        [homepage-native.shared.ui :as ui]
        [homepage-native.shared.style :as s]
        [homepage-native.shared.networking :as net]))



;Costants
(def sec-name-font-size 28) 
(def sec-name-height (+ sec-name-font-size 20))
(def fav-font-size 20)
(def fav-height (+ fav-font-size 24))


(defn toggle-section-height
    "Toggles the favorites section expansion animation on and off"
    [heightAnim favs-count sepAnim]
    (let [min sec-name-height
          max (+ min (* fav-height (+ favs-count 1)))]
        (if (= (ui/anim-get-value heightAnim) min)
            (do (ui/anim-set-value heightAnim max)
                (ui/anim-set-value sepAnim utils/sw))
            (do (ui/anim-set-value heightAnim min)
                (ui/anim-set-value sepAnim 0)))))



(defn fav-section
    "The component for a single favorites section"
    [item] ^{:key item}
    (r/create-element
        (r/reactify-component 
            (fn []
                (let [heightAnim    (ui/anim-new-value sec-name-height)
                      separatorAnim (ui/anim-new-value 0)
                      info          (:item (js->clj item :keywordize-keys true))
                      section-name  (first info)
                      favs          (second info)]
                    [ui/animated-view {:style (merge (s/style-light-background-and-border) {:height (:anim heightAnim) :margin-bottom 1 :margin-top 1})}

                        ;Favorite section header
                        [ui/custom-button-clear section-name
                            {:color s/col-white :font-weight "600" :margin-top 0
                             :height sec-name-height :font-size sec-name-font-size}
                            #(toggle-section-height heightAnim (count favs) separatorAnim)
                            {:margin-top 8 :margin-bottom -8 :width utils/sw}]

                        ;Animated separator
                        [ui/animated-view {:style {:backgroundColor @s/col-accent1 :width (:anim separatorAnim) :height 1
                                                   :margin-left "auto" :margin-right "auto"}}]

                        ;Favorites links
                        [ui/view {:style {:margin-top (* fav-height 0.5)}}
                            (for [fav favs] ^{:key (name (first fav))}
                                [ui/custom-button-clear (name (first fav))
                                    {:color s/col-white :font-size fav-font-size :font-weight "100" :height fav-height :margin 0}
                                    #(net/http-open-url (second fav))
                                    {:margin-top 0 :margin-bottom 0}])]])))))



(defn main-controller
    "The main Favorites controller"
    []
    (let [favs    (rf/subscribe [:favorites])
          logAtom (r/atom (str @favs))]
        (fn []
            [ui/view
                ;Header
                [ui/custom-header1 "Favorites"]

                ;Favorites list
                [ui/flat-list {:data  (vec (seq @favs))
                               :style {:minHeight (- utils/sh @ui/topInset @ui/topInset ) }
                               :render-item fav-section
                               :key-extractor (fn [item index] (str index))}]

                ;Debug
                ;[ui/view [ui/text @logAtom]]
                ])))
