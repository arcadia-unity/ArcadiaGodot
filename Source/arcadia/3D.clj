(ns arcadia.3D
  (:import 
    [Godot Node GD SceneTree Spatial Vector3 KinematicBody]))

(defn ^Vector3 translation [^Spatial o]
  (.Translation o))

(defn ^Vector3 translation! [^Spatial o ^Vector3 v]
  (.SetTranslation o v))

(defn ^Vector3 move-and-slide
  [^KinematicBody o
   ^Vector3 v
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
