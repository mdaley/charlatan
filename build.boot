(def project 'charlatan)
(def version "0.1.0-SNAPSHOT")

(set-env!
  :resource-paths #{"src"}
  :source-paths #{"test"}
  :dependencies '[[org.clojure/clojure "1.8.0" :scope "provided"]
                  [cheshire "5.5.0"]
                  [clj-http "2.0.1"]
                  [me.raynes/conch "0.8.0"]
                  [adzerk/boot-test "1.2.0" :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "Clojure client library for Mountebank"
      :scm         {:url "https://github.com/GreenhouseGroupBV/charlatan"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))

(require '[adzerk.boot-test :refer [test]])
