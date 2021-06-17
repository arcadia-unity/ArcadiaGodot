(ns arcadia.internal.cmd
  (:require
   [clojure.string :as string]))

(defn- flag? [s]
  (string/starts-with? s "--"))

(defn parse-opts [opts]
  (reduce
   (fn [{:keys [current] :as acc} x]
     (cond
       (and current (flag? x))
       (-> acc
           (update :flags conj (keyword current))
           (assoc :current x))
       current
       (-> acc
           (assoc-in [:pairs (keyword (subs current 2))] x)
           (assoc :current nil))
       (flag? x)
       (assoc acc :current x)
       :else
       (update acc :singles conj (keyword x))))
   {:current nil
    :flags #{}}
   opts))
