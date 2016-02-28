(ns charlatan.core-test
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.test :refer :all]
            [charlatan.core :refer :all]
            [charlatan.mountebank :as mb]))

(def mb-port 2525)
(def port 8080)

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

(deftest simple-response-to-ping
  (testing "mountebank launched, set with stub, request made, correct response, stop mountebank"
    (let [m (mb/start {:port mb-port})]
      (try
        (println "M" m)
        (Thread/sleep 1000)
        (mb/create-imposter mb-port port
                            {:stubs [{:responses [{:is {:statusCode 202
                                                        :body "pong"}}]
                                      :predicates [{:equals {:method "GET"
                                                             :path "/ping"}}]}]})
        (println "CREATED IMPOSTER")
        (Thread/sleep 1000)
        (let [response (http/get (str "http://localhost:" port "/ping")
                                 {:throw-exceptions false})]
          (println "RESPONSE" response)
          (is (= (:status response) 202))
          (is (= (:body response) "pong"))
          )
        (finally
          (println "FINALLY")
          (mb/stop m))))))
