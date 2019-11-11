(ns clicker.core
  (:require
   [cljs.core.async :as async]
   [clicker.mui :as mui]
   [clicker.hashtels :as hashtels]
   [clicker.silly-names :as silly-names]
   [clojure.string :as string]
   [firemore.core :as firemore]
   [firemore.firestore :as firemore.firestore]
   [reagent.core :as reagent])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

;; STATE

(defonce app (reagent/atom {:text "Hello world!"}))

;; ACTIONS

(defn create-game [user-id]
  (go
    (async/<!
     (firemore.firestore/add-db! [:games] {:user user-id :name "The greatest show on Earth"}))))

#_(create-game "test-user-id")

(defn add-player [game-id user-id]
  (go
    (async/<!
     (firemore/write!
      [:games game-id :players user-id]
      {:score 0 :user-id user-id}))))

#_(add-player "IBbIOTrgDzTGyJOPmy1W" "test-user-id")

(defn join-game [game-id user-id]
  (go
    (firemore/add! app [:game] [:games game-id])
    (firemore/add! app [:players] [:games game-id :players])))

#_(join-game "IBbIOTrgDzTGyJOPmy1W")

(defn leave-game [game-id]
  (go
    (async/<!
     (firemore/subtract! app [:game]))))

#_(leave-game "IBbIOTrgDzTGyJOPmy1W")

(defn click-player [game-id user-id]
  (go
    (let [reference [:games game-id :players user-id]
          old-xs (get-in @app [:firestore :players])]
      (when-let [old (->> old-xs (filter #(= (:user-id %) user-id)) first)]
        (let [{:keys [score]} old]
          (firemore/merge! reference {:score (inc score)}))))))

#_(click-player "IBbIOTrgDzTGyJOPmy1W" "test-user-id")

;; VIEWS

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
           (silly-names/random)]]]])]]])

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
