(ns arcadia.core
  (:require
    clojure.string
    [arcadia.internal.map-utils :as mu])
  (:import 
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
  Godot.Quat      10
  Godot.AABB      11
  Godot.Basis     12
  Godot.Transform 13
  Godot.Color     14
  Godot.NodePath  15
  Godot.RID       16
  Godot.Object    17
  Godot.Collections.Dictionary 18
  Godot.Collections.Array 19
  System.Byte     20
  |System.Int32[]|  21
  |System.Single[]| 22
  |System.String[]| 23
  |Godot.Vector2[]| 24
  |Godot.Vector3[]| 25
  |Godot.Color[]|   26 })

(defonce runtime-hook (RuntimeHook.))

(defn connect [^Node node ^String signal-name f]
  (.Register runtime-hook (hash f) f)
  (.Connect node signal-name runtime-hook "CatchMethod" 
    (Godot.Collections.Array. (into-array Object [(hash f)])) 0))

(defn add-signal 
  "Adds a user signal to the object, which can then be emitted with `emit`"
  ([^Godot.Object o ^String name]
    (RuntimeHook/AddSignal o name))
  ([^Godot.Object o ^String name & args]
    (let [variants (zipmap (remove nil? (map variants-enum args)) "abcdefghijklmnopqrstuvwxyz")
          arguments (map 
                      (fn [[i n]] 
                        (let [dict (Godot.Collections.Dictionary.)]
                          (.Add dict "name" (str n))
                          (.Add dict "type" i) dict)) 
                      variants)]
      (RuntimeHook/AddSignal o name (Godot.Collections.Array. (into-array Object arguments))))))

(defn emit
  ([^Godot.Object o ^String name]
    (RuntimeHook/Emit o name))
  ([^Godot.Object o ^String name & args]
    (RuntimeHook/Emit o name (Godot.Collections.Array. (into-array Object args)))))



'(connect (find-node "Button") "pressed" (fn [] (log "BUTTON WAS PRESSED")))
'(connect (find-node "Button") "button_down" (fn [] (log "BUTTON DOWN")))

'(connect (find-node "Button2") "pressed" (fn [] (throw (Exception. "hmm"))))


'(add-signal (find-node "Button") "foo")

'(connect (find-node "Button") "foo" (fn [] (log "FOO SIG")))
; this doesn't match 0 arity, but i can't pass nil here?
'(emit (find-node "Button") "foo")


'(add-signal (find-node "Button") "bark" System.String)
'(connect (find-node "Button") "bark" (fn [a] (log "bark SIG" [a])))
'(emit (find-node "Button") "bark" "Woof" (Godot.Vector3.))







'(for [i (range 1 12)
      :let [args (take i "abcdefghijklmnop")
            typed (apply str (map #(str "Object " % ", ") args))
            invoked (apply str (interpose ", " args ))]]
  (print 
    (str "    public void CatchMethod(" typed "int hash){\n")
    "       try\n"
    "       {\n"
    (str "           functions[hash].invoke(" invoked ");\n")
    "       }\n"
    "       catch (System.Exception err)\n"
    "       {\n"
    "           GD.PrintErr(err);\n"
    "       }\n"
    "   }\n\n"))















;; ============================================================
;; defcomponent 
;; ============================================================

(defmacro defleaked [var]
  `(def ~(with-meta (symbol (name var)) {:private true})
     (var-get (find-var '~var))))

(defleaked clojure.core/validate-fields)
(defleaked clojure.core/parse-opts+specs)
(defleaked clojure.core/build-positional-factory)

(defn- emit-defclass* 
  "Do not use this directly - use defcomponent"
  [tagname name extends assem fields interfaces methods]
  (assert (and (symbol? extends) (symbol? assem)))
  (let [classname (with-meta
                    (symbol
                      (str (namespace-munge *ns*) "." name))
                    (meta name))
        interfaces (conj interfaces 'clojure.lang.IType)]
    `(defclass*
       ~tagname ~classname
       ~extends ~assem
       ~fields 
       :implements ~interfaces 
       ~@methods)))

(defmacro defcomponent*
  [name fields & opts+specs]
  (validate-fields fields name)
  (let [gname name 
        [interfaces methods opts] (parse-opts+specs opts+specs)
        ns-part (namespace-munge *ns*)
        classname (symbol (str ns-part "." gname))
        hinted-fields fields
        fields (vec (map #(with-meta % nil) fields))
        [field-args over] (split-at 20  fields)]
    `(let []
       ~(emit-defclass*
          name
          gname
          'Godot.Node
          'Godot
          (vec hinted-fields)
          (vec interfaces)
          methods)
       (import ~classname)
       ~(build-positional-factory gname classname fields)
       ~classname)))

;TODO error is to long maybe?
(defn- normalize-method-implementations [mimpls]
  (for [[[protocol] impls] (partition 2
                             (partition-by symbol? mimpls))
        [name args & fntail] impls]
    (mu/lit-map protocol name args fntail)))

(defn- find-message-protocol-symbol [s]
  (symbol (str "arcadia.messages/I" s)))

(defn- awake-method? [{:keys [name]}]
  (= name 'Awake))

(defn- normalize-message-implementations [msgimpls]
  (for [[name args & fntail] msgimpls
        :let [protocol (find-message-protocol-symbol name)]]
    (mu/lit-map protocol name args fntail)))

(defn- process-method [{:keys [protocol name args fntail]}]
  [protocol `(~name ~args ~@fntail)])

(defn- process-awake-method [impl]
  (process-method
    (update-in impl [:fntail]
      #(cons `(require (quote ~(ns-name *ns*))) %))))

(defn ^:private ensure-has-awake [mimpls]
  (if (some awake-method? mimpls)
    mimpls
    (cons {:protocol (find-message-protocol-symbol 'Awake)
           :name     'Awake
           :args     '[this]
           :fntail   nil}
      mimpls)))

(defn- process-defcomponent-method-implementations [mimpls]
  (let [[msgimpls impls] ((juxt take-while drop-while)
                          (complement symbol?)
                          mimpls)
        nrmls            (ensure-has-awake
                           (concat
                             (normalize-message-implementations msgimpls)
                             (normalize-method-implementations impls)))]
    (apply concat
      (for [impl nrmls]
        (if (awake-method? impl)
          (process-awake-method impl)
          (process-method impl))))))

(defmacro defcomponent
  "Defines a new component."
  [name fields & method-impls] 
  (let [fields2 (mapv #(vary-meta % assoc :unsynchronized-mutable true) fields) ;make all fields mutable
        method-impls2 (process-defcomponent-method-implementations method-impls)]
    `(defcomponent* ~name ~fields2 ~@method-impls2)))


