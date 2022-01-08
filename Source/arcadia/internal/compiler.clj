(ns arcadia.internal.compiler
  (:require
   clojure.string
   [arcadia.core :as arc]
   [arcadia.internal.config :as config])
  (:import
   [Arcadia Util]
   [System.IO Path]))

(defn prepare-export [compile-path]
  (let [nss (config/get-config-key :export-namespaces)]
    (arc/log "prepare export to directory: " compile-path)
    (binding [*compile-path* compile-path
              clojure.core/*loaded-libs* (ref (sorted-set))]
      (doall (for [ns nss]
               (let [file-res-path (. Path Combine "res://" compile-path (str ns ".clj.dll") )]
                 (Util/RemoveFile file-res-path)
                 (arc/log "compiling " ns)
                 (compile ns)))))))