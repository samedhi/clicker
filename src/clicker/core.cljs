(ns clicker.core
  (:require
   [clicker.mui :as mui]
   [clicker.hashtels :as hashtels]
   [clojure.string :as string]
   [firemore.core :as firemore]
   [reagent.core :as reagent]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (reagent/atom {:text "Hello world!"}))

(defn hello-world []
  [:<>
   [mui/app-bar {:position :fixed :style {:flex-grow 1}}
    [mui/toolbar
     [mui/icon-button {:edge :start :color :inherit}
      [mui/icon "menu"]]
     [mui/typography {:variant :h6 :style {:flex-grow 1}}
      "Clicker"]
     [mui/button {:color :inherit} "Login"]]]

   [mui/container {:style {:margin-top "5em"}}
    [mui/grid {:container true :spacing 2}
     (for [sym '(I am the walrus Goo goo g'joob)
           :let [s (str sym)]]
       ^{:key s}
       [mui/grid {:item true :xs 12}
        [mui/card {:style (merge {:background-color (hashtels/pastel s)}
                                 (when (= 'walrus sym)
                                   {:border "2px solid black"}))}
         [mui/card-content
          [mui/typography {:variant :h5 :component :h2}
           (string/capitalize s)]
          [mui/typography {:color :textSecondary :gutter-bottom true}
           "Word of the day"]]]])]]])

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
