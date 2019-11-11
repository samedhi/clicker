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

(defonce game-id (atom nil))

;; ACTIONS

(defn create-game [user-id]
  (go
    (reset!
     game-id
     (:id
      (async/<!
       (firemore.firestore/add-db! [:games] {:user user-id :name "The greatest show on Earth"}))))))

#_(create-game "test-user-id")

(defn add-player [game-id user-id]
  (go
    (async/<!
     (firemore/write!
      [:games game-id :players user-id]
      {:score 0 :user-id user-id}))))

#_(add-player @game-id "test-user-id")

(defn join-game [game-id]
  (go
    (firemore/add! app [:game] [:games game-id])
    (firemore/add! app [:players] [:games game-id :players])))

#_(join-game @game-id)

(defn leave-game [game-id]
  (go
    (async/<!
     (firemore/subtract! app [:game]))))

#_(leave-game @game-id)

(defn update-player [game-id user-id fx]
  (go
    (let [reference [:games game-id :players user-id]
          old-xs (get-in @app [:firestore :players])]
      (when-let [old (->> old-xs (filter #(= (:user-id %) user-id)) first)]
        (let [{:keys [score]} old]
          (firemore/merge! reference {:score (fx score)}))))))

(defn inc-player [game-id user-id]
  (update-player game-id user-id inc))

(defn dec-player [game-id user-id]
  (update-player game-id user-id dec))

;; VIEWS

(defn hello-world []
  (let [{:keys [game players]} (:firestore @app)
        game-id (-> game meta :id)]
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
       (for [{:keys [score user-id] :as player} players
             :let [me? (= user-id "test-user-id")]]
         ^{:key user-id}
         [mui/grid {:item true :xs 12}
          [mui/card {:on-click (if me?
                                 #(inc-player game-id user-id)
                                 #(dec-player game-id user-id))
                     :style (merge {:background-color (hashtels/pastel user-id)}
                                   (when me? {:border "2px solid black"}))}
           [mui/card-content
            [mui/typography {:variant :h5 :component :h2}
             user-id]
            [mui/typography {:color :textSecondary :gutter-bottom true}
             (-> user-id hash silly-names/consistent)]]]])]]]))

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
