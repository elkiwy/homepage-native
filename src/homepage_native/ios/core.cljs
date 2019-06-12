(ns homepage-native.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :as rf]
            [homepage-native.shared.ui :as ui]
            [homepage-native.shared.style :as style]
            [homepage-native.shared.utils :as utils]
            [homepage-native.shared.networking :as net]
            [homepage-native.shared.controllers.reddit :as ctrl-reddit]
            [homepage-native.shared.controllers.account :as ctrl-account]
            [homepage-native.shared.db]))



(def sidebarW (* utils/sw 0.8))

(def topInsetView (r/atom nil))
(def topInset (r/atom 0))


(defn sidebar [posAnimVal]
    (fn []
        [ui/animated-view {:style {:position "absolute"
                                   :left posAnimVal :width sidebarW :height utils/sh
                                   :paddingTop @topInset
                                   :backgroundColor style/col-black}}
                [ui/custom-button-clear "Favorites" {:color style/col-white} #()]
                [ui/custom-button-clear "Reddit"    {:color style/col-white} #()]
                [ui/custom-button-clear "Rss"       {:color style/col-white} #()]
                [ui/custom-button-clear "Account"   {:color style/col-white} #()]]))







(defn app-root []
    (let [sidebar-value-x (ui/anim-new-value (* -1 sidebarW))]
        (fn []
            (let [rand-hue (rand-int 359)]
                (reset! style/col-accent1 (str "hsl(" rand-hue ", 30%, 70%)"))
                (reset! style/col-accent2 (str "hsl(" (+ rand-hue 80) ", 30%, 70%)"))



                [ui/view {:style {:flex-direction "column" :height "100%" :align-items "center" }}
                    [ui/gradient {:style {:position "absolute" :left 0 :top 0 :width "100%" :height "100%"} :colors [@style/col-accent1 @style/col-accent2] :start {:x 0 :y 0} :end {:x 1 :y 1}}]

                    [ui/safe-area-view {:style {:width utils/sw :flex 1}}


                        [ui/view {:ref (fn [me] (reset! topInsetView me)) :style {:height 0}
                                  :onLayout (fn [e] (.measure @topInsetView (fn [_ y _ _ _ _] (reset! topInset y))))}]


                        

                        
                        [sidebar (:anim sidebar-value-x)]


                        [ui/view {:style {:width (* utils/sw 0.1) :position "relative" :margin-left (* utils/sw 0.1)}}
                            [ui/custom-button-clear "=" {:color style/col-black }
                                #(ui/anim-set-value sidebar-value-x (if (= (ui/anim-get-value sidebar-value-x) 0) (* -1 sidebarW) 0))]]


                        ]]



                ))))







(defn init []
    (rf/dispatch-sync [:initialize])
    (.registerComponent (.-AppRegistry utils/react) "homepageNative" #(r/reactify-component app-root)))

