(ns figurehead.plugin.mastermind.main
  (:require (figurehead.util [services :as services :refer [get-service]]))
  (:require (core [init :as init]
                  [state :as state]
                  [bus :as bus]
                  [plugin :as plugin]))
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.core.async
             :as async
             :refer [thread chan <!! >!!]])
  (:import
   (java.util UUID)
   (java.net Socket)
   (java.io IOException)
   (android.text.format Time)
   (android.app IActivityManager
                IActivityController$Stub
                ActivityManager$RunningAppProcessInfo)
   (android.content Intent)
   (android.content.pm IPackageManager
                       PackageManager
                       
                       ActivityInfo
                       ServiceInfo
                       ProviderInfo)))

(def defaults
  (atom
   {
    :mastermind-port 4321
    :socket-timeout 15000
    :writer-buffer 1000
    }))

(defn populate-parse-opts-vector
  [current-parse-opts-vector]
  (init/add-to-parse-opts-vector [
                                  
                                  ["-a"
                                   "--mastermind-address ADDR"
                                   "mastermind address"]

                                  (let [option :mastermind-port
                                        default (option @defaults)]
                                    ["-p"
                                     (str "--"
                                          (name option)
                                          " [PORT]")
                                     (str "mastermind port")
                                     :default default
                                     :parse-fn (get-in @init/parse-opts-vector-helper
                                                       [:parse-fn :inet-port])])

                                  ]))

(defn init
  [options]
  (when (and (:mastermind-address options)
             (:mastermind-port options))
    true))

(defn run
  [options]
  (plugin/blocked-thread-wrapper
   options
   (let [verbose (:verbose options)
         mastermind-address (:mastermind-address options)
         mastermind-port (:mastermind-port options)]
     (try
       (let [sock (Socket. ^String mastermind-address
                           ^int mastermind-port)]
         (.setSoTimeout sock (:socket-timeout @defaults))
         ;; reader thread
         (thread
           ;; catch sub-thread exception
           (try
             (with-open [^java.io.BufferedReader reader (io/reader sock)]
               (while true
                 (try
                   (when-let [line (.readLine reader)]
                     (try
                       (let [message (read-string line)
                             topic (bus/get-message-topic message)
                             content (bus/remove-message-topic message)]
                         (case topic
                           :command
                           (do
                             (bus/say!! :command content))))
                       (catch RuntimeException e
                         (when verbose
                           (print-stack-trace e)))))
                   (catch IOException e
                     (when verbose
                       (print-stack-trace e))))))
             (finally
               (.close sock))))
         ;; writer thread
         (thread
           (try
             (with-open [^java.io.BufferedWriter writer (io/writer sock)]
               (let [ch (chan (:writer-buffer @defaults))]
                 (try
                   (bus/register-listener ch)
                   (while true
                     (let [message (<!! ch)
                           topic (bus/get-message-topic message)]
                       (cond
                        (not (contains? #{:command} topic))
                        (do
                          ;; do not echo command back
                          (.write writer (prn-str message))
                          (.flush writer)))))
                   (finally
                     (bus/unregister-listener ch)))))
             (finally
               (.close sock)))))))))


(def config-map
  "the config map"
  {:populate-parse-opts-vector populate-parse-opts-vector
   :init init
   :run run
   :param {:auto-restart true}})
