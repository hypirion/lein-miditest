(ns lein-miditest.test.plugin
  (:require [lein-miditest.plugin :refer :all]
            [clojure.test :refer :all]))

(defn- default-test-setup []
    (let [result-atom (atom 0)
          ok-fn (fn [] (swap! result-atom + 1))
          failure-fn (fn [] (reset! result-atom -1))]
      [result-atom ok-fn failure-fn]))

(defn- fib
  [n]
  (if (<= n 1) n
      (+ (fib (- n 1))
         (fib (- n 2)))))

(deftest simple-call-after-tests
  (let [[result-atom ok-fn failure-fn] (default-test-setup)
        default-fn (fn [] :success)
        altered-fn ((call-after ok-fn failure-fn) default-fn)]
    (testing "that the result from the fn is not altered"
      (is (= (altered-fn) :success))
      (reset! result-atom 0))
    (testing "that the success function is called after successes"
      (is (= (do (altered-fn) @result-atom) 1))
      (is (= (do (altered-fn) @result-atom) 2))
      (is (= (do (altered-fn) @result-atom) 3))
      (reset! result-atom 0))))

(deftest recursive-call-after-tests
  (let [[result-atom ok-fn failure-fn] (default-test-setup)
        altered-fib ((call-after ok-fn failure-fn) fib)]
    (testing "that the function only calls the ok-fn once"
      (is (= (fib 10) (altered-fib 10)))
      (is (= @result-atom 1)))))
