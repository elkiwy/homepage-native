(ns homepage-native.shared.controllers.favorites
    (:require [reagent.core :as r ]
        [re-frame.core :as rf]
        [homepage-native.shared.utils :as utils]
        [homepage-native.shared.ui :as ui]
        [homepage-native.shared.style :as style]
        [homepage-native.shared.networking :as net]))




;Costants
(def sec-name-font-size 28) 
(def sec-name-height (+ sec-name-font-size 20))
(def fav-font-size 20)
(def fav-height (+ fav-font-size 24))




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
                    [ui/animated-view {:style (merge (style/style-light-background-and-border) {:height (:anim heightAnim) :margin-bottom 1 :margin-top 1})}

                        ;Favorite section header
                        [ui/custom-button-clear section-name
                            {:color style/col-white :font-weight "600" :margin-top 0
                             :height sec-name-height :font-size sec-name-font-size}
                            #(ui/toggle-section-height heightAnim sec-name-height (+ sec-name-height (* fav-height (+ (count favs) 1))) separatorAnim)
                            {:margin-top 8 :margin-bottom -8 :width utils/sw}]

                        ;Animated separator
                        [ui/animated-view {:style {:backgroundColor @style/col-accent1 :width (:anim separatorAnim) :height 1
                                                   :margin-left "auto" :margin-right "auto"}}]

                        ;Favorites links
                        [ui/view {:style {:margin-top (* fav-height 0.5)}}
                            (for [fav favs] ^{:key (name (first fav))}
                                [ui/custom-button-clear (name (first fav))
                                    {:color style/col-white :font-size fav-font-size :font-weight "100" :height fav-height :margin 0}
                                    #(net/http-open-url (second fav))
                                    {:margin-top 0 :margin-bottom 0}])]])))))


(defn get-first-category [favs]
    (if (nil? (first favs))
        nil
        (key (first favs))))


(defn settings-view []
    (let [favs (rf/subscribe [:favorites])
          categories (rf/subscribe [:favorites-categories])

          nameCatAtom (r/atom "")

          nameAtom (r/atom "")
          linkAtom (r/atom "")
          cateAtom (r/atom (get-first-category @favs))

          removeCateAtom (r/atom (get-first-category @favs))
          linksToRemove (r/atom (map first (get @favs @removeCateAtom)))
          removeFavAtom  (r/atom "")

          removeCategoryAtom (r/atom "")]
        (fn []
            [ui/view {:style {}}

                [ui/custom-header1 "Favorites settings" {:color style/col-white}]


                ;Category
                [ui/custom-header2 "Add a category" {:color style/col-white :margin-top 30}]
                [ui/custom-text-input nameCatAtom {} "Name"]
                [ui/custom-button "Add" {:backgroundColor @style/col-accent2} #(rf/dispatch [:favorites-category-added @nameCatAtom])]

                ;Favorite
                [ui/custom-header2 "Add a favorite" {:color style/col-white :margin-top 30}]
                [ui/custom-text-input nameAtom {} "Name"]
                [ui/custom-text-input linkAtom {} "URL"]
                [ui/custom-selection-input cateAtom categories {}]
                [ui/custom-button "Add" {:backgroundColor @style/col-accent2} #(rf/dispatch [:favorites-link-added @cateAtom @nameAtom @linkAtom])]

                [ui/custom-header2 "Remove a favorite" {:color style/col-white :margin-top 30}]
                [ui/custom-selection-input removeCateAtom categories {} {} #(do (reset! linksToRemove (map first (get @favs @removeCateAtom))) (reset! removeFavAtom ""))]
                [ui/custom-selection-input removeFavAtom linksToRemove {}]
                [ui/custom-button "Remove" {:backgroundColor @style/col-accent2} #(rf/dispatch [:favorites-link-removed @removeCateAtom @removeFavAtom ])]


            ])))







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
