# charlatan

A Clojure library that wraps [Mountebank](http://www.mbtest.org/) and that provides
a clean and simple way to launch imposters for isolated acceptance testing.

So, you have a piece of code (a web service for example) that you can launch and run
acceptance tests on. The code makes calls to other web resources. The aim of this library
is to allow you to stub calls to the remote web resources so that you can acceptance
test your code in isolation.

This library provides a way of launching Mountebank and creating _imposters_ that
represent the remote resources. You can choose how the _imposters_ respond to all the
calls made by your code. This can easily be done on a per test basis.

Why did I create this? In the last few years I have been testing clojure web services
using an acceptance testing mocking framework called [rest-driver](https://github.com/rest-driver/rest-driver) and its associated clojure wrapper [rest-cljer](https://github.com/whostolebenfrog/rest-cljer). This works really well but only with http requests. I was attracted by Mountebank's ability to work with other protocols (e.g. tcp, https) and by the fact that Mountebank was on Thoughtwork's TechRadar. So, I thought I'd give it a try...

## Usage

Firstly, install Mountebank following the [instructions](http://www.mbtest.org/docs/gettingStarted) and maybe read through a bit of the documentation to familiarise yourself with how it works.

Simply wrap a test or tests with the macro `with-mb` to launch a mountebank instance with the imposters and stubs required for that particular situation.

Here's a very simple example where the healthcheck resource of your service under test makes a call to the ping resource of a remote service:

```clojure
(ns test-namespace
  (:require [clj-http.client :as http]
            [clojure.test :refer :all]
            [charlatan.mountebank :as mb]))

(deftest service-healthcheck
  (testing "Healthcheck returns failure response when remote service ping fails"
    (mb/with-mb {:port 2525 :debug true}
      (mb/create-imposter 8081 {:stubs [{:responses [{:is {:statusCode 500}}]
                                         :predicates [{:equals {:method "GET"
                                                                :path "/ping"}}]}]})
      (let [{status :status body :body} (http/get "http://localhost:8080/healthcheck"
                                                  {:throw-exceptions false})]
        (is (= status 200))
        (is (= body "Something useful that says remote service is broken"))))))
```

The service under test is running on port 8080. It is configured to call the remote service on port 8081. The imposter has been set up so that when `/ping` is called on port 8081, it responds with 500 internal server error. Mountebank itself is running on port 2525.

For more complex situations, any number of imposters can be created running on different ports. So, stubs can be created for any number of remote services. Also, this is not limited to http conversations - imposters can be created for any protocol supported by Mountebank.

## License

Copyright © 2016 Matthew Daley

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
