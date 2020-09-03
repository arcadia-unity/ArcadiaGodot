(ns magic.peephole
  (:require [mage.core :as il]))

(defn normalize [il]
  (->> il
       flatten
       (remove nil?)))

(defn pass [il]
  (let [il* (normalize il)]
    (println "[peephole]" (filter ::il/method il*))
    il*))

;; drop in replacement
(defn emit!
  ([stream] (emit! {} stream))
  ([initial-ctx stream]
   (il/emit! initial-ctx (pass stream))))