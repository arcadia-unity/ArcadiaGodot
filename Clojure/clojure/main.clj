;; Copyright (c) Rich Hickey All rights reserved. The use and
;; distribution terms for this software are covered by the Eclipse Public
;; License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can be found
;; in the file epl-v10.html at the root of this distribution. By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from this
;; software.

;; Originally contributed by Stephen C. Gilardi

(ns ^{:doc "Top-level main function for Clojure REPL and scripts."
       :author "Stephen C. Gilardi and Rich Hickey"}
  clojure.main
  (:refer-clojure :exclude [with-bindings])
  (:require [clojure.spec.alpha :as spec])
  (:import (System.IO StringReader FileInfo FileStream Path StreamWriter)                ;;; java.io StringReader BufferedWriter  FileWriter
                                                                                               ;;; (java.nio.file Files)
                                                                                               ;;; (java.nio.file.attribute FileAttribute)                                                        
           (clojure.lang Compiler Compiler+CompilerException                                   ;;; Compiler$CompilerException
                         LineNumberingTextReader RT  LispReader+ReaderException))              ;;; LineNumberingPushbackReader LispReader$ReaderException
  ;;(:use [clojure.repl :only (demunge root-cause stack-element-str get-stack-trace)])
  )

(declare main)
 
;;;;;;;;;;;;;;;;;;; redundantly copied from clojure.repl to avoid dep ;;;;;;;;;;;;;;

(defn demunge
  "Given a string representation of a fn class,
  as in a stack trace element, returns a readable version."
  {:added "1.3"}
  [fn-name]
  (clojure.lang.Compiler/demunge fn-name))

(defn root-cause
  "Returns the initial cause of an exception or error by peeling off all of
  its wrappers"
  {:added "1.3"}
  [ ^Exception t]                     ;;; ^Throwable
  (loop [cause t]
    (if (and (instance? clojure.lang.Compiler+CompilerException cause)
	         (not= (.Source ^clojure.lang.Compiler+CompilerException cause) "NO_SOURCE_FILE"))  ;;; .source
      cause
	  (if-let [cause (.InnerException cause)]    ;;; .getCause
        (recur cause)
        cause))))

;;;;;;;;;;;;;;;;;;; end of redundantly copied from clojure.repl to avoid dep ;;;;;;;;;;;;;;

(def ^:private core-namespaces
  #{"clojure.core" "clojure.core.reducers" "clojure.core.protocols" "clojure.data" "clojure.datafy"
    "clojure.edn" "clojure.instant" "clojure.java.io" "clojure.main" "clojure.pprint" "clojure.reflect"
    "clojure.repl" "clojure.set" "clojure.spec.alpha" "clojure.spec.gen.alpha" "clojure.spec.test.alpha"
    "clojure.string" "clojure.template" "clojure.uuid" "clojure.walk" "clojure.xml" "clojure.zip"})

(defn- core-class?
  [^String class-name]
  (and (not (nil? class-name))
       (or (.StartsWith class-name "clojure.lang.")                                            ;;; .startsWith
           (contains? core-namespaces (second (re-find #"^([^$]+)\$" class-name))))))

;;;  Added -DM

(defn get-stack-trace 
  "Gets the stack trace for an Exception"
  [^Exception e]
  (.GetFrames (System.Diagnostics.StackTrace. e true)))

(defn stack-element-classname
  [^System.Diagnostics.StackFrame el]
  (if-let [t (some-> el (.GetMethod) (.ReflectedType))]
    (.FullName t)
    "NO_CLASS"))

(defn stack-element-methodname
  [^System.Diagnostics.StackFrame el]
  (or (some-> el (.GetMethod) (.Name))
      "NO_METHOD"))

;;;


(defn stack-element-str
  "Returns a (possibly unmunged) string representation of a StackTraceElement"
  {:added "1.3"}
  [^System.Diagnostics.StackFrame el]                                                          ;;; StackTraceElement
  (let [file (.GetFileName el)                                                                 ;;; getFileName
        clojure-fn? (and file (or (.EndsWith file ".clj")                                      ;;; endsWith
                                  (.EndsWith file ".cljc") (.EndsWith file ".cljr")            ;;; endsWith + DM: Added cljr
                                  (= file "NO_SOURCE_FILE")))]
    (str (if clojure-fn?
           (demunge (stack-element-classname el))                              ;;; (.getClassName el))
           (str (stack-element-classname el) "." (stack-element-methodname el)))   ;;; (.getClassName el)  (.getMethodName el)
         " (" (.GetFileName el) ":" (.GetFileLineNumber el) ")")))        ;;; getFileName  getLineNumber
;;;;;;;;;;;;;;;;;;; end of redundantly copied from clojure.repl to avoid dep ;;;;;;;;;;;;;;


(defmacro with-bindings
  "Executes body in the context of thread-local bindings for several vars
  that often need to be set!: *ns* *warn-on-reflection* *math-context* 
  *print-meta* *print-length* *print-level* *compile-path* 
  *command-line-args* *1 *2 *3 *e"
  [& body]
  `(binding [*ns* *ns*
             *warn-on-reflection* *warn-on-reflection*
             *math-context* *math-context*
             *print-meta* *print-meta*
             *print-length* *print-length*
             *print-level* *print-level*
             *print-namespace-maps* true
			 *data-readers* *data-readers*
			 *default-data-reader-fn* *default-data-reader-fn*
             *compile-path* (or (Environment/GetEnvironmentVariable "CLOJURE_COMPILE_PATH") ".")  ;;;(System/getProperty "clojure.compile.path" "classes")
             *command-line-args* *command-line-args*
			 *unchecked-math* *unchecked-math*
             *assert* *assert*
             clojure.spec.alpha/*explain-out* clojure.spec.alpha/*explain-out*
			 *1 nil
             *2 nil
             *3 nil
             *e nil]
     ~@body))

(defn repl-prompt
  "Default :prompt hook for repl"
  []
  (print (str (ns-name *ns*) "=> ")))    ;;;  until we get printf defined for real:  (printf "%s=> " (ns-name *ns*)))

(defn skip-if-eol
  "If the next character on stream s is a newline, skips it, otherwise
  leaves the stream untouched. Returns :line-start, :stream-end, or :body
  to indicate the relative location of the next character on s. The stream
  must either be an instance of LineNumberingPushbackReader or duplicate
  its behavior of both supporting .unread and collapsing all of CR, LF, and
  CRLF to a single \\newline."
  [s]
  (let [c (.Read s)]                             ;;; .read
    (cond
     (= c (int \newline)) :line-start
     (= c -1) :stream-end
     :else (do (.Unread s c) :body))))           ;;; .unread

(defn skip-whitespace
  "Skips whitespace characters on stream s. Returns :line-start, :stream-end,
  or :body to indicate the relative location of the next character on s.
  Interprets comma as whitespace and semicolon as comment to end of line.
  Does not interpret #! as comment to end of line because only one
  character of lookahead is available. The stream must either be an
  instance of LineNumberingPushbackReader or duplicate its behavior of both
  supporting .unread and collapsing all of CR, LF, and CRLF to a single
  \\newline."
  [s]
  (loop [c (.Read s)]							;;; .read
    (cond
     (= c (int \newline)) :line-start
     (= c -1) :stream-end
     (= c (int \;)) (do (.ReadLine s) :line-start)                             ;;; .readLine
     (or (Char/IsWhiteSpace (char c)) (= c (int \,))) (recur (.Read s))        ;;; (Character/isWhitespace c)    .read
     :else (do (.Unread s c) :body))))                                         ;;; .unread

(defn renumbering-read
  "Reads from reader, which must be a LineNumberingPushbackReader, while capturing
  the read string. If the read is successful, reset the line number and re-read.
  The line number on re-read is the passed line-number unless :line or
  :clojure.core/eval-file meta are explicitly set on the read value."
  {:added "1.10"}
  ([opts ^LineNumberingTextReader reader line-number]                                                             ;;; LineNumberingPushbackReader
   (let [pre-line (.LineNumber reader)                                                                            ;;; .getLineNumber
         [pre-read s] (read+string opts reader)
         {:keys [clojure.core/eval-file line]} (meta pre-read)
         re-reader (doto (LineNumberingTextReader. (StringReader. s))                                            ;;; LineNumberingPushbackReader.
                     (.set_LineNumber (if (and line (or eval-file (not= pre-line line))) line line-number)))]    ;;; .setLineNumber
     (read opts re-reader))))

(defn repl-read
  "Default :read hook for repl. Reads from *in* which must either be an
  instance of LineNumberingPushbackReader or duplicate its behavior of both
  supporting .unread and collapsing all of CR, LF, and CRLF into a single
  \\newline. repl-read:
    - skips whitespace, then
      - returns request-prompt on start of line, or
      - returns request-exit on end of stream, or
      - reads an object from the input stream, then
        - skips the next input character if it's end of line, then
        - returns the object."
  [request-prompt request-exit]
  (or ({:line-start request-prompt :stream-end request-exit}
       (skip-whitespace *in*))
      (let [input (renumbering-read {:read-cond :allow} *in* 1)]
        (skip-if-eol *in*)
        input)))

(defn repl-exception
  "Returns the root cause of throwables"
  [throwable]
  (root-cause throwable))

(defn- file-name
  "Helper to get just the file name part of a path or nil"
  [^String full-path]
  (when full-path
    (try
      (.Name (System.IO.FileInfo. full-path))                                              ;;; .getName java.io.File.
      (catch Exception t))))                                                               ;;; Throwable

(defn- file-path                                                                           ;;; probably not exactly equivalante to Java version, not similar notion of relative/absolute.
  "Helper to get the relative path to the source file or nil"
  [^String full-path]
  (when full-path
    (try
      (let [path (.DirectoryName (System.IO.FileInfo. full-path))                          ;;; .getPath   java.io.File.
            cd-path (str (.DirectoryName (System.IO.FileInfo. "")) "\\")]                  ;;; .getAbsolutePath   java.io.File.  "/"
        (if (.StartsWith path cd-path)                                                     ;;; .startsWith
          (subs path (count cd-path))
          path))
      (catch Exception t                                                                   ;;; Throwable
        full-path))))

(defn- java-loc->source
  "Convert Java class name and method symbol to source symbol, either a
  Clojure function or Java class and method."
  [clazz method]
  (if (#{'invoke 'invokeStatic} method)
    (let [degen #(.Replace #"--.*$" ^String %  "")                                                                    ;;; #(.replaceAll ^String % "--.*$" "") 
          [ns-name fn-name & nested] (->> (str clazz) (.Split #"\$") (map demunge) (map degen))]                    ;;; .split
      (symbol ns-name (String/Join "$" ^|System.String[]| (into-array String (cons fn-name nested)))))                ;;; String/join   ^"[Ljava.lang.String;"
    (symbol (name clazz) (name method))))

(defn ex-triage
  "Returns an analysis of the phase, error, cause, and location of an error that occurred
  based on Throwable data, as returned by Throwable->map. All attributes other than phase
  are optional:
    :clojure.error/phase - keyword phase indicator, one of:
      :read-source :compile-syntax-check :compilation :macro-syntax-check :macroexpansion
      :execution :read-eval-result :print-eval-result
    :clojure.error/source - file name (no path)
    :clojure.error/path - source path
    :clojure.error/line - integer line number
    :clojure.error/column - integer column number
    :clojure.error/symbol - symbol being expanded/compiled/invoked
    :clojure.error/class - cause exception class symbol
    :clojure.error/cause - cause exception message
    :clojure.error/spec - explain-data for spec error"
  {:added "1.10"}
  [datafied-throwable]
  (let [{:keys [via trace phase] :or {phase :execution}} datafied-throwable
        {:keys [type message data]} (last via)
        {:clojure.spec.alpha/keys [problems fn], :clojure.spec.test.alpha/keys [caller]} data
        {:clojure.error/keys [source] :as top-data} (:data (first via))]
    (assoc
      (case phase
        :read-source
        (let [{:clojure.error/keys [line column]} data]
          (cond-> (merge (-> via second :data) top-data)
            source (assoc :clojure.error/source (file-name source)
                          :clojure.error/path (file-path source))
            (#{"NO_SOURCE_FILE" "NO_SOURCE_PATH"} source) (dissoc :clojure.error/source :clojure.error/path)
            message (assoc :clojure.error/cause message)))

        (:compile-syntax-check :compilation :macro-syntax-check :macroexpansion)
        (cond-> top-data
          source (assoc :clojure.error/source (file-name source)
                        :clojure.error/path (file-path source))
          (#{"NO_SOURCE_FILE" "NO_SOURCE_PATH"} source) (dissoc :clojure.error/source :clojure.error/path)
          type (assoc :clojure.error/class type)
          message (assoc :clojure.error/cause message)
          problems (assoc :clojure.error/spec data))

        (:read-eval-result :print-eval-result)
        (let [[source method file line] (-> trace first)]
          (cond-> top-data
            line (assoc :clojure.error/line line)
            file (assoc :clojure.error/source file)
            (and source method) (assoc :clojure.error/symbol (java-loc->source source method))
            type (assoc :clojure.error/class type)
            message (assoc :clojure.error/cause message)))

        :execution
        (let [[source method file line] (->> trace (drop-while #(core-class? (name (first %)))) first)
              file (first (remove #(or (nil? %) (#{"NO_SOURCE_FILE" "NO_SOURCE_PATH"} %)) [(:file caller) file]))
              err-line (or (:line caller) line)]
          (cond-> {:clojure.error/class type}
            err-line (assoc :clojure.error/line err-line)
            message (assoc :clojure.error/cause message)
            (or fn (and source method)) (assoc :clojure.error/symbol (or fn (java-loc->source source method)))
            file (assoc :clojure.error/source file)
            problems (assoc :clojure.error/spec data))))
      :clojure.error/phase phase)))

(defn ex-str
  "Returns a string from exception data, as produced by ex-triage.
  The first line summarizes the exception phase and location.
  The subsequent lines describe the cause."
  {:added "1.10"}
  [{:clojure.error/keys [phase source path line column symbol class cause spec]
    :as triage-data}]
  (let [loc (str (or path source "REPL") ":" (or line 1) (if column (str ":" column) ""))
        class-name (name (or class ""))
        simple-class (if class (or (first (re-find #"([^.])+$" class-name)) class-name))          ;;;  #"([^.])++$"
        cause-type (if (contains? #{"Exception" "RuntimeException"} simple-class)
                     "" ;; omit, not useful
                     (str " (" simple-class ")"))]
    (case phase
      :read-source
      (format "Syntax error reading source at (%s).%n%s%n" loc cause)

      :macro-syntax-check
      (format "Syntax error macroexpanding %sat (%s).%n%s"
              (if symbol (str symbol " ") "")
              loc
              (if spec
                (with-out-str
                  (spec/explain-out
                    (if (= spec/*explain-out* spec/explain-printer)
                      (update spec :clojure.spec.alpha/problems
                              (fn [probs] (map #(dissoc % :in) probs)))
                      spec)))
                (format "%s%n" cause)))

      :macroexpansion
      (format "Unexpected error%s macroexpanding %sat (%s).%n%s%n"
              cause-type
              (if symbol (str symbol " ") "")
              loc
              cause)

      :compile-syntax-check
      (format "Syntax error%s compiling %sat (%s).%n%s%n"
              cause-type
              (if symbol (str symbol " ") "")
              loc
              cause)

      :compilation
      (format "Unexpected error%s compiling %sat (%s).%n%s%n"
              cause-type
              (if symbol (str symbol " ") "")
              loc
              cause)

      :read-eval-result
      (format "Error reading eval result%s at %s (%s).%n%s%n" cause-type symbol loc cause)

      :print-eval-result
      (format "Error printing return value%s at %s (%s).%n%s%n" cause-type symbol loc cause)

      :execution
      (if spec
        (format "Execution error - invalid arguments to %s at (%s).%n%s"
                symbol
                loc
                (with-out-str
                  (spec/explain-out
                    (if (= spec/*explain-out* spec/explain-printer)
                      (update spec :clojure.spec.alpha/problems
                              (fn [probs] (map #(dissoc % :in) probs)))
                      spec))))
        (format "Execution error%s at %s(%s).%n%s%n"
                cause-type
                (if symbol (str symbol " ") "")
                loc
                cause)))))

(defn err->msg
  "Helper to return an error message string from an exception."
  [^Exception e]                                                   ;;; Throwable
  (-> e Throwable->map ex-triage ex-str))

(defn repl-caught
  "Default :caught hook for repl"
  [e]
  (binding [*out* *err*]
    (print (err->msg e))
    (flush)))

(def ^{:doc "A sequence of lib specs that are applied to `require`
by default when a new command-line REPL is started."} repl-requires
  '[[clojure.repl :refer (source apropos dir pst doc find-doc)]
    ;;;[clojure.java.javadoc :refer (javadoc)]                            ;;; commented out
    [clojure.pprint :refer (pp pprint)]])

(defmacro with-read-known
  "Evaluates body with *read-eval* set to a \"known\" value,
   i.e. substituting true for :unknown if necessary."
  [& body]
  `(binding [*read-eval* (if (= :unknown *read-eval*) true *read-eval*)]
     ~@body))

(defn repl
  "Generic, reusable, read-eval-print loop. By default, reads from *in*,
  writes to *out*, and prints exception summaries to *err*. If you use the
  default :read hook, *in* must either be an instance of
  LineNumberingPushbackReader or duplicate its behavior of both supporting
  .unread and collapsing CR, LF, and CRLF into a single \\newline. Options
  are sequential keyword-value pairs. Available options and their defaults:

     - :init, function of no arguments, initialization hook called with
       bindings for set!-able vars in place.
       default: #()

     - :need-prompt, function of no arguments, called before each
       read-eval-print except the first, the user will be prompted if it
       returns true.
       default: (if (instance? LineNumberingPushbackReader *in*)
                  #(.atLineStart *in*)
                  #(identity true))

     - :prompt, function of no arguments, prompts for more input.
       default: repl-prompt

     - :flush, function of no arguments, flushes output
       default: flush

     - :read, function of two arguments, reads from *in*:
         - returns its first argument to request a fresh prompt
           - depending on need-prompt, this may cause the repl to prompt
             before reading again
         - returns its second argument to request an exit from the repl
         - else returns the next object read from the input stream
       default: repl-read

     - :eval, function of one argument, returns the evaluation of its
       argument
       default: eval

     - :print, function of one argument, prints its argument to the output
       default: prn

     - :caught, function of one argument, a throwable, called when
       read, eval, or print throws an exception or error
       default: repl-caught"
  [& options]
  ;;;(let [cl (.getContextClassLoader (Thread/currentThread))]
  ;;;  (.setContextClassLoader (Thread/currentThread) (clojure.lang.DynamicClassLoader. cl)))
  (let [{:keys [init need-prompt prompt flush read eval print caught]
         :or {init        #()
              need-prompt (if (instance? LineNumberingTextReader *in*)     ;;; LineNumberingPushbackReader
                            #(.AtLineStart ^LineNumberingTextReader *in*)                           ;;; atLineStart  LineNumberingPushbackReader
                            #(identity true))
              prompt      repl-prompt
              flush       flush
              read        repl-read
              eval        eval
              print       prn
              caught      repl-caught}}
        (apply hash-map options)
        request-prompt (Object.)
        request-exit (Object.)
        read-eval-print
        (fn []
          (try
            (let [read-eval *read-eval*
                  input (try
                          (with-read-known (read request-prompt request-exit))
                          (catch LispReader+ReaderException e                                                         ;;; LispReader$ReaderException
                            (throw (ex-info nil {:clojure.error/phase :read-source} e))))]
             (or (#{request-prompt request-exit} input)
               (let [value (binding [*read-eval* read-eval] (eval input))]
                   (set! *3 *2)
                   (set! *2 *1)
                   (set! *1 value)
                   (try
                     (print value)
                     (catch Exception e                                                                               ;;; Throwable
                       (throw (ex-info nil {:clojure.error/phase :print-eval-result} e)))))))
           (catch Exception e           ;;; Throwable
             (caught e)
             (set! *e e))))]
    (with-bindings
     (try
      (init)
      (catch Exception e                ;;; Throwable
        (caught e)
        (set! *e e)))
     (prompt)
     (flush)
     (loop []
       (when-not 
          (try (identical? (read-eval-print) request-exit)
    (catch Exception e                 ;;; Throwable
     (caught e)
     (set! *e e)
     nil))
         (when (need-prompt)
           (prompt)
           (flush))
         (recur))))))

(defn load-script
  "Loads Clojure source from a file or resource given its path. Paths
  beginning with @ or @/ are considered relative to classpath."
  [^String path]
  (if (.StartsWith path "@")                                  ;;; startsWith
    (RT/LoadCljScript                                         ;;; loadResourceScript
     (.Substring path (if (.StartsWith path "@/") 2 1)))      ;;; substring  startsWith
    (Compiler/loadFile path)))

(defn- init-opt
  "Load a script"
  [path]
  (load-script path))

(defn- eval-opt
  "Evals expressions in str, prints each non-nil result using prn"
  [str]
  (let [eof (Object.)
        reader (LineNumberingTextReader. (System.IO.StringReader. str))]               ;;; LineNumberingPushbackReader. java.io.StringReader.
      (loop [input (with-read-known (read reader false eof))]
        (when-not (= input eof)
          (let [value (eval input)]
            (when-not (nil? value)
              (prn value))
            (recur (with-read-known (read reader false eof))))))))

(defn- init-dispatch
  "Returns the handler associated with an init opt"
  [opt]
  ({"-i"     init-opt
    "--init" init-opt
    "-e"     eval-opt
    "--eval" eval-opt} opt))

(defn- initialize
  "Common initialize routine for repl, script, and null opts"
  [args inits]
  (in-ns 'user)
  (set! *command-line-args* args)
  (doseq [[opt arg] inits]
    ((init-dispatch opt) arg)))


(defn- main-opt
  "Call the -main function from a namespace with string arguments taken from
  the command line."
  [[_ main-ns & args] inits]
  (with-bindings
    (initialize args inits)
    (apply (ns-resolve (doto (symbol main-ns) require) '-main) args)))

(defn- repl-opt
  "Start a repl with args and inits. Print greeting if no eval options were
  present"
  [[_ & args] inits]
  (when-not (some #(= eval-opt (init-dispatch (first %))) inits)
    (println "Clojure" (clojure-version)))
  (repl :init (fn []
                (initialize args inits)
                (apply require repl-requires)))
  (prn)
  (Environment/Exit 0))                        ;;;  System.Exit

(defn- script-opt
  "Run a script from a file, resource, or standard in with args and inits"
  [[path & args] inits]
  (with-bindings
    (initialize args inits)
    (if (= path "-")
      (load-reader *in*)
      (load-script path))))

(defn- null-opt
  "No repl or script opt present, just bind args and run inits"
  [args inits]
  (with-bindings
    (initialize args inits)))

(defn- help-opt
  "Print help text for main"
  [_ _]
  (println (:doc (meta (var main)))))

(defn- main-dispatch
  "Returns the handler associated with a main option"
  [opt]
  (or
   ({"-r"     repl-opt
     "--repl" repl-opt
	 "-m"     main-opt
	 "--main" main-opt
     nil      null-opt
     "-h"     help-opt
     "--help" help-opt
     "-?"     help-opt} opt)
   script-opt))

(defn- legacy-repl
  "Called by the clojure.lang.Repl.main stub to run a repl with args
  specified the old way"
  [args]
  (println "WARNING: clojure.lang.Repl is deprecated.
Instead, use clojure.main like this:
java -cp clojure.jar clojure.main -i init.clj -r args...")
  (let [[inits [sep & args]] (split-with (complement #{"--"}) args)]
    (repl-opt (concat ["-r"] args) (map vector (repeat "-i") inits))))

(defn- legacy-script
  "Called by the clojure.lang.Script.main stub to run a script with args
  specified the old way"
  [args]
  (println "WARNING: clojure.lang.Script is deprecated.
Instead, use clojure.main like this:
java -cp clojure.jar clojure.main -i init.clj script.clj args...")
  (let [[inits [sep & args]] (split-with (complement #{"--"}) args)]
    (null-opt args (map vector (repeat "-i") inits))))

(defn report-error
  "Create and output an exception report for a Throwable to target.

  Options:
    :target - \"file\" (default), \"stderr\", \"none\"

  If file is specified but cannot be written, falls back to stderr."
  [^Exception t & {:keys [target]                                                       ;;; Throwable
                   :or {target "file"} :as opts}]
  (when-not (= target "none")
    (let [trace (Throwable->map t)
          triage (ex-triage trace)
          message (ex-str triage)
          report (array-map
                   :clojure.main/message message
                   :clojure.main/triage triage
                   :clojure.main/trace trace)
          report-str (with-out-str
                       (binding [*print-namespace-maps* false]
                         ((requiring-resolve 'clojure.pprint/pprint) report)))
          err-path (when (= target "file")
                     (try
                       (let [f (FileInfo. (Path/Combine (Path/GetTempPath) (str "clojure-" (System.Guid/NewGuid) ".edn")))]     ;;; (.toFile (Files/createTempFile "clojure-" ".edn" (into-array FileAttribute [])))
                         (with-open [w (StreamWriter. (.OpenWrite f))]                                                                       ;;; [w (BufferedWriter. (FileWriter. f))
                           (binding [*out* w] (println report-str)))
                         (.FullName f))                                                                                      ;;; .getAbsolutePath
                       (catch Exception _)))] ;; ignore, fallback to stderr                                                  ;;; Throwable
      (binding [*out* *err*]
        (if err-path
          (println (str message (Environment/NewLine) "Full report at:" (Environment/NewLine) err-path))                     ;;; System/lineSeparator
          (println (str report-str (Environment/NewLine) message)))))))                                                      ;;; System/lineSeparator

(defn main
  "Usage: java -cp clojure.jar clojure.main [init-opt*] [main-opt] [arg*]

  With no options or args, runs an interactive Read-Eval-Print Loop

  init options:
    -i, --init path     Load a file or resource
    -e, --eval string   Evaluate expressions in string; print non-nil values
    --report target     Report uncaught exception to \"file\" (default), \"stderr\",
                        or \"none\", overrides System property clojure.main.report

  main options:
    -r, --repl          Run a repl
    path                Run a script from a file or resource
	-m, --main ns-name  Run the -main function from a given namespace
    -                   Run a script from standard input
    -h, -?, --help      Print this help message and exit

  operation:

    - Establishes thread-local bindings for commonly set!-able vars
    - Enters the user namespace
    - Binds *command-line-args* to a seq of strings containing command line
      args that appear after any main option
    - Runs all init options in order
    - Calls a -main function or runs a repl or script if requested

  The init options may be repeated and mixed freely, but must appear before
  any main option. The appearance of any eval option before running a repl
  suppresses the usual repl greeting message: \"Clojure ~(clojure-version)\".

  Paths may be absolute or relative in the filesystem or relative to
  classpath. Classpath-relative paths have prefix of @ or @/"
  [& args]
  (try
   (if args
     (loop [[opt arg & more :as args] args, inits [], flags nil]
       (cond
         ;; flag
         (contains? #{"--report"} opt)
         (recur more inits (merge flags {(subs opt 2) arg}))

         ;; init opt
         (init-dispatch opt)
         (recur more (conj inits [opt arg]) flags)

         :main-opt
         (try
           ((main-dispatch opt) args inits)
           (catch Exception t                                                                                                              ;;; Throwable
             (report-error t :target (get flags "report" (or (System.Environment/GetEnvironmentVariable "clojure.main.report") "file")))   ;;; System/getProperty
             (Environment/Exit 1)))))                                                                                                      ;;; System/exit                 
     (try
       (repl-opt nil nil)
       (catch Exception t                                                                                                                  ;;; Throwable
         (report-error t :target "file")
         (Environment/Exit 1))))                                                                                                           ;;; System/exit
   (finally 
     (flush))))

