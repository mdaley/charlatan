(defproject charlatan "0.1.0-SNAPSHOT"
  :description "A clojure wrapper for mountebank - 'test doubles over the wire'."
  :url "http://github.com/mdaley/charlatan"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.5.0"]
                 [clj-http "2.0.1"]
                 [me.raynes/conch "0.8.0"]
                 [org.clojure/clojure "1.8.0"]]
  :scm {:name "git"
        :url "https://github.com/mdaley/charlatan"}
  :repositories [["releases" {:url "https://clojars.org/repo"
                              :creds :gpg}]])
