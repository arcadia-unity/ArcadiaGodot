(ns arcadia.core
  (:require
    clojure.string)
  (:import 
    [Godot Node GD ResourceLoader 
      Node Node2D SceneTree Sprite]))

(defn log [& args]
  "Log message to the Godot Editor console. Arguments are combined into a string."
  (GD/print (into-array (map #(str % " ") args))))

(defn ifn-arr [s]
  (let [xs  (re-seq #"[^ \t\n\r]+" s)
        fns
        (into-array clojure.lang.IFn
          (remove nil? 
            (map 
              (fn [s]
                (when-let [-ns (last (re-find #"#'([^/]+)\/.+" s))]
                  (try 
                    (require (symbol -ns))
                    (eval (read-string s))
                    (catch Exception e (log e)))))
              xs)))]
    fns))

(defn node-path [s] (Godot.NodePath. s))

(defn tree [node] (.GetTree node))

(defn group! 
  ([node s] (group! node s true))
  ([node s pers] (.AddToGroup node s pers)))

(defn load-scene [s]
  (let [scene (ResourceLoader/Load (str "res://" s) "PackedScene" true)]
    scene))