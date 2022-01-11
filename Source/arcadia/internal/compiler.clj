(ns arcadia.internal.compiler
  (:require
   clojure.string
   [arcadia.core :as arc]
   [arcadia.internal.config :as config])
  (:import
   [Arcadia Util Paths]
   [System.IO Path]))

(def arcadia-nss 
  ['arcadia.internal.namespace 'arcadia.internal.config 'arcadia.repl 'arcadia.internal.nrepl-support])

(defn prepare-export []
  (let [export-nss (config/get-config-key :export-namespaces)
        nss (concat arcadia-nss export-nss)
        compile-path (config/get-config-key :export-folder)]
    (arc/log "Prepare export in directory: " compile-path)
    (binding [*compile-path* compile-path
              clojure.core/*loaded-libs* (ref (sorted-set))]
      (doall (for [ns nss]
               (let [file-res-path (. Path Combine "res://" compile-path (str ns ".clj.dll") )]
                 (Util/RemoveFile file-res-path)
                 (arc/log "compiling " ns)
                 (compile ns)))))))

(defn recompile-filesystem []
  (let [compile-path Paths/Infrastructure
        file-res-path (. Path Combine "res://" compile-path (str "arcadia.internal.filesystem.clj.dll"))]
    (arc/log "Recompile arcadia.internal.filesystem in directory: " Paths/Infrastructure)
    (binding [*compile-path* compile-path
              clojure.core/*loaded-libs* (ref (sorted-set))]
      (Util/RemoveFile file-res-path)
      (arc/log "compiling arcadia.internal.filesystem")
      (compile 'arcadia.internal.filesystem))))