(ns tasks)

(defn build []
  (binding [*compile-path* "Infrastructure"
            clojure.core/*loaded-libs* (ref (sorted-set))]
    (compile 'nostrand.core)
    (compile 'nostrand.tasks)))