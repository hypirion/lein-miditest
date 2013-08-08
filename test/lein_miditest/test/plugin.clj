(ns lein-miditest.test.plugin
  (:require [lein-miditest.plugin :refer :all]
            [clojure.test :refer :all]))

(defn- default-test-setup []
    (let [result-atom (atom 0)
          ok-fn (fn [] (swap! result-atom + 1))
          failure-fn (fn [] (reset! result-atom -1))]
      [result-atom ok-fn failure-fn]))

(deftest call-after-tests
  (let [[result-atom ok-fn failure-fn] (default-test-setup)
        default-fn (fn [] :success)
        altered-fn ((call-after ok-fn failure-fn) default-fn)]
    (testing "that the result from the fn is not altered"
      (is (= (altered-fn) :success))
      (reset! result-atom 0))
    (testing "that the success function is called after success"
      (is (= (do (altered-fn) @result-atom) 1))
      (is (= (do (altered-fn) @result-atom) 2))
      (is (= (do (altered-fn) @result-atom) 3))
      (reset! result-atom 0))))
