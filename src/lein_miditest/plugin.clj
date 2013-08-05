(ns lein-miditest.plugin
  (:require [leiningen.miditest :as midi]))

(defn hooks
  []
  (println "Oh, I'm hooked! I'm hooked! Wee :D"))

(alter-var-root #'hooks memoize)
