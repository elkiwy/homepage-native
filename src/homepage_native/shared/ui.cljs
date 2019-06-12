(ns homepage-native.shared.ui
    (:require [reagent.core :as r :refer [atom]]
              [homepage-native.shared.style :as style]
              [homepage-native.shared.utils :as utils]))



; ------------------------------------------------------------
; External libraries
(def gradient (r/adapt-react-class (.-default (js/require "react-native-linear-gradient"))))

; ------------------------------------------------------------
; Components
(def text                (r/adapt-react-class (.-Text utils/react)))
(def text-input          (r/adapt-react-class (.-TextInput utils/react)))
(def safe-area-view      (r/adapt-react-class (.-SafeAreaView utils/react)))
(def view                (r/adapt-react-class (.-View utils/react)))
(def flat-list           (r/adapt-react-class (.-FlatList utils/react)))
(def image               (r/adapt-react-class (.-Image utils/react)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight utils/react)))
(def touchable-opacity   (r/adapt-react-class (.-TouchableOpacity utils/react)))




; ------------------------------------------------------------
; Custom ui elements
(defn custom-button [label extraStyle f]
    (fn [] [touchable-highlight {:style (merge {:background-color style/col-black :padding 10 :border-radius 5
                                               :marginTop 8 :marginBottom 8 :marginLeft "auto" :marginRight "auto"} extraStyle) :on-press f} 
            [text {:style {:color style/col-white :text-align "center" :font-weight "bold"}} label]]))


(defn custom-text-input [myAtom extraStyle & [default password]]
    (fn [] [text-input {:value @myAtom :placeholder (str default)
                       :autoCapitalize "none"
                       :secureTextEntry (if (nil? password) false password)
                       :style (merge {:margin 8 :padding 10 :marginLeft "auto" :marginRight "auto"
                                      :border-radius 5 :backgroundColor style/col-white} extraStyle)
                       :onChangeText (fn [text] (reset! myAtom (clojure.string/trim text)))}]))


(defn custom-header1 [label & [extraStyle]]
    (fn [] [text {:style (merge (style/style-text style/col-black "800" 30) {:margin-top 0 :margin-bottom 4 :text-align "center"})} label]))
    

(defn custom-header2 [label & [extraStyle]]
    (fn [] [text {:style (merge (style/style-text style/col-black "600" 26) {:margin-top 0 :margin-bottom 4 :text-align "center"})} label]))
