(ns clicker.mui
  (:require
   [clojure.string :as string]
   [clicker.mui-symbols :as mui-symbols]))

(defn kebab-case [s]
  (string/join "-" (map string/lower-case (re-seq #"\w[a-z]+" s))))

(defn def-mui-by-symbol [sym]
  (let [s (str sym)]
    `(def ~(-> s kebab-case symbol)
       (reagent.core/adapt-react-class (goog.object/get js/window.MaterialUI ~s)))))

(defmacro declare-mui-definitions []
  `(do ~@(map def-mui-by-symbol mui-symbols/mui-symbols)))
