(ns homepage-native.shared.controllers.reddit
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :as rf]
            [homepage-native.shared.utils :as utils]
            [homepage-native.shared.style :as style]
            [homepage-native.shared.ui :as ui]
            [homepage-native.shared.networking :as net]
            [homepage-native.shared.db]))



(def reddit-base-url "https://www.reddit.com/r/")

(def subSelectionYMin (* utils/sh 0.2))
(def subSelectionYMax (* utils/sh 1.0))
(def subSelectionY (ui/anim-new-value subSelectionYMax))




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
    (ui/anim-set-value subSelectionY (if (= (ui/anim-get-value subSelectionY) subSelectionYMin) subSelectionYMax subSelectionYMin)))


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
                    [ui/custom-button-clear subName {:color style/col-white} #(do (rf/dispatch-sync [:subreddit-selected-changed subName]) (toggle-selection-view))])))))




(:children (:data @(rf/subscribe [:subreddit-selected-data])))
(mapv #(name (first %)) (seq @(rf/subscribe [:subreddits])))


(defn main-controller []
    (let [subredditsAtom (rf/subscribe [:subreddits])
          subredditDataAtom (rf/subscribe [:subreddit-selected-data])
          subredditNameAtom (rf/subscribe [:subreddit-selected-name])]
        (fn []
            [ui/view 
                (cond
                    (empty? @subredditNameAtom)
                        [ui/custom-header2 "No subreddit selected."]

                    (empty? @subredditDataAtom)
                        (do (net/http-get-json (str reddit-base-url @subredditNameAtom ".json") (fn [data] (rf/dispatch [:subreddit-fetched-data @subredditNameAtom data])) )
                        [ui/custom-header2 "Loading..."])

                    :else
                        (let [posts (:children (:data @subredditDataAtom))]
                            [ui/view  {:style {:height utils/sh}}
                                [ui/flat-list {:data (clj->js posts) :render-item reddit-post :key-extractor (fn [item index] (str index))
                                               :ListHeaderComponent (r/create-element (r/reactify-component (fn [] [title @subredditNameAtom])))}]]
                        )
                )

                [ui/animated-view {:style {:backgroundColor style/col-black :position "absolute" :left 0 :top (:anim subSelectionY) :width utils/sw :height (* utils/sh 0.8)}}
                    (let [subs (mapv #(name (first %)) (seq @subredditsAtom))]
                        [ui/flat-list {:data subs
                                       :render-item subs-selection
                                       :key-extractor (fn [item index] (str index))}])]])))
