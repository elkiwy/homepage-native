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


(defn get-first-subreddit []
    (let [subreddits (rf/subscribe [:reddit-subreddits])]
        (if (empty? @subreddits) "No subreddits." (first @subreddits))))


(defn settings-view []
    (let [newSubName (r/atom "")
          subreddits (rf/subscribe [:reddit-subreddits])
          subToRemove (r/atom (get-first-subreddit))]
        (fn []
            [ui/view {:style {}}

                [ui/custom-header1 "Reddit settings" {:color style/col-white}]

                ;Add subreddit
                [ui/custom-header2 "Add a subreddit" {:color style/col-white :margin-top 30}]
                [ui/custom-text-input newSubName {:width (* utils/sw 0.8)} "subreddit-name"]
                [ui/custom-button "Add" {:width (* utils/sw 0.8) :backgroundColor @style/col-accent2} #(rf/dispatch [:reddit-added-subreddit @newSubName])]

                ;Remove fav
                [ui/custom-header2 "Remove a subreddit" {:color style/col-white :margin-top 30}]
                [ui/custom-selection-input subToRemove subreddits {:width (* utils/sw 0.8) :backgroundColor style/col-white}]
                [ui/custom-button "Remove" {:width (* utils/sw 0.8) :backgroundColor @style/col-accent2} #(do (rf/dispatch-sync [:reddit-removed-subreddit @subToRemove])
                                                                                                              (reset! subToRemove (get-first-subreddit)))]]
        
        )))


;(str (:reddit @re-frame.db/app-db))
;(rf/dispatch-sync [:reddit-added-subreddit "vim"])
;(rf/dispatch-sync [:reddit-selected-changed "clojure"])


(defn main-controller []
    (let [subredditsAtom (rf/subscribe [:reddit-subreddits])
          subredditNameAtom (rf/subscribe [:reddit-selected])
          subredditDataAtom (r/atom {@subredditNameAtom {}})]
        (fn []
            [ui/view 
                (cond
                    (empty? @subredditNameAtom)
                        [title "Nothing"]
                        ;[ui/custom-header2 "No subreddit selected."]

                    (empty? (get-in @subredditDataAtom [@subredditNameAtom]))
                        (do (net/http-get-json (str reddit-base-url @subredditNameAtom ".json")
                                (fn [data] (reset! subredditDataAtom {@subredditNameAtom data})))
                            [ui/custom-header2 "Loading..."])

                    :else
                        (let [posts (:children (:data (get @subredditDataAtom @subredditNameAtom )))]
                            [ui/view  {:style {:height utils/sh}}

                                [title @subredditNameAtom]

                                [ui/flat-list {:data (clj->js posts) :render-item reddit-post :key-extractor (fn [item index] (str index))}]]))


                [ui/animated-view {:pointerEvents "none"
                                   :style {:position "absolute" :left 0 :top -100 :width utils/sw :height (+ utils/sh 100)
                                           :backgroundColor style/col-black-full :opacity (:anim subSelectionAlpha) }}]
                [ui/animated-view {:style {:backgroundColor style/col-black :opacity 0.85 :position "absolute" :left 0 :top (:anim subSelectionY) :width utils/sw :height utils/sh}}
                        (let [subs @subredditsAtom]
                            [ui/flat-list {:data subs
                                           :render-item subs-selection
                                           :key-extractor (fn [item index] (str index))}])]
                

                    ])))
