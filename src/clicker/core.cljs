(ns clicker.core
  (:require
   [cljs.core.async :as async]
   [clicker.mui :as mui]
   [clicker.hashtels :as hashtels]
   [clicker.silly-names :as silly-names]
   [clojure.string :as string]
   [firemore.core :as firemore]
   [firemore.firestore :as firemore.firestore]
   [goog.object :as goog.object]
   [reagent.core :as reagent])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

;; STATE

(defonce app (reagent/atom {:history {}}))

;; ACTIONS

(defn add-player [game-id user-id]
  (go (firemore/write!
       [:games game-id :players user-id]
       {:score 0 :user-id user-id})))

(defn join-game [game-id user-id]
  (go (add-player game-id user-id)
      (firemore/add! app [:game] [:games game-id])
      (firemore/add! app [:players] [:games game-id :players])))

(defn create-game [user-id]
  (go (let [game-id (->> (firemore.firestore/add-db! [:games] {:user user-id
                                                               :name "The greatest show on Earth"})
                         async/<!
                         :id)]
        (goog.object/set js/window.location "hash" game-id)
        (join-game game-id user-id))))

(defn leave-game [game-id]
  (go (firemore/subtract! app [:game])
      (firemore/subtract! app [:players])))

(defn update-player [game-id user-id fx]
  (go (let [reference [:games game-id :players user-id]
            old-xs (get-in @app [:firestore :players])]
        (when-let [old (->> old-xs (filter #(= (:user-id %) user-id)) first)]
          (let [{:keys [score]} old]
            (firemore/merge! reference {:score (fx score)}))))))

(defn inc-player [game-id user-id]
  (update-player game-id user-id inc))

(defn dec-player [game-id user-id]
  (update-player game-id user-id dec))

(def index->weight
  {0 2
   1 4
   2 8
   3 16
   4 16})

(defn add-to-history [history player]
  (let [{:keys [user-id score]} player]
    (-> history
        (update user-id #(cons score %))
        (update user-id #(take (-> index->weight count inc) %)))))

(defn update-history [app]
  (let [{:keys [players]} (:firestore app)]
    (update app
            :history
            #(reduce
              add-to-history
              %
              players))))

(def instance-uuid (atom (random-uuid)))

(go-loop [my-uuid @instance-uuid]
  (async/<! (async/timeout 1000))
  (when (= my-uuid @instance-uuid)
    (swap! app update-history)
    (recur my-uuid)))

(defn average-delta [vs]
  (->> (partition 2 1 vs)
       (map-indexed (fn [i [old new]] (/ (- old new) (index->weight i))))
       (reduce +)))

(defn colorize [n]
  [mui/typography {:align :right
                   :variant :h4
                   :style {:color (if (neg? n) "red" "green")
                           :user-select :none}}
   n])

;; VIEWS

(defn hello-world []
  (let [{:keys [firestore my-user-id history]} @app
        {:keys [game players]} firestore
        game-id (-> game meta :id)]
    [:<>
     [mui/app-bar {:position :fixed :style {:flex-grow 1}}
      [mui/toolbar

       [mui/typography {:variant :h6 :style {:flex-grow 1}}
        "Clicker"]
       [mui/typography {:variant :h4 :style {:flex-grow 1}}
        (-> my-user-id hash silly-names/consistent)]
       [mui/button {:variant :outlined
                    :color :inherit
                    :on-click #(create-game my-user-id)}
        "New Game"]]]

     [mui/container {:style {:margin-top "5em"}}
      [mui/typography (pr-str @app)]
      [mui/grid {:container true :spacing 2}
       (for [{:keys [score user-id] :as player} players
             :let [me? (= user-id my-user-id)]]
         ^{:key user-id}
         [mui/grid {:item true :xs 12}
          [mui/card {:on-click (if me?
                                 #(inc-player game-id user-id)
                                 #(dec-player game-id user-id))
                     :style (merge {:background-color (hashtels/pastel user-id)}
                                   (when me? {:border "2px solid black"}))}
           [mui/card-content
            [mui/grid {:container true}
             [mui/grid {:item true
                        :container true
                        :xs 4
                        :justify :center
                        :align-items :center}
              [mui/grid {:item true}
               [mui/typography {:variant :h5
                                :component :h2
                                :style {:user-select :none}}
                (-> user-id hash silly-names/consistent)]]]
             [mui/grid {:item true :xs 4}
              [mui/typography {:align :center
                               :variant :h4
                               :color :textSecondary
                               :style {:user-select :none}}
               (str "Score: " score)]]
             [mui/grid {:item true
                        :container true
                        :justify :flex-end
                        :xs 4}
              (-> (get history user-id)
                  (or [0 0 0 0 0])
                  average-delta
                  colorize)]]]]])]]]))

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

;; INIT
(go
  (let [user-id (async/<! (firemore/uid))
        game-id js/window.location.hash]
    (swap! app assoc :my-user-id user-id)
    (when-not (string/blank? game-id)
      (-> game-id
          (subs 1)
          (join-game user-id)))))
