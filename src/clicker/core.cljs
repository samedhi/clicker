(ns clicker.core
  (:require
   [clicker.mui :as mui]
   [firemore.core :as firemore]
   [reagent.core :as reagent]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (reagent/atom {:text "Hello world!"}))

(defn n-from-hash-of [o]
  (let [n (* 10 1000)]
    (-> o hash (mod n) (/ n))))

;; https://stackoverflow.com/questions/43193341/how-to-generate-random-pastel-or-brighter-color-in-javascript
(defn pastel [o]
  (let [n (n-from-hash-of o)]
    (str "hsl(" (* 360 n) ",70%,85%)")))

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
    [mui/grid-list {:cell-height 120  :cols 1}
     (for [sym '(I am the walrus Goo goo g'joob)
           :let [s (str sym)]]
       [mui/grid-list-tile {:key s
                            :style {:background-color (pastel s)}}
        [mui/grid-list-tile-bar
         {:title (str "A study in '" s "'")
          :subtitle (str "by: " s "-kun")}]
        [mui/typography s]])]]])


(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
