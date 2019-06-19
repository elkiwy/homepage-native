(ns homepage-native.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :as rf]
            [re-frame.db :as rfdb]
            [homepage-native.shared.ui :as ui]
            [homepage-native.shared.style :as style]
            [homepage-native.shared.utils :as utils]
            [homepage-native.shared.networking :as net]
            [homepage-native.shared.controllers.reddit :as ctrl-reddit]
            [homepage-native.shared.controllers.account :as ctrl-account]
            [homepage-native.shared.db :as db]))




(def topInsetView (r/atom nil))
(def topInset (r/atom 0))

(def pages {:Reddit ctrl-reddit/main-controller :Account ctrl-account/main-controller})
(def settings {:Reddit ctrl-reddit/settings-view :Account nil})

(def sidebarW (* utils/sw 0.8))
(defonce sidebar-animvalue-x (ui/anim-new-value (* -1 sidebarW)))
(defonce setting-animvalue-x (ui/anim-new-value utils/sw))




(defn toggle-settings []
    (ui/anim-set-value setting-animvalue-x (if (= (ui/anim-get-value setting-animvalue-x) 0) utils/sw 0)))
    

(defn toggle-sidebar []
    (ui/anim-set-value sidebar-animvalue-x (if (= (ui/anim-get-value sidebar-animvalue-x) 0) (* -1 sidebarW) 0)))


(defn back-button [f]
    (fn []
        [ui/view {:style {:width (* utils/sw 0.05) :height 50 :position "absolute"
                          :margin-top (- @topInset 10) :margin-left (* utils/sw 0.05) }}
            [ui/custom-button-clear "<" {:color style/col-white :font-size 30} f]]))


(defn sidebar-item [label key & [last?]]
    (let [f (fn [page] (do (rf/dispatch [:page-changed page]) (ui/anim-set-value sidebar-animvalue-x (* -1 sidebarW))))]
        (fn []
            [ui/view {:style {:border-bottom-color style/col-medium-gray :border-bottom-width (if last? 0 1) :margin-left 15 :margin-right 15}}
                [ui/custom-button-clear label {:marginTop 10 :marginBottom 10 :color style/col-white :font-size 22 :font-weight "600"} #(f key)]])))

(defn sidebar [posAnimVal]
    (fn []
        [ui/animated-view {:style {:position "absolute" :left posAnimVal :width sidebarW :height utils/sh
                                   :paddingTop @topInset :backgroundColor (str style/col-black-full "fc")
                                   :border-right-color style/col-medium-gray :border-right-width 1 }}
            ;Header
            [ui/view {:style {:border-bottom-color @style/col-accent1 :border-bottom-width 1 :padding-bottom 15 :margin-left 15 :margin-right 15}}
                [ui/custom-header1 "Pages" {:color style/col-white}]]
            ;Items
            [sidebar-item "Favorites" :Favorites]
            [sidebar-item "Reddit" :Reddit]
            [sidebar-item "Rss" :Rss]
            [sidebar-item "Account" :Account true]
            [back-button toggle-sidebar]]))





(defn app-root []
    (let [page (rf/subscribe [:page-current])]
        (fn []
            [ui/view {:style {:flex-direction "column" :height "100%" :align-items "center" }}
                ;Gradient background
                [ui/gradient {:style {:position "absolute" :left 0 :top 0 :width "100%" :height "100%"} :colors [@style/col-accent1 @style/col-accent2] :start {:x 0 :y 0} :end {:x 1 :y 1}}]


                ;Main working area
                [ui/safe-area-view {:style {:width utils/sw :flex 1}}
                    ;Workaround for the safe area insets
                    [ui/view {:ref (fn [me] (reset! topInsetView me)) :style {:height 0}
                              :onLayout (fn [e] (.measure @topInsetView (fn [_ y _ _ _ _] (reset! topInset y))))}]

                    ;Main screen
                    (when (not (nil? @page))
                        (let [p ((keyword @page) pages)]
                            (if (nil? p)
                                [ui/custom-header1 (str "No view ready for " @page)]
                                [p])))

                    ;Sidebar button
                    [ui/view {:style {:width (* utils/sw 0.1) :position "absolute" :margin-top (- @topInset 10) :margin-left (* utils/sw 0.05)}}
                        [ui/custom-button-clear "=" {:color style/col-black :font-size 30}
                            toggle-sidebar]]


                    ;Page settings button
                    [ui/view {:style {:width (* utils/sw 0.1) :position "absolute" :margin-top (- @topInset 10) :margin-left (* utils/sw 0.85)}}
                        [ui/custom-button-clear "+" {:color style/col-black :font-size 30}
                            toggle-settings]]
                    


                    ;Page settings
                    (when (not (nil? @page))
                        (let [p ((keyword @page) settings)]
                            [ui/animated-view {:style {:backgroundColor (str style/col-black-full "f0") :position "absolute" :paddingTop @topInset 
                                                           :top 0 :left (:anim setting-animvalue-x) :width utils/sw :height utils/sh}}
                                (if (nil? p)
                                    [ui/view [ui/custom-header2 "No settings for this page" {:color style/col-white}]]
                                    [p])
                                [back-button toggle-settings]]))

                    ;sidebar
                    [sidebar (:anim sidebar-animvalue-x) ]
                ]
            ])))








(defn init []
    (println "init")
    (db/load-state)
        
    ;(net/try-download-state)
    (.registerComponent (.-AppRegistry utils/react) "homepageNative" #(r/reactify-component app-root)))

