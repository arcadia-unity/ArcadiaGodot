(ns arcadia.3D
  (:import 
    [Godot Node GD SceneTree Spatial Vector3 KinematicBody]))


(defn ^Vector3 translation [^Spatial o]
  (.origin (.GetGlobalTransform o)))

(defn ^Vector3 local-translation [^Spatial o]
  (.Translation o))

(defn ^Vector3 translation! [ o ^Vector3 v]
  (let [tx (.GetGlobalTransform o)]
    (set! (.origin tx) v)
    (.SetGlobalTransform o tx)))

(defn ^Vector3 local-translation! [^Spatial o ^Vector3 v]
  (.SetTranslation o v))



(defn ^Vector3 scale [^Spatial o]
  (.Scale o))

(defn ^Vector3 scale! [^Spatial o ^Vector3 v]
  (set! (.Scale o) v))

(defn ^Vector3 move-and-slide
  "Calls the `.MoveAndSlide` method on a `KinematicBody`.
   This function exists because `(.MoveAndSlide ...)` requires
   that all C# optional parameters are provided."
  [^KinematicBody o
   ^Vector3 v
   & {:keys [floor-normal
             stop-on-slope?
             max-slides
             floor-max-angle
             infinite-inertia?]
      :or {floor-normal (Vector3.)
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
