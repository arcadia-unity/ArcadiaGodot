(ns arcadia.internal.config
  [:import [Arcadia Util]])

(defn load-config []
  (merge
   (read-string
    (try (Util/LoadText "res://addons/ArcadiaGodot/configuration.edn")
         (catch Exception e "{}")))
   (read-string
    (try (Util/LoadText "res://configuration.edn")
         (catch Exception e "{}")))))

(def config (atom (load-config)))

(defn reload-config [] (reset! config (load-config)))

(defn get-config [] @config)

(defn get-config-key [s] (get @config (keyword s)))