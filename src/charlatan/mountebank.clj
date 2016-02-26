(ns charlatan.mountebank
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.string :refer [blank?]]
            [me.raynes.conch :refer [programs]]
            [me.raynes.conch.low-level :refer [proc stream-to-out exit-code destroy] :as sh]))

(programs which)
(programs mb)

(defn- installed?
  "Returns true if program is installed and on path."
  [exe]
  (not (blank? (which exe {:throw false}))))

(defn- kw->arg
  [kw]
  (str "--" (name kw)))

(def conch-keys #{:clear-env :env :dir :verbose :redirect-err})

(def mb-keys {:port ["--port" :hasval]
              :config-file ["--configfile" :hasval]
              :log-file ["--logfile" :hasval]
              :log-level ["--loglevel" :hasval]
              :allow-injection ["--allowInjection"]
              :allow-cors ["--allowCORS"]
              :mock ["--mock"]
              :debug ["--debug"]})

(defn- kvp->args
  "Convert a key value pair of the options to the right form for mountebank -
   either a valid parameter and a value or a valid parameter by itself.
   An exception is thrown if an invalid key is supplied."
  [[k v]]
  (if (contains? conch-keys k)
    [k v]
    (if-let [mb-valdef (k mb-keys)]
      (if (second mb-valdef)
        [(first mb-valdef) (str v)]
        [(first mb-valdef)])
      (throw (Exception. (str "Invalid argument '" k "'."))))))

(defn- options->args
  "Convert options map to args required by mountebank."
  [options]
  (flatten (mapv kvp->args options)))

(defn start
  "Starts a mountebank server"
  [& [options]]
  (let [args (options->args options)
        p (proc "mb" args)]
    (future (stream-to-out p :out))
    (future (stream-to-out p :err))
    p))

(defn mburl+
  [mb-port & [suffix]]
  (apply str "http://localhost:" mb-port suffix))

(defn get-config
  [mb-port]
  (http/get (mburl+ mb-port "/config")
            {:as :json}))

(defn- create-imposter-data
  [{:keys [port protocol name stubs key cert mode
           mutual-auth end-of-request-resolver] :or {protocol :http}}]
  (cond-> {:protocol (clojure.core/name protocol)}
    port (assoc :port port)
    name (assoc :name name)
    stubs (assoc :stubs stubs)
    key (assoc :key key)
    cert (assoc :cert cert)
    mutual-auth (assoc :mutualAuth mutual-auth)
    end-of-request-resolver (assoc :endOfRequestResolver end-of-request-resolver)
    mode (assoc :mode (clojure.core/name mode))))

(defn create-imposter
  "Create an imposter. Note, if you want an imposter on a random port,
   pass in port = nil."
  [mb-port port & [params]]
  (http/post (mburl+ mb-port "/imposters")
             {:as :json
              :content-type "application/json"
              :body (json/generate-string (create-imposter-data (assoc params :port port)))}))

(defn get-imposter
  [mb-port port]
  (http/get (mburl+ mb-port "/imposters/" port)
            {:as :json}))

(defn- replay-and-proxy-params
  [{:keys [replayable remove-proxies]}]
  (cond-> {}
    (not (nil? replayable)) (assoc :replayable replayable)
    (not (nil? remove-proxies)) (assoc :removeProxies remove-proxies)))

(defn delete-imposter
  [mb-port port & [params]]
  (http/delete (mburl+ mb-port "/imposters/" port)
               {:as :json
                :query-params (replay-and-proxy-params params)}))

(defn list-imposters
  [mb-port & [params]]
  (http/get (mburl+ mb-port "/imposters")
            {:as :json
             :query-params (replay-and-proxy-params params)}))

(defn replace-imposters
  "Replace all existing imposters with a new set. `imposters` is a list of
   one or more new imposters."
  [mb-port imposters]
  (http/put (mburl+ mb-port "/imposters")
            {:as :json
             :content-type "application/json"
             :body (json/generate-string {:imposters (map create-imposter-data imposters)})}))

(defn delete-imposters
  [mb-port & [params]]
  (http/delete (mburl+ mb-port "/imposters")
               {:as :json
                :query-params (replay-and-proxy-params params)}))
