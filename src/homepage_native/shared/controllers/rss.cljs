(ns homepage-native.shared.controllers.rss
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :as rf]
            [homepage-native.shared.utils :as utils]
            [homepage-native.shared.style :as style]
            [homepage-native.shared.ui :as ui]
            [homepage-native.shared.networking :as net]
            [homepage-native.shared.db]))



(:rss @re-frame.db/app-db)

(rf/subscribe [:rss-feeds])
(rf/subscribe [:rss-selected-name])
(rf/subscribe [:rss-selected-url])

(def rss-proxy "https://api.rss2json.com/v1/api.json?rss_url=")
(def data-atom (r/atom nil))











(defn rss-item [item]
    (r/create-element
        (r/reactify-component
            (let []
                (fn []
                    (let [item (:item (js->clj item :keywordize-keys true) )
                          descriptionHtml (:description item)
                          textStyle {:style (merge (style/style-text) {:flex 1 :padding 12})}
                          ;descriptionData (map hc/as-hiccup (hc/parse-fragment descriptionHtml))
                          ;component       (find-tag (first descriptionData) :img)
                          ;componentData   (second component)
                          ;componentStyle  {:float "left" :overflow "auto" :width 72 :height 72 :border-radius 2
                                           ;:box-shadow "4px 4px 16px -10px black" :margin-right 12}
                          ;styledComponent [:img (assoc componentData :style componentStyle)]
                          ]
                        [ui/view {:style (merge (style/style-light-background-and-border)
                                                {:margin-top 1 :margin-bottom 1})}
                            
                            [ui/text textStyle (:title item)]]))))))


(defn main-controller []
    (let [feeds (rf/subscribe [:rss-feeds])
          feed-data-name (rf/subscribe [:rss-selected-name])
          feed-data-atom (r/atom {@feed-data-name {}})]
        (fn []
            [ui/view
                (cond
                    ;No rss case
                    (empty? @feed-data-name)
                        [ui/custom-title "Nothing"]

                    ;Loading feed case
                    (empty? (get @data-atom @feed-data-name))
                        (do (net/http-get-json (str rss-proxy (get @feeds (keyword @feed-data-name)))
                                (fn [data] (reset! data-atom {@feed-data-name data})))
                            [ui/custom-header2 "Loading..."])

                    ;Normal case
                    :else
                        (let [items (get (get @data-atom @feed-data-name) :items)]
                            [ui/view  {:style {:height (- utils/sh @ui/topInset)}}
                                [ui/custom-title (str @feed-data-name)]
                                [ui/flat-list {:data items
                                               :render-item rss-item
                                               :key-extractor (fn [item index] (str index))}]]))



                ;[rss-feed]
                ])))


