(ns arcadia.internal.config
  [:import [Arcadia Util Paths]])

(defn load-config []
  (merge
   (read-string (Util/LoadText Paths/ArcadiaConfig))
   (read-string (Util/LoadText Paths/ProjectConfig))))

(def config (atom (load-config)))

(defn reload-config [] (reset! config (load-config)))

(defn get-config [] @config)

(defn get-config-key [s] (get @config (keyword s)))