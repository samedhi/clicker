(ns clicker.hashtels)

(defn n-from-hash-of [o]
  (let [n (* 10 1000)]
    (-> o hash (mod n) (/ n))))

;; https://stackoverflow.com/questions/43193341/how-to-generate-random-pastel-or-brighter-color-in-javascript
(defn pastel [o]
  (let [n (n-from-hash-of o)]
    (str "hsl(" (* 360 n) ",70%,85%)")))
