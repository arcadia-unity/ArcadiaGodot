(ns arcadia.3D
  (:import 
    [Godot Node GD SceneTree Spatial Vector3]))

(defn ^Vector3 translation [^Spatial o]
  (.Translation o))

(defn ^Vector3 translation! [^Spatial o ^Vector3 v]
  (.SetTranslation o v))