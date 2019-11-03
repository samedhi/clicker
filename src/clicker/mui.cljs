(ns clicker.mui
  (:require
   [cljsjs.material-ui])
  (:require-macros
   [clicker.mui :refer [declare-mui-definitions]])
  (:refer-clojure :exclude [list]))

(declare-mui-definitions)
