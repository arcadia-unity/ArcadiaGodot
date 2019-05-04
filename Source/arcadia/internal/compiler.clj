(ns arcadia.internal.compiler
  (:use
    arcadia.core)
  (:require
    clojure.string
    [arcadia.config :as config])
  (:import
    [ArcadiaHook]))

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

(defn aot [path ns-syms]
  "aot ns-syms to the given path, with all dependencies"
  (ArcadiaHook/SetClojureLoadPath)
  (aot-namespaces path (concat ns-syms [
    'clojure.core
    'clojure.core.server]))
  (ArcadiaHook/SetClojureLoadPathWithDLLs))