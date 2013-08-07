(ns lein-miditest.plugin
  (:require [leiningen.miditest :as midi]
            [leiningen.test :as test]
            [leiningen.core.main :as main]))

(def ^:private ^:dynamic *recursive-test* false)

(defn ok-sound []
  (midi/play-instrument "Timpani" 80))

(defn failure-sound []
  (midi/play-instrument "French Horn" 60))

(defn play-after [ok failure]
  (fn [f]
    (fn [& args]
      (let [exit-process? main/*exit-process?*]
        (try
          (binding [*recursive-test* true
                    main/*exit-process?* false]
            (apply f args)
            (ok))
          (catch clojure.lang.ExceptionInfo e
            (if exit-process?
              (let [exit-code (get (ex-data e) :exit-code 1)]
                (when-not *recursive-test*
                  (failure))
                (main/exit exit-code))
              (throw e))))))))

(defn hooks
  []
  (alter-var-root #'test/test
                  (play-after ok-sound failure-sound)))

(alter-var-root #'hooks memoize)
