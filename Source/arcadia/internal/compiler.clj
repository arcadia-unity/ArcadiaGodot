(ns arcadia.internal.compiler
  (:use
   arcadia.core)
  (:require
   [clojure.string :as string]
   [arcadia.internal.config :as config])
  (:import
   [Arcadia.Boot]))

(defn- aot-namespaces [path nss]
   ;; We want to ensure that namespaces are neither double-aot'd, nor
   ;; _not_ aot'd if already in memory.
   ;; In other words, we want to walk the forest of all the provided
   ;; namespaces and their dependency namespaces, aot-ing each
   ;; namespace we encounter in this walk exactly once. `:reload-all`
   ;; will re-aot encountered namespaces redundantly, potentially
   ;; invalidating old type references (I think). Normal `require`
   ;; will not do a deep walk over already-loaded namespaces. So
   ;; instead we rebind the *loaded-libs* var to a ref with an empty
   ;; set and call normal `require`, which gives the desired behavior.
   (let [loaded-libs' (binding [*compiler-options* (get config/config :compiler-options {})
                                *compile-path* path
                                *compile-files* true
                                clojure.core/*loaded-libs* (ref #{})]
                        (doseq [ns nss]
                          (log "compiling " ns "..")
                          (require ns))
                        @#'clojure.core/*loaded-libs*)]
     (dosync
       (alter @#'clojure.core/*loaded-libs* into @loaded-libs'))
     nil))

(defn aot
  "aot ns-syms to the given path, with all dependencies"
  [path ns-syms]
  (Arcadia.Boot/SetClojureLoadPath)
  (Arcadia.Boot/AddSourcePaths)
  (aot-namespaces path (concat ns-syms ['clojure.core
                                        'clojure.core.server
                                        'arcadia.internal.namespace
                                        'arcadia.repl]))
  (Arcadia.Boot/SetClojureLoadPathWithDLLs)
  (Arcadia.Boot/AddSourcePaths))

(defn- copy-infrastructure! [dlls-dir]
  (doseq [file (System.IO.Directory/GetFiles "ArcadiaGodot/Infrastructure")
          :when (string/ends-with? file "dll")]
    (let [file-name (last (string/split file #"/"))]
      (System.IO.File/Copy file (System.IO.Path/Combine dlls-dir file-name) true))))

(defn- compile-project! [target namespaces]
  (let [dlls-dir (System.IO.Path/Combine target "dlls")]
    (System.IO.Directory/CreateDirectory dlls-dir)
    (copy-infrastructure! dlls-dir)
    (aot dlls-dir namespaces)))

(defn- clojure-namespaces [source-paths]
  (for [src source-paths
        file (System.IO.Directory/GetFiles src "*.clj", SearchOption/AllDirectories)
        :when (not (string/includes? file "ArcadiaGodot"))]
    (-> file
        (subs (inc (count src)))
        (string/replace #"\/" ".")
        (string/replace #"\.clj$" "")
        (string/replace #"_" "-")
        (symbol))))

(defn- compile []
  (let [source-paths (get config/config :source-paths [])
        target-path  (get config/config :target-path [])]
    (compile-project! target-path (clojure-namespaces source-paths))))

(defn ready [_ _]
  (try
    (compile)
    (.Quit (arcadia.core/tree) 0)
    (catch Exception e
      (println "ERROR: Compilation failure!")
      (println e)
      (.Quit (arcadia.core/tree) 1))))
