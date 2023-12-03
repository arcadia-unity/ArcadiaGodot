(ns arcadia.core
  (:require
    clojure.string)
  (:import 
    [Arcadia ArcadiaHook]
    [Godot GD ResourceLoader 
      Node Node2D]))

(defn log
  "Log message to the Godot Editor console. Arguments are combined into a string."
  [& args]
  (GD/Print (into-array (map #(str % " ") args))))

(defn obj 
  "Will return nil if the object instance is not valid."
  [^Godot.GodotObject o]
  (if (Godot.GodotObject/IsInstanceValid o) o nil))

(defn node-path [s] (Godot.NodePath. s))

(defn ^Node root
  "returns the root node"
  []
  (.Root (Godot.Engine/GetMainLoop)))

(defn tree
  "Return the SceneTree of `node`. If `node` isn't provided return the
  SceneTree of the root node."
  ([] (tree (root)))
  ([node] (.GetTree node)))

(defn call-group!
  "Call `method` on each member of the given `group` in
  `scene-tree`. `args` will be passed as arguments to the
  `method`. `scene-tree` defaults to the root SceneTree if not
  provieded."
  ([group method args]
   (call-group! (tree) group method args))
  ([scene-tree group method args]
   (.CallGroup scene-tree group method (to-array args))))

(defn group!
  ([node ^Godot.StringName s] (group! node s true))
  ([node ^Godot.StringName s pers] (.AddToGroup node s pers)))

(defn in-group? [^Godot.Node node ^Godot.StringName group]
  (.IsInGroup node group))

(defn objects-in-group [^Godot.StringName group]
  (.GetNodesInGroup (Godot.Engine/GetMainLoop) group))

(defn change-scene ;FIXME
  "Changes the root scene to the one at the given path"
  [s]
  (.ChangeSceneToFile (Godot.Engine/GetMainLoop) (str "res://" s)))

(defn load-scene [s] ;FIXME
  (let [scene (ResourceLoader/Load (str "res://" s) "PackedScene" true)]
    scene))

(defn get-node 
  "Gets child of a node by path, or uses the global scene viewport Node if only 1 argument is given, \"/root/etc\""
  ([s]
   (get-node (root) s))
  ([n s]
    (.GetNode n (node-path s))))

(defn find-node 
  "Recursive find a descendant node , s is a name string supporting * and ? wildcards. Uses the global scene viewport Node if only 1 argument is given"
  ([s]
    (find-node (root) s))
  ([n s]
    (.FindNode n s true false)))

(defn instance [pscn]
  "Instance a PackedScene"
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


(def alphabet ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"])

(def variants-enum {
  nil             0
  System.Boolean  1
  System.Int32    2
  System.Single   3
  System.String   4
  Godot.Vector2   5
  Godot.Rect2     6
  Godot.Vector3   7
  Godot.Transform2D 8
  Godot.Plane     9
  Godot.Quaternion 10
  ;Godot.AABB      11
  Godot.Basis     12
  Godot.Transform3D 13
  Godot.Color     14
  Godot.NodePath  15
  ;Godot.RID       16
  Godot.GodotObject    17
  Godot.Collections.Dictionary 18
  Godot.Collections.Array 19
  System.Byte     20
  |System.Int32[]|  21
  |System.Single[]| 22
  |System.String[]| 23
  |Godot.Vector2[]| 24
  |Godot.Vector3[]| 25
  |Godot.Color[]|   26 })

(defonce ^:private adhoc-signals (AdhocSignals.))

(defn ^:private _connect [^Node node ^String signal-name ^Godot.GodotObject o f]
  (let [guid (str (System.Guid/NewGuid))]
    (.Register o guid f)
    (.Connect node signal-name o "CatchMethod" 
      (Godot.Collections.Array. (into-array Object [guid])) 0)))

(defn connect
  "Connects a node's signal to a function. These connections share a Godot.GodotObject 
   instance and only one connection can be made for each node's signal."
  [^Node node ^String signal-name f]
  (_connect node signal-name adhoc-signals f))

(defn connect*
  "Like `connect` but uses a unique Godot.GodotObject for multiple connections to one 
   signal. Returns the object if you need to `disconnect` or `destroy` it later."
  [^Node node ^String signal-name f]
  (let [o (AdhocSignals.)]
    (_connect node signal-name o f) o))

(defn connected? 
  "Check if a signal is connected. If checking a `connect*` instance provide it 
   as the third argument."
  ([^Node node ^String signal-name]
    (connected? node signal-name adhoc-signals))
  ([^Node node ^String signal-name ^Godot.GodotObject o]
    (.IsConnected node signal-name o "CatchMethod")))

(defn disconnect
  "Disconnect a signal. If disconnecting a `connect*` instance provide it as 
   the third argument."
  ([^Node node ^String signal-name]
    (disconnect node signal-name adhoc-signals))
  ([^Node node ^String signal-name ^Godot.GodotObject o]
    (.Disconnect node signal-name o "CatchMethod")))

(defn add-signal 
  "Adds a user signal to the object, which can then be emitted with `emit`. args 
   should be type literals matching those listed in the `Godot.Variant.Type` enum"
  ([^Godot.GodotObject o ^String name]
    (AdhocSignals/AddSignal o name))
  ([^Godot.GodotObject o ^String name & args]
    (let [variants (remove nil? (map (fn [t] (get variants-enum t 17)) args))
          names    (take (count variants) alphabet)]
      (AdhocSignals/AddSignal o name (into-array System.String names) (into-array System.Int32 variants)))))

(defn emit
  "Emit's a node's signal."
  ([^Godot.GodotObject o ^String name]
    (AdhocSignals/Emit o name))
  ([^Godot.GodotObject o ^String name a]
    (AdhocSignals/Emit o name a)))


(def hook-types {
  :enter-tree "_enter_tree"
  :exit-tree "_exit_tree"
  :ready  "_ready"
  :tree-ready "_tree_ready"
  :process "_process"
  :physics-process "_physics_process"
  :input "_input"
  :unhandled-input "_unhandled_input"})

;TODO make this a C# helper, it'll be used a lot for state fns
(defn ^:private ensure-hook [^Node node]
  (let [node (obj node)]
    (or (Arcadia.Util/GetHook node)
      (let [o (Arcadia.ArcadiaHook.)]
        (.AddChild node o true) o))))

(defn ^:private keystr [x]
  (cond (keyword? x) (name x) (string? x) x :else (str x)))

(defn hook+ 
  "Attach a Clojure function, preferrably a Var instance, to Node
  `node` on key `k`. The function `f` will be invoked every time the event
  identified by `event-kw` is triggered by Godot.

  `f` must be a function of 2 arguments, plus however many arguments
  the corresponding Godot override method takes. The first argument is
  the Node `node` that `f` is attached to. The second argument is
  the key `k` it was attached with. The remaining arguments are the
  arguments normally passed to the corresponding Godot method."
  [^Node node event-kw ^clojure.lang.Keyword k f]
  (let [node (obj node)
        h (ensure-hook node)]
    (.Add h (get hook-types event-kw (str event-kw)) (keystr k) f) node))

(defn hook- 
  "Removes hook function from Node `node` on the hook type `event-kw` at `key`, if it exists."
  [^Node node event-kw ^clojure.lang.Keyword k]
  (let [node (obj node)
        h (ensure-hook node)]
    (.Remove h (get hook-types event-kw (str event-kw)) (keystr k)) node))

(defn clear-hooks
  "Removes all hook functions on all hook types"
  [^Node node]
  (let [node (obj node)
        h (ensure-hook node)]
    (.RemoveAll h) node))


(defn state 
  "Retrieves state stored on an object's ArcadiaHook, second arity retrieves by key k"
  ([^Node node k] (get (state node) k))
  ([^Node node]
    (if-let [^ArcadiaHook hook (Arcadia.Util/GetHook node)] 
      (.state hook))))

(defn update-state
  "Updates state stored on an object's ArcadiaHook"
  ([^Node node k ^clojure.lang.IFn f]
    (update-state node (fn [m] (update m k f))))
  ([^Node node ^clojure.lang.IFn f]
    (let [hook (ensure-hook node)]
      (set! (.state hook) (f (.state hook))))))

(defn set-state 
  "Sets state stored on an object's ArcadiaHook"
  ([^Node node k ^System.Object value]
    (update-state node (fn [m] (assoc m k value))))
  ([^Node node ^System.Object value]
    (set! (.state (ensure-hook node)) value)))

(defn timeout 
  "invoke fn `f` after `n` seconds"
  [^System.Double n ^clojure.lang.IFn f]
  (connect (.CreateTimer (Godot.Engine/GetMainLoop) n true false false) "timeout" f))

(def tween-ease-enum {
  :in     0
  :out    1
  :in-out 2
  :out-in 3})

(def tween-transition-enum {
  :linear  0
  :sine    1
  :quint   2
  :quart   3
  :quad    4
  :expo    5
  :elastic 6
  :cubic   7
  :circ    8
  :bounce  9
  :back    10})

(defn tween
  "Creates a Tween node, mounts it to the root node, runs the tween, then destroys the node. 
  `property` is a snake_case string of the property coordinates, you can hover over properties in the inspector to see their path
  Can take an option map of: 
    `:transition` (`:linear :sine :quint :quart :quad :expo :elastic :cubic :circ :bounce :back`)
    `:easing` (`:in :out :in-out :out-in`)
    `:delay` (float seconds)
    `:callback` (fn to call when tween completes)

  example:
  ```
  (tween (find-node \"New\") \"rect_rotation\" 0 90 2 {:transition :elastic :callback (fn [] (log \"callback\"))})
  ```
    "
  ([object property initialVal finalVal duration] (tween object property initialVal finalVal duration {}))
  ([object property initialVal finalVal duration {:keys [transition easing delay callback]}]
    (let [^System.Object t (Godot.Tween.)
          adhoc (AdhocSignals.)]
      (add-child (root) t)
      (.InterpolateProperty t object (Godot.NodePath. property) initialVal finalVal duration 
        (get tween-transition-enum transition 0)
        (get tween-ease-enum easing 2) (or delay 0))
      (_connect t "tween_all_completed" adhoc (fn [] (if callback (callback)) (destroy t) (destroy adhoc)))
      (.Start t) t)))


(defn play-sound
  "Convenience fn to play an audio file, `(play-sound \"music/song1.ogg\")`"
  ([s] (play-sound s 0))
  ([s n]
    (let [audio (Godot.AudioStreamPlayer.)]
      (add-child (root) audio)
      (set! (.VolumeDb audio) (float n))
      (set! (.Stream audio) (load-scene s))
      (.Play audio 0)
      (connect* audio "finished" (fn [] (destroy audio))))))
