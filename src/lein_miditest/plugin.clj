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
      (let [exit-after-tests leiningen.test/*exit-after-tests*]
        (try
          (binding [*recursive-test* true
                    leiningen.test/*exit-after-tests* false]
            (apply f args)
            (ok))
          (catch clojure.lang.ExceptionInfo e
            (println "we're getting here.")
            (when-not *recursive-test*
              (failure))
            (throw e)))))))

(defn hooks
  []
  (alter-var-root #'leiningen.test/test
                  (play-after ok-sound failure-sound)))

(alter-var-root #'hooks memoize)
