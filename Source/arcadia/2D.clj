(ns arcadia.2D
  (:import 
    [Godot Node GD Node2D Vector2 KinematicBody2D]))

(defn ^Vector2 position [^Node2D node]
  (.Position node))

(defn ^Vector2 position! [^Node2D node ^Vector2 v]
  (.SetPosition node v))

(defn ^Vector2 move-and-slide
  [^KinematicBody2D o
   ^Vector2 v
   & {:keys [floor-normal
             stop-on-slope?
             max-slides
             floor-max-angle
             infinite-inertia?]
      :or {floor-normal (v2)
           stop-on-slope? false
           max-slides 4
           floor-max-angle 0.785398
           infinite-inertia? true}}]
  (.MoveAndSlide o
                 v
                 floor-normal
                 stop-on-slope?
                 max-slides
                 floor-max-angle
                 infinite-inertia?))
