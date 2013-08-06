(ns lein-miditest.plugin
  (:require [leiningen.miditest :as midi]
            [leiningen.test]))

(def ^:private ^:dynamic *recursive-test* false)

(defn error-sound []
  (midi/play-instrument "French Horn"))

(defn play-after [song]
  (fn [f]
    (fn [& args]
      (binding [*recursive-test* true]
        (apply f args))
      (when-not *recursive-test*
        (song)))))

(defn hooks
  []
  (alter-var-root #'leiningen.test/test (play-after error-sound)))

(alter-var-root #'hooks memoize)
