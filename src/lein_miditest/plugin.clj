(ns lein-miditest.plugin
  (:require [leiningen.miditest :as midi]
            [leiningen.test]))

(defn error-sound []
  (midi/play-instrument "French Horn"))

(defn play-after [song]
  (fn [f]
    (fn [& args]
      (apply f args)
      (song))))

(defn hooks
  []
  (println "Oh, I'm hooked! I'm hooked! Wee :D")
  (alter-var-root #'leiningen.test/test (play-after error-sound)))

(alter-var-root #'hooks memoize)
