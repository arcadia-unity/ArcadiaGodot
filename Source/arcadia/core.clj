(ns arcadia.core
  (:require
    clojure.string)
  (:import 
    Helper
    [Godot Node GD ResourceLoader 
      Node Node2D SceneTree Sprite Spatial]))

(defn log [& args]
  "Log message to the Godot Editor console. Arguments are combined into a string."
  (GD/Print (into-array (map #(str % " ") args))))

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

(defn get-node 
  "Uses the global scene viewport Node, \"/root/etc\""
  [s]
  (.GetNode (.Root (Godot.Engine/GetMainLoop)) (node-path s)))

(defn find-node 
  "Recursive find from global root, s is a wildcard string supporting * and ?"
  [s]
  (.FindNode (.Root (Godot.Engine/GetMainLoop)) s true false))

(defn instance [pscn]
  ;not so optional second arg, PackedScene.GenEditState.Disabled = 0
  (.Instance pscn 0))

(defn add-child [^Node node ^Node child]
  (.AddChild node child true))

(defn remove-child [^Node node ^Node child]
  (.RemoveChild node child))

(defn parent [^Node node]
  (.GetParent node (type-args Node)))

(defn children [^Node node]
  (.GetChildren node))

(defn destroy [^Node node]
  (.QueueFree node))

(defn destroy-immediate [^Node node]
  (.Free node))

(defn is-qeued-for-deletion? [^Node node]
  (.IsQueuedForDeletion node))



; this works and is interesting as maybe how dynamic hooks work?
'(.Connect (find-node "Button") "pressed" (Godot.Object.) "flarf" nil 0) 


;TODO this code will crash godot session
;figure out how to capture these kinds of exceptions 
'(let [o (instance (load-scene "boid.tscn"))]
  (destroy-immediate o)
  (is-qeued-for-deletion? o) o)

;as oposed to these which are fine
'(throw (Exception. "foo"))

'(defn foo [] "bar")

'(gen-class
 :name "Bird"
 ;:implements [clojure.examples.IBar]
 ;:prefix "impl-"
 :methods [[foo [] String]])

; No such var: clojure.core/gen-interface
'(defprotocol Fly
  "a protocol for flying"
  (fly [this] "Method to fly"))