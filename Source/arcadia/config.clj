(ns arcadia.config)

(def config 
  (merge 
    (read-string 
      (try (slurp "ArcadiaGodot/configuration.edn" :encoding "utf-8")
           (catch Exception e "{}")))
    (read-string
      (try (slurp "configuration.edn" :encoding "utf-8")
           (catch Exception e "{}")))))