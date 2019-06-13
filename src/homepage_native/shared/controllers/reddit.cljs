(ns homepage-native.shared.controllers.reddit
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [homepage-native.shared.utils :as utils]
            [homepage-native.shared.style :as style]
            [homepage-native.shared.ui :as ui]
            [homepage-native.shared.networking :as net]
            [homepage-native.shared.db]))



(def test-url "https://www.reddit.com/r/clojure.json")




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





(defn title [name]
    (fn []
        [ui/view {:style {:border-bottom-width 2 :border-bottom-color @style/col-accent2}}
            [ui/custom-header1 name]]))





(defn main-controller []
    (let [subredditDataAtom (r/atom {})]
        (fn []
            (cond
                (empty? @subredditDataAtom)
                    (do (net/http-get-json test-url (fn [data] (reset! subredditDataAtom data)) )
                       [ui/custom-header2 "Loading..."])
            :else
                (let [posts (:children (:data @subredditDataAtom))]


                    [ui/view  {:style {:height utils/sh}}
                        [ui/flat-list {:data (clj->js posts) :render-item reddit-post :key-extractor (fn [item index] (str index))
                                          :ListHeaderComponent (r/create-element (r/reactify-component (fn [] [title "Reddit"])))}]]



                    )))))
