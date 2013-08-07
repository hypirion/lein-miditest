(ns lein-miditest.plugin
  (:require [leiningen.miditest :as midi]
            [leiningen.test]))

(def ^:private ^:dynamic *recursive-test* false)

(defn ok-sound []
  (midi/play-instrument "Timpani" 80))

(defn failure-sound []
  (midi/play-instrument "French Horn" 60))

(defn play-after [ok failure]
  (fn [f]
    (fn [& args]
      (try
        (binding [*recursive-test* true]
          (apply f args)
          (ok))
        (catch Exception e
          (println "hullo")
          (when-not *recursive-test*
            (failure))
          (throw e))))))

(defn hooks
  []
  (alter-var-root #'leiningen.test/test (play-after ok-sound failure-sound)))

(alter-var-root #'hooks memoize)
