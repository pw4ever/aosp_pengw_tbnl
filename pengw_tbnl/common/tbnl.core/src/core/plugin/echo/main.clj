(ns core.plugin.echo.main
  (:require (core [init :as init]
                  [state :as state]
                  [bus :as bus]
                  [plugin :as plugin]))
  (:require [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.core.async
             :as async
             :refer [chan
                     <!!
                     alt!!
                     timeout]]))

(def defaults
  (atom
   {
    :wait 1000
    :echo-buffer 1000
    }))

(defn populate-parse-opts-vector
  [current-parse-opts-vector]
  (init/add-to-parse-opts-vector [
                                  
                                  [nil
                                   "--disable-echo"
                                   "disable echo mode"]

                                  ]))

(defn init
  [options]
  (when (not (:disable-echo options))
    true))

(defn run
  [options]
  (plugin/blocked-thread-wrapper
   options
   (let [verbose (:verbose options)
         ch (chan (:echo-buffer @defaults))]
     (bus/register-listener ch)
     (try
       (while true
         (prn (<!! ch)))
       (catch Exception e
         (when verbose
           (print-stack-trace e))
         (bus/unregister-listener ch)
         (throw e))))))

(def config-map
  "the config map"
  {:populate-parse-opts-vector populate-parse-opts-vector
   :init init
   :run run
   :param {:auto-restart true
           :wait (:wait @defaults)}})
