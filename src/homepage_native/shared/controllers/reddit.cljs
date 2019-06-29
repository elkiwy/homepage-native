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





              ;:author "Mustardforyou"

              ;:created 1554023315
              ;:domain "discord.gg"

              ;:downs 0
              ;:ups 296
              ;:score 296
              ;:num_comments 49

              ;:preview {:images [{:source {:url "https://external-preview.redd.it/T2UIMQ3uvC16xcd5gc6aCPeoOE7CdKnf-arERS5PCJA.jpg?auto=webp&amp;s=c422ea97905baf0b46680f61bca5b8480cccdc4b" :width 256 :height 256} :resolutions [{:url "https://external-preview.redd.it/T2UIMQ3uvC16xcd5gc6aCPeoOE7CdKnf-arERS5PCJA.jpg?width=108&amp;crop=smart&amp;auto=webp&amp;s=ac33d4801eaabbfe91931ee21ddedbbccf0493ab" :width 108 :height 108} {:url "https://external-preview.redd.it/T2UIMQ3uvC16xcd5gc6aCPeoOE7CdKnf-arERS5PCJA.jpg?width=216&amp;crop=smart&amp;auto=webp&amp;s=e1d02c0f58f919aaddced6a4bc50c34aeb2cc412" :width 216 :height 216}] :variants {} :id "_ea2YllHFhs3sRFzrQD4LPdD0xPtYXLYGi-oKu-6f3E"}] :enabled false}

              ;:thumbnail "https://b.thumbs.redditmedia.com/dXAU8_8k9pxIySie4SsPBZ1U50OaHI1giiwm78TpaFY.jpg"
              ;:thumbnail_height 140
              ;:thumbnail_width 140

              ;:title "Join the official /r/Meme Discord server!"
              ;:url "https://discord.gg/GA7B7tg"
              ;:permalink "/r/meme/comments/b7ia84/join_the_official_rmeme_discord_server/"




(def post-image-height 100)
(def post-image-margin 24)
(def post-extra-height 100)



(defn reddit-post [item] ^{:key item}
    (r/create-element
        (r/reactify-component 
            (let [myRef   (r/atom nil)
                  min-h   (r/atom 0)]
                (fn []
                    (let [info (:item (js->clj item :keywordize-keys true))
                          max-h      (+ @min-h post-image-height (* post-image-margin 2) post-extra-height)
                          animHeight (ui/anim-new-value @min-h)
                          animSep    (ui/anim-new-value 0)
                          textStyle  {:style (merge (style/style-text) {:flex 1 :padding 12})}
                          {title :title
                           url   :url
                           thumbnail :thumbnail
                           permalink :permalink
                           selftext :selftext}  (:data info)]

                        ;Main holder
                        [ui/view
                            ;Measurable view (Couldn't managed to get things working on animated-views, TODO this one day)
                            [ui/view {:style {:position "absolute" :left -1000 :top -1000 :hidden true}   :ref (fn [me] (reset! myRef me))
                                      :onLayout (fn [e] (.measure @myRef (fn [_ _ _ h _ _] (reset! min-h h))))}
                                [ui/text (merge textStyle {:opacity 0}) (str title)]]

                            ;Actual view
                            [ui/animated-view {:style (merge (style/style-light-background-and-border)
                                                             {:height (:anim animHeight) :width utils/sw :margin-top 1 :margin-bottom 1 :overflow "hidden"})}
                                ;Main List row
                                [ui/touchable-opacity {:on-press #(ui/toggle-section-height animHeight @min-h max-h animSep)
                                                        :style {:flex-direction "row"}}
                                    [ui/text textStyle (str title)]]

                                ;Separator
                                [ui/animated-view {:style {:backgroundColor @style/col-accent1 :width (:anim animSep) :height 1
                                                           :margin-left "auto" :margin-right "auto"}}]


                                ;Post detail
                                (let [image? (not (or (empty? thumbnail) (= thumbnail "self") (= thumbnail "default")))]
                                    [ui/view {:style {:flex-direction "row" :flex 1}}
                                        ;Image
                                        (when image?
                                            [ui/image {:source {:uri thumbnail}
                                                       :style {:borderWidth 2 :borderColor @style/col-accent2 :margin post-image-margin :width post-image-height :height post-image-height}}])

                                        ;Selftext / external url
                                        (if (empty? selftext)
                                            ;External URL link
                                            [ui/view {:style {:flex 1 :margin-top post-image-margin }}
                                                ;External url label
                                                [ui/view {:style {:flex 1 :flex-direction "row" }}
                                                    [ui/custom-header2  "External link to:" (merge {:flex 1 :flexWrap "wrap" :margin post-image-margin :margin-left 0}
                                                                                                (style/style-text style/col-white "400" 16))]]

                                                ;Link
                                                [ui/touchable-opacity {:on-press #(net/http-open-url url) :style {:flex 3}} 
                                                    [ui/text {:style (merge (style/style-text style/col-white "300" 18)
                                                                        {:flex 1 :textAlign "center" :margin post-image-margin :margin-left (if image? 0 post-image-margin)})}
                                                        url]]]

                                            ;Selftext
                                            [ui/view
                                                ;Full post link
                                                [ui/touchable-opacity {:on-press #(net/http-open-url (str "https://reddit.com" permalink)) } 
                                                    [ui/text {:style (merge (style/style-text style/col-white "400" 16)
                                                                            {:height 20 :textDecorationLine "underline"
                                                                            :textAlign "center" :margin-top post-image-margin})}
                                                        "Full post"]]

                                                ;Text
                                                [ui/text {:style (merge (style/style-text style/col-white "200" 12)
                                                                        {:textAlign "justify" :margin post-image-margin :margin-left (if image? 0 post-image-margin)})}
                                                    selftext]])])

                            ]]))))))



(defn toggle-selection-view []
    (let [current (ui/anim-get-value subSelectionY)
          toHide? (= current subSelectionYMin)]
        (ui/anim-set-value subSelectionAlpha (if toHide? 0 0.95))
        (ui/anim-set-value subSelectionY (if toHide? subSelectionYMax subSelectionYMin))))


(defn title [name]
    (fn []
        [ui/view {:style {:border-bottom-width 2 :border-bottom-color @style/col-accent2}}
            [ui/custom-button-clear (str "r/" name) {:font-size 22} #(toggle-selection-view)]]))



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
                [ui/custom-text-input newSubName {} "subreddit-name"]
                [ui/custom-button "Add" {:backgroundColor @style/col-accent2} #(rf/dispatch [:reddit-added-subreddit @newSubName])]

                ;Remove fav
                [ui/custom-header2 "Remove a subreddit" {:color style/col-white :margin-top 30}]
                [ui/custom-selection-input subToRemove subreddits {}]
                [ui/custom-button "Remove" {:backgroundColor @style/col-accent2} #(do (rf/dispatch-sync [:reddit-removed-subreddit @subToRemove])
                                                                                                              (reset! subToRemove (get-first-subreddit)))]])))



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
                            [ui/view  {:style {:height (- utils/sh @ui/topInset )}}


                                ;Title
                                [title @subredditNameAtom]

                                ;Post list
                                [ui/flat-list {:data (filter #(not (nil? (:data %))) posts) :render-item reddit-post :key-extractor (fn [item index] (str index))}]]))


                ;Hidden subreddit selection view
                [ui/animated-view {:pointerEvents "none" :style {:position "absolute" :left 0 :top -100 :width utils/sw :height (+ utils/sh 100)
                                                                 :backgroundColor style/col-black-full :opacity (:anim subSelectionAlpha) }}]
                [ui/animated-view {:style {:backgroundColor style/col-black :opacity 0.85 :position "absolute"
                                           :left 0 :top (:anim subSelectionY) :width utils/sw :height utils/sh}}
                    [ui/flat-list {:data @subredditsAtom :render-item subs-selection :key-extractor (fn [item index] (str index))}]]
                

                    ])))
