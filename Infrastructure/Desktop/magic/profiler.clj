(ns magic.profiler)

(def stop-watch (System.Diagnostics.Stopwatch/StartNew))

(def profile-trace (atom []))

(defn reset-trace! [] (reset! profile-trace []))

(def US-RATIO  (/ 1000000.0 System.Diagnostics.Stopwatch/Frequency))
(def MS-RATIO  (/ 1000.0 System.Diagnostics.Stopwatch/Frequency))
(def S-RATIO  (/ 1.0 System.Diagnostics.Stopwatch/Frequency))

(defmacro profile [name args & expr]
  `(do ~@expr)
  #_
  `(let [start# (.ElapsedTicks ^System.Diagnostics.Stopwatch stop-watch)
         result# (do ~@expr)
         end# (.ElapsedTicks ^System.Diagnostics.Stopwatch stop-watch)]
     (swap! profile-trace conj {:ph "B" :ts (* US-RATIO start#) :name ~name :args ~args})
     (swap! profile-trace conj {:ph "E" :ts (* US-RATIO end#) :name ~name :args ~args})
     result#))

(defn stringify [x]
  (-> x pr-str pr-str))

(defn write-trace! [path]
  (let [sb (System.Text.StringBuilder. "[\n")]
    (doseq [{:keys [name ph ts args]} @magic.profiler/profile-trace]
      (.AppendLine sb (str "{"
                           "\"name\":" (stringify name)
                           ",\"ph\":" (pr-str ph)
                           ",\"ts\":" (stringify ts)
                           ",\"args\":" (stringify args)
                           "},")))
    (spit path (.ToString sb) :file-mode System.IO.FileMode/Truncate)))