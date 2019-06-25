(ns homepage-native.shared.ui
    (:require [reagent.core :as r]
              [homepage-native.shared.style :as style]
              [homepage-native.shared.utils :as utils]))


; ------------------------------------------------------------
; Safe margins workaround
(def topInsetView (r/atom nil))
(def topInset (r/atom 0))

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
(def picker              (r/adapt-react-class (.-Picker utils/react)))
(def pickerItem          (r/adapt-react-class (.-Item (.-Picker utils/react))))
(def actionsheet         (.-ActionSheetIOS utils/react))



; ------------------------------------------------------------
; Animations
(def animated            (.-Animated utils/react))
(def animated-value      (.-Value animated))
(def animated-view       (r/adapt-react-class (.-View animated)))
(def easing              (.-Easing utils/react))



(defn anim-new-value
    "Creates a new animated value object with the animated value from react and a reagent atom."
    [value]
    {:anim (new animated-value value) :atom (r/atom value)})



(defn anim-set-value
    "Changes the value for the animated value object for both the value and the atom."
    [obj newValue]
    (reset! (:atom obj) newValue)
    (-> (:anim obj)
        (animated.timing #js {:toValue newValue :duration 500 :easing (.out easing (.poly easing 6))})
        (.start)))



(defn anim-get-value
    "Gets the atom value from the animated value object, this always represents the current target value."
    [obj]
    @(:atom obj))
        


; ------------------------------------------------------------
; Custom ui elements
(defn custom-button
    "A predefined button with default style."
    [label extraStyle f & [textStyle]]
    (fn [] [touchable-highlight {:style (merge {:background-color style/col-black :padding 10 :border-radius 5
                                               :margin-top 8 :margin-bottom 8 :margin-left "auto" :margin-right "auto"} extraStyle) :on-press f} 
            [text {:style (merge (style/style-text) {:color style/col-white :text-align "center" :font-weight "bold"} textStyle)}   (if (string? label) label @label)]]))



(defn custom-button-clear
    "A predefined button without the background."
    [label extraStyle f & [boxStyle]]
    (fn [] [touchable-opacity {:style (merge {:background-color nil :margin-top 8 :margin-bottom 8 :margin-left "auto" :margin-right "auto" :align-items "center" :justifyContent "center" } boxStyle) :on-press f} 
            [text {:style (merge (style/style-text) {:color style/col-black :text-align "center" :font-weight "bold"} extraStyle)} label]]))



(defn custom-text-input
    "A predefined text input with default style."
    [myAtom extraStyle & [default password]]
    (fn [] [text-input {:value @myAtom :placeholder (str default)
                       :autoCapitalize "none"
                       :secureTextEntry (if (nil? password) false password)
                       :style (merge (style/style-text style/col-dark-gray "400" 14) {:margin 8 :padding 10 :marginLeft "auto" :marginRight "auto"
                                      :border-radius 5 :backgroundColor style/col-white} extraStyle)
                       :onChangeText (fn [text] (reset! myAtom (clojure.string/trim text)))}]))



(defn custom-header1
    "A predefined big header with default style."
    [label & [extraStyle]]
    (fn [] [text {:style (merge (style/style-text style/col-black "800" 30) {:margin-top 0 :margin-bottom 4 :text-align "center"} extraStyle)} label]))
    


(defn custom-header2
    "A predefined smaller header with default style."
    [label & [extraStyle]]
    (fn [] [text {:style (merge (style/style-text style/col-black "600" 26) {:margin-top 0 :margin-bottom 4 :text-align "center"} extraStyle)} label]))


(defn custom-selection-input 
    "A custom button that triggers an action sheet selection. The button always shows the atom value."
    [myAtom itemsAtom & [extraStyle textStyle]]
    (fn [] [custom-button myAtom (if (nil? extraStyle) {} extraStyle)
              #(.showActionSheetWithOptions actionsheet
                   (clj->js {:options @itemsAtom })
                   (fn [buttonIndex] (reset! myAtom (nth @itemsAtom buttonIndex))))
              (merge {:textAlign "left"} (style/style-text style/col-dark-gray "400" 14) (if (nil? textStyle) {} textStyle)) ]))















