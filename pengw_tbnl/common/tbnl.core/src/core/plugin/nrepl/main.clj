(ns core.plugin.nrepl.main
  (:require (core [init :as init]
                  [state :as state]
                  [bus :as bus]
                  [plugin :as plugin]))
  (:require [clojure.tools.nrepl.server :as nrepl-server]
            [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.core.async
             :as async
             :refer [<!! chan]]))

(def defaults
  (atom
   {
    :wait 1000
    :nrepl-port 12321
    }))

(defn populate-parse-opts-vector
  [current-parse-opts-vector]
  (init/add-to-parse-opts-vector [

                                  (let [option :nrepl-port
                                        default (option @defaults)]
                                    ["-R"
                                     (str "--"
                                          (name option)
                                          " [PORT]")
                                     (str "nREPL port")
                                     :default default
                                     :parse-fn (get-in @init/parse-opts-vector-helper
                                                       [:parse-fn :inet-port])])

                                  ]))

(defn init
  [options]
  (when (:nrepl-port options)
    true))

(defn run
  [options]
  (plugin/blocked-thread-wrapper 
   options
   (let [nrepl-port (:nrepl-port options)]
     (plugin/set-state-entry :nrepl-server
                             (nrepl-server/start-server :port nrepl-port)))))

(def config-map
  "the config map"
  {
   :populate-parse-opts-vector populate-parse-opts-vector
   :init init
   :run run
   :param {:auto-restart true
           :wait (:wait @defaults)}})
