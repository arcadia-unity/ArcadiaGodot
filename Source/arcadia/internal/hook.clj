(ns arcadia.internal.hook
  (:require
   [clojure.edn :as edn])
  (:use arcadia.core))

(defn- arcadia-hook-instance []
  (-> "res://ArcadiaGodot/ArcadiaHook.tscn"
      (Godot.ResourceLoader/Load "PackedScene" true)
      (.Instance 0)))

(defn- get-arcadia-hook [node]
  (->> node
       (arcadia.core/children)
       (filter (fn [n] (#{"ArcadiaHook"} (.Name n))))
       (first)))

(defn equal-hooks? [arcadia-hook hooks]
  (and arcadia-hook
       (= (.-ready_fn arcadia-hook) (get hooks :hook/ready ""))
       (= (.-tree_ready_fn arcadia-hook) (get hooks :hook/tree-ready ""))
       (= (.-enter_tree_fn arcadia-hook) (get hooks :hook/enter-tree ""))
       (= (.-exit_tree_fn arcadia-hook) (get hooks :hook/exit-tree ""))
       (= (.-process_fn arcadia-hook) (get hooks :hook/process ""))
       (= (.-physics_process_fn arcadia-hook) (get hooks :hook/physics-process ""))
       (= (.-input_fn arcadia-hook) (get hooks :hook/input ""))
       (= (.-unhandled_input_fn arcadia-hook) (get hooks :hook/unhandled-input ""))))

(defn set-hooks! [node hooks]
  (set! (.-ready_fn node) (get hooks :hook/ready ""))
  (set! (.-tree_ready_fn node) (get hooks :hook/tree-ready ""))
  (set! (.-enter_tree_fn node) (get hooks :hook/enter-tree ""))
  (set! (.-exit_tree_fn node) (get hooks :hook/exit-tree ""))
  (set! (.-process_fn node) (get hooks :hook/process ""))
  (set! (.-physics_process_fn node) (get hooks :hook/physics-process ""))
  (set! (.-input_fn node) (get hooks :hook/input ""))
  (set! (.-unhandled_input_fn node) (get hooks :hook/unhandled-input "")))

(defn add-hook-script! [scene hooks]
  (when-not (Godot.ResourceLoader/Load scene "PackedScene" true)
    (throw (ex-info (str "Resource " scene " not found while trying to apply hooks") {:scene scene :hooks hooks})))
  (let [resource (Godot.ResourceLoader/Load scene "PackedScene" true)
        resource-instance (.Instance resource 0)
        arcadia-hook (arcadia-hook-instance)
        packed-scene (Godot.PackedScene.)]
    (when-not (equal-hooks? (get-arcadia-hook resource-instance) hooks)
      (when-let [arcadia-hook (get-arcadia-hook resource-instance)]
        (.RemoveChild resource-instance arcadia-hook))
      (set-hooks! arcadia-hook hooks)
      (.AddChild resource-instance arcadia-hook  false)
      (set! (.-Owner arcadia-hook) resource-instance)
      (.Pack packed-scene resource-instance)
      (Godot.ResourceSaver/Save scene packed-scene (Arcadia.Util/FLAG_RELATIVE_PATHS)))))

(def hook-keys
  #{:hook/ready
    :hook/tree-ready
    :hook/enter-tree
    :hook/exit-tree
    :hook/process
    :hook/physics-process
    :hook/input
    :hook/unhandled-input})

(defn hooks-from-file [file]
  (for [[_ v] (-> file slurp edn/read-string second ns-publics)
        [mk mv] (meta v)
        :when (hook-keys mk)]
    {:hook/key mk
     :hook/fn (str v)
     :hook/scenes mv}))

(defn hooks-per-scene [hooks]
  (->> hooks (mapv :hook/scenes) flatten set
       (mapv (fn [scene]
               {scene
                (mapv
                 (fn [hook] {(:hook/key hook)
                             (:hook/fn hook)})
                 (filter (fn [hook]
                           ((set (flatten (:hook/scenes hook))) scene)) hooks))}))
       (into {})
       (mapv (fn [[k v]] [k (apply merge v)]) )
       (into {})))

(defn reload [file]
  (doseq [[scene hooks] (hooks-per-scene (hooks-from-file file))]
    (add-hook-script! scene hooks)))
