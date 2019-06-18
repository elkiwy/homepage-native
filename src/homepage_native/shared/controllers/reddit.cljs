(ns homepage-native.shared.controllers.reddit
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :as rf]
            [homepage-native.shared.utils :as utils]
            [homepage-native.shared.style :as style]
            [homepage-native.shared.ui :as ui]
            [homepage-native.shared.networking :as net]
            [homepage-native.shared.db]))



(def reddit-base-url "https://www.reddit.com/r/")

(def subSelectionYMin (* utils/sh 0))
(def subSelectionYMax (* utils/sh 1.0))
(defonce subSelectionY     (ui/anim-new-value subSelectionYMax))
(defonce subSelectionAlpha (ui/anim-new-value 0))




(defn reddit-post [item] ^{:key item}
    (r/create-element
        (r/reactify-component 
            (fn []
                (let [info (:item (js->clj item :keywordize-keys true))]
                    [ui/view {:style {:width utils/sw :margin-top 1 :margin-bottom 1}}
                        [ui/touchable-opacity {:on-press #( (net/http-open-url (:url (:data info))))
                                            :style (merge (style/style-light-background-and-border) {:flex-direction "row"})}
                            [ui/text {:style (merge (style/style-text) {:flex 1 :padding 12})}
                                (str (:title (:data info)))]]])))))



(defn toggle-selection-view []
    (let [current (ui/anim-get-value subSelectionY)
          toHide? (= current subSelectionYMin)]
        (ui/anim-set-value subSelectionAlpha (if toHide? 0 0.95))
        (ui/anim-set-value subSelectionY (if toHide? subSelectionYMax subSelectionYMin))
))


(defn title [name]
    (fn []
        [ui/view {:style {:border-bottom-width 2 :border-bottom-color @style/col-accent2}}
            [ui/custom-button-clear (str "r/" name) {:font-size 22} #(toggle-selection-view)]
            ]))



(defn subs-selection [item] ^{:key item}
    (r/create-element
        (r/reactify-component
            (fn []
                (let [subName (str (:item (js->clj item :keywordize-keys true)))]
                    [ui/view {:style {:borderBottomWidth 1 :borderBottomColor (str style/col-white "20") :paddingBottom 15 :paddingTop 15}}
                        [ui/custom-button-clear subName {:color style/col-white :font-size 24}
                            #(do (rf/dispatch-sync [:reddit-selected-changed subName])
                                 (toggle-selection-view))]])))))


(defn settings-view []
    (let [newSubName (r/atom "")]
        (fn []
            [ui/view {:style {}}

                ;Add subreddit
                [ui/custom-header1 "Reddit settings" {:color style/col-white}]
                [ui/custom-header2 "Add a subreddit" {:color style/col-white}]


                [ui/custom-text-input newSubName {} "subreddit-name"]
                [ui/custom-button "Add" {} #(rf/dispatch [:reddit-added-subreddit @newSubName])]



                ;Remove fav
                [ui/custom-header2 "Remove a subreddit" {:color style/col-white}]


                ;Convert this to actionsheet

                [ui/picker {:selectedValue "1" :style {:backgroundColor "white" :color "black" :margin-left 10 :height 500 :width 300}
                            :onValueChange (fn [value index] (println (str value "," index)))}

                    [ui/pickerItem {:label "cose" :value "1"}]
                    [ui/pickerItem {:label "cose2" :value "2"}]


                    ]


                ;[:select {:on-change #(reset! remSubNameAtom (-> % .-target .-value))} :defaultValue ""
                    ;[:option  ""]
                    ;(for [subname (seq @subs)]
                        ;^{:key (first subname)} [:option (first subname)])]
;
                ;[:input {:type "button" :value "Remove"
                                        ;:on-click #(rf/dispatch [:subreddit-removed @remSubNameAtom])}]
                ]
        
        )))



(defn main-controller []
    (let [subredditsAtom (rf/subscribe [:reddit-subreddits])
          subredditDataAtom (r/atom nil)
          subredditNameAtom (rf/subscribe [:reddit-selected])]
        (fn []
            [ui/view 
                (cond
                    (empty? @subredditNameAtom)
                        [ui/custom-header2 "No subreddit selected."]

                    (empty? @subredditDataAtom)
                        (do (net/http-get-json (str reddit-base-url @subredditNameAtom ".json") (fn [data] (reset! subredditDataAtom data)) )
                        [ui/custom-header2 "Loading..."])

                    :else
                        (let [posts (:children (:data @subredditDataAtom))]
                            [ui/view  {:style {:height utils/sh}}
                                [ui/flat-list {:data (clj->js posts) :render-item reddit-post :key-extractor (fn [item index] (str index))
                                               :ListHeaderComponent (r/create-element (r/reactify-component (fn [] [title @subredditNameAtom])))}]]
                        )
                )


                [ui/animated-view {:pointerEvents "none"
                                   :style {:position "absolute" :left 0 :top -100 :width utils/sw :height (+ utils/sh 100)
                                           :backgroundColor style/col-black-full :opacity (:anim subSelectionAlpha) }}]
                [ui/animated-view {:style {:backgroundColor style/col-black :opacity 0.85 :position "absolute" :left 0 :top (:anim subSelectionY) :width utils/sw :height utils/sh}}
                        (let [subs @subredditsAtom]
                            [ui/flat-list {:data subs
                                           :render-item subs-selection
                                           :key-extractor (fn [item index] (str index))}])]
                

                    ])))
