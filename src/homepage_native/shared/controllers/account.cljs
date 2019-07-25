(ns homepage-native.shared.controllers.account
    (:require [reagent.core :as r :refer [atom]]
        [re-frame.core :as rf]
        [homepage-native.shared.utils :as utils]
        [homepage-native.shared.ui :as ui]
        [homepage-native.shared.networking :as networking]
        ))





(defn account-with-account [account logAtom]
    (fn []
        [ui/view
            [ui/custom-header2 "Manage account"]
            [ui/custom-header2 (str "Logged in as " (:name @account))]

            [ui/custom-button (str "Manually upload to " (:name @account) "'s cloud") {} #(networking/addConfig (:name @account) (:pass @account) logAtom nil)]

            [ui/custom-button "Log out" {} #(do
                                                (rf/dispatch-sync [:replace-db {}])
                                                (rf/dispatch-sync [:initialize])
                                                ;(rf/dispatch-sync [:page-changed :Account])
                                            )]
            ]))




(defn account-without-account [account logAtom]
    (let [usernameAtom (r/atom "")
          passwordAtom (r/atom "")]
        (fn []
            [ui/view {:style {:justifyContent "center"}}
                ;Register
                [ui/custom-header2 "Account"]
                [ui/custom-text-input usernameAtom {:width (* utils/sw 0.8)} "Username"]
                [ui/custom-text-input passwordAtom {:width (* utils/sw 0.8)} "Password" true]

                ;[ui/custom-button "Create an account"  {} #(addConfig @usernameAtom @passwordAtom logAtom nil)]
                [ui/custom-button "Create an account"  {:width (* utils/sw 0.8)} #(networking/addConfig @usernameAtom @passwordAtom logAtom nil)]

                ;Login
                ;[:input {:value "Log in" :on-click #(getConfig @usernameAtom @passwordAtom logAtom)}]
                [ui/custom-button "Log in"  {:width (* utils/sw 0.8)} #(networking/getConfig @usernameAtom @passwordAtom logAtom)]

                [ui/custom-button "Reset data" {} #(do
                                                       (rf/dispatch-sync [:replace-db {}])
                                                       (rf/dispatch-sync [:initialize])
                                                       ;(rf/dispatch-sync [:page-changed :Account])
                                                       (println "ciao!")
                                                    )]
                ])))





(defn main-controller []
    (let [account (rf/subscribe [:account])
          logAtom (r/atom "")]
        (fn []
            [ui/view
                [ui/custom-title "Account"]

                (if (not (empty? (:name @account)))
                    [account-with-account account logAtom]
                    [account-without-account account logAtom])
                [ui/view [ui/text @logAtom]]
                ])))
















