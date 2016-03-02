(ns charlatan.core-test
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.test :refer :all]
            [charlatan.core :refer :all]
            [charlatan.mountebank :as mb]))

(def mb-port 2525)
(def port 8080)

(deftest simple-response-to-ping
  (testing "mountebank simple response stub"
    (let [m (mb/start {:port mb-port})]
      (try
        (mb/create-imposter mb-port port
                            {:stubs [{:responses [{:is {:statusCode 202
                                                        :body "pong"}}]
                                      :predicates [{:equals {:method "GET"
                                                             :path "/ping"}}]}]})
        (let [response (http/get (str "http://localhost:" port "/ping")
                                 {:throw-exceptions false})]
          (is (= (:status response) 202))
          (is (= (:body response) "pong"))
          )
        (finally
          (println "FINALLY")
          (mb/stop m))))))

(deftest test-with-approach
  (testing "mountebank simple response stub inside with-mb macro"
    (mb/with-mb {:port 2567}
      (mb/create-imposter port  {:stubs [{:responses [{:is {:statusCode 202
                                                            :body "pong"}}]
                                          :predicates [{:equals {:method "GET"
                                                                 :path "/ping"}}]}]})
      (let [{status :status body :body} (http/get (str "http://localhost:" port "/ping")
                                                  {:throw-exceptions false})]
        (is (= status 202))
        (is (= body "pong"))))))
