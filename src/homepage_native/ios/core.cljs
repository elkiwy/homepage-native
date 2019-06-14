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

(def sidebarW (* utils/sw 0.8))
(def sidebar-animvalue-x (ui/anim-new-value (* -1 sidebarW)))



(defn sidebar [posAnimVal]
    (let [s {:marginTop 20 :color style/col-white :font-size 22 :font-weight "600"}
          f (fn [page] (do (rf/dispatch [:page-changed page]) (println (str page)) (ui/anim-set-value sidebar-animvalue-x (* -1 sidebarW))))]
        (fn []
            [ui/animated-view {:style {:position "absolute" :left posAnimVal :width sidebarW :height utils/sh :paddingTop @topInset :backgroundColor style/col-black}}
                [ui/custom-button-clear "Favorites" s #(f :Favorites)]
                [ui/custom-button-clear "Reddit"    s #(f :Reddit)]
                [ui/custom-button-clear "Rss"       s #(f :Rss)]
                [ui/custom-button-clear "Account"   s #(f :Account)]])))




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
                    (let [p ((keyword @page) pages)]
                        (if (nil? p)
                            [ui/text (str "No view ready for " @page)]
                            [p]))

                    ;Sidebar
                    [sidebar (:anim sidebar-animvalue-x) ]
                    [ui/view {:style {:width (* utils/sw 0.1) :position "absolute" :margin-top (- @topInset 10) :margin-left (* utils/sw 0.05)}}
                        [ui/custom-button-clear "=" {:color style/col-black :font-size 30}
                            #(ui/anim-set-value sidebar-animvalue-x (if (= (ui/anim-get-value sidebar-animvalue-x) 0) (* -1 sidebarW) 0))]]
                ]])))








(defn init []
    (println "init")
    (rf/dispatch-sync [:initialize])
    (db/load-state)
        
    ;(net/try-download-state)
    (.registerComponent (.-AppRegistry utils/react) "homepageNative" #(r/reactify-component app-root)))

