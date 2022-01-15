;; -load fn from magic which used godot fs and used to load assemblies in standalone exports
(ns arcadia.internal.filesystem
  (:import [Arcadia Util Paths]
           [Godot Directory]))

(def config
  (merge
   (read-string (Util/LoadText Paths/ArcadiaConfig))
   (read-string (Util/LoadText Paths/ProjectConfig))))

(def utilDirectory (Directory.))

(defn file-exists? [path] (.FileExists utilDirectory path))

(def default-assembly-paths [Paths/Infrastructure])

(def load-paths (let [assembly-paths (:assembly-paths config)
                      export-folder (:export-folder config)
                      paths (concat default-assembly-paths assembly-paths [export-folder])]
                  (map #(str "res://" %) paths)
                  ))

(defn find-file
  ([filename]
   (loop [path (first load-paths)
          paths (rest load-paths)]
     (when path
       (if-let [fi (find-file path filename)]
         fi
         (recur (first paths) (rest paths))))))
  ([path filename]
   (let [probe-path (-> path
                        (System.IO.Path/Combine filename)
                        (.Replace "/" (str System.IO.Path/DirectorySeparatorChar)))]
     (when (file-exists? probe-path) probe-path))))

(defn -try-load-init-type [relative-path]
  (binding [*ns* *ns*
            *warn-on-reflection* *warn-on-reflection*
            *unchecked-math* *unchecked-math*]
    (clojure.lang.Compiler/TryLoadInitType relative-path)))

(defn -load-assembly [full-path relative-path]
  (binding [*ns* *ns*
            *warn-on-reflection* *warn-on-reflection*
            *unchecked-math* *unchecked-math*]
    (let [assy (Util/LoadAssembly full-path)]
      (clojure.lang.Compiler/InitAssembly assy relative-path))))

(defn -load
  ([^String relative-path] (-load relative-path true))
  ([^String relative-path fail-of-not-found]
   (let [
         clj-dll-name (str (.Replace relative-path "/" ".") ".clj.dll")
         cljc-dll-name (str (.Replace relative-path "/" ".") ".cljc.dll")
         ^String assy-path (or (find-file clj-dll-name)
                                           (find-file cljc-dll-name))]
     (cond
       ;; load from file system
       assy-path
       (-load-assembly assy-path relative-path)


       ;; load from init type or fail
       :else
       (or (-try-load-init-type relative-path)
           (and fail-of-not-found
                (throw (System.IO.FileNotFoundException.
                        (str "Could not locate any of "
                             [clj-dll-name cljc-dll-name]
                             (str " on load path " (vec load-paths))
                             (when (.Contains relative-path "_")
                               (str " Please check that namespaces with dashes "
                                    "use underscores in the Clojure file name.")))))))))))
