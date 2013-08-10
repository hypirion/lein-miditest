(ns lein-miditest.test.plugin
  (:require [leiningen.core.main :as main]
            [lein-miditest.plugin :refer :all]
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

(defn- recursive-failure
  [n]
  (if (neg? n)
    (throw (ex-info "Something we should never see." {:exit-code 1}))
    (do
      (try (recursive-failure (- n 1))
           (catch clojure.lang.ExceptionInfo e))
      (try (recursive-failure (- n 2))
           (catch clojure.lang.ExceptionInfo e))
      n)))

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
        altered-fib ((call-after ok-fn failure-fn) fib)
        altered-recursive-failure ((call-after (fn []) failure-fn)
                                   recursive-failure)]
    (testing "that the function only calls the ok-fn once"
      (is (= (fib 10) (altered-fib 10)))
      (is (= @result-atom 1)))
    (testing "that ExceptionInfo from recursive calls doesn't get caught"
      (reset! result-atom 0)
      (is (and (= (altered-recursive-failure 1) 1)
               (zero? @result-atom)))
      (is (and (= (altered-recursive-failure 10) 10)
               (zero? @result-atom))))))

(deftest call-after-failure-test
  (testing "that ExceptionInfo is thrown and no failure fn is called"
    (let [[result-atom ok-fn failure-fn] (default-test-setup)
          error-fn ((call-after ok-fn failure-fn)
                    (fn [] (throw (ex-info "ba-dumm" {:exit-code :arbitrary}))))]
      (binding [main/*exit-process?* false]
        (is (thrown? clojure.lang.ExceptionInfo (error-fn)))
        (reset! result-atom 0)
        (try
          (error-fn)
          (catch clojure.lang.ExceptionInfo e
            (is (zero? @result-atom))
            (is (= :arbitrary (get (ex-data e) :exit-code))))))))

  (testing "that main/exit is called if main/*exit-process?* is true"
    (let [[result-atom failure-fn ok-fn] (default-test-setup)
          ;; ^^ Swapping failure and ok here. Failure adds one, ok resets to -1.
          fail-fn ((call-after ok-fn failure-fn)
                   (fn ([] (throw (ex-info "boom!" {:no :exit-code})))
                     ([n] (throw (ex-info "boom!" {:exit-code n})))))]
      (with-redefs [main/exit (fn [exit-code] {:exit-code exit-code})]
        (binding [main/*exit-process?* true]
          (testing "that the correct exit-code is thrown"
            (is (= (fail-fn) {:exit-code 1}))
            (is (= (fail-fn 1) {:exit-code 1}))
            (is (= (fail-fn 5) {:exit-code 5})))
          (testing "that the failure command is called"
            (reset! result-atom 0)
             (is (= (do (fail-fn) @result-atom) 1))
             (is (= (do (fail-fn) @result-atom) 2))
             (is (= (do (fail-fn) @result-atom) 3))))))))
