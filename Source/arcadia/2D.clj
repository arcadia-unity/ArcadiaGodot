(ns arcadia.2D
  (:import 
    [Godot Node GD Node2D Vector2]))

(defn ^Vector2 position [^Node2D node]
  (.Position node))

(defn ^Vector2 position! [^Node2D node ^Vector2 v]
  (.SetPosition node v))