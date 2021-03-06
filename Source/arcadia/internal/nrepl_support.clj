(ns arcadia.internal.nrepl-support
  (:require [arcadia.internal.autocompletion :as ac])
  (:import [BList]
           [BDictionary]))

(defn bencode-completion-result
  "Converts a seq of completion maps into a BList of BDictionary"
  [completions]
  #_(let [blist (BList.)]
    (doseq [candidate completions]
      (.Add blist (doto (BDictionary.)
                    (.Add "candidate" candidate))))
    blist) )

(defn complete [^String prefix]
  (bencode-completion-result
   (ac/completions prefix)))

(defn error-string [e]
  #_(let [error-opts (merge stacktrace/default-opts (:error-options (config/config)))]
    (stacktrace/exception-str e, error-opts))
  e)