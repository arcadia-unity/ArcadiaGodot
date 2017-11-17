(ns arcadia.linear
  (import 
    [Godot Vector3 Vector2 Node GD ResourceLoader 
      Node Node2D SceneTree Sprite]))

(defn v3 
  ([] (Vector3.))
  ([x y z] (Vector3. x y z)))

(defn v3* [^Vector3 v n]
  (Vector3. 
    (* (.x v) n)
    (* (.y v) n)
    (* (.z v) n)))

(defn v3+ 
  ([^Vector3 a ^Vector3 b]
    (Vector3. 
      (+ (.x a) (.x b))
      (+ (.y a) (.y b))
      (+ (.z a) (.z b))))
  ([^Vector3 a b & more]
    (reduce v3+ (v3+ a b) more)))

(defn v3- 
  ([^Vector3 a ^Vector3 b]
    (Vector3. 
      (- (.x a) (.x b))
      (- (.y a) (.y b))
      (- (.z a) (.z b))))
  ([^Vector3 a b & more]
    (reduce v3- (v3- a b) more)))

(defn v3div 
  ([^Vector3 a n]
    (Vector3. 
      (/ (.x a) n)
      (/ (.y a) n)
      (/ (.z a) n)))
  ([^Vector3 a b & more]
    (reduce v3div (v3div a b) more)))

(defn v2
  ([] (Vector2.))
  ([x y] (Vector2. x y)))

(defn v2* [^Vector2 v n]
  (Vector2. 
    (* (.x v) n)
    (* (.y v) n)))

(defn v2+ 
  ([^Vector2 a ^Vector2 b]
    (Vector2. 
      (+ (.x a) (.x b))
      (+ (.y a) (.y b))))
  ([^Vector2 a b & more]
    (reduce v2+ (v2+ a b) more)))

(defn v2- 
  ([^Vector2 a ^Vector2 b]
    (Vector2. 
      (- (.x a) (.x b))
      (- (.y a) (.y b))))
  ([^Vector2 a b & more]
    (reduce v2- (v2- a b) more)))

(defn v2div
  ([^Vector2 a n]
    (Vector2. 
      (/ (.x a) n)
      (/ (.y a) n)))
  ([^Vector2 a b & more]
    (reduce v2div (v2div a b) more)))