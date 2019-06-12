(ns homepage-native.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :as rf]
            [homepage-native.shared.ui :as ui]
            [homepage-native.shared.style :as style]
            [homepage-native.shared.utils :as utils]
            [homepage-native.shared.networking :as net]
            [homepage-native.shared.controllers.reddit :as ctrl-reddit]
            [homepage-native.shared.account :as account]
            [homepage-native.db]))







(defn app-root []
    (let []
        (fn []
            (let [rand-hue (rand-int 359)]
                (reset! style/col-accent1 (str "hsl(" rand-hue ", 30%, 70%)"))
                (reset! style/col-accent2 (str "hsl(" (+ rand-hue 80) ", 30%, 70%)"))
                [ui/view {:style {:flex-direction "column" :height "100%" :align-items "center" }}
                    [ui/gradient {:style {:position "absolute" :left 0 :top 0 :width "100%" :height "100%"} :colors [@style/col-accent1 @style/col-accent2] :start {:x 0 :y 0} :end {:x 1 :y 1}}]
                    [ui/safe-area-view {:style {:width utils/sw :flex 1}}
                        [account/main-controller]
                        ;[ui/view
                            ;j[ui/text "cippo"]]
                        ]]))))







(defn init []
    (rf/dispatch-sync [:initialize])
    (.registerComponent (.-AppRegistry utils/react) "homepageNative" #(r/reactify-component app-root)))

