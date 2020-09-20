(ns arcadia.internal.namespace)

(defn quickquire
  "Bit like require, but fails with fewer allocations"
  [ns-sym]
  (when-not (contains? (loaded-libs) ns-sym)
    (require ns-sym)))

(defn ^clojure.lang.IFn eval-ifn [s]
  (when-let [vs (first (re-seq #"[^ \t\n\r]+" s))]
    (when-let [-ns (last (re-find #"#'([^/]+)\/.+" s))]
      (try 
        (require (symbol -ns))
        (eval (read-string s))
        (catch Exception e 
          (GD/PrintErr (into-array [e])))))))