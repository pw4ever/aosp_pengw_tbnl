(ns mastermind.plugin.figurehead.main
  (:require (core [init :as init]
                  [state :as state]
                  [bus :as bus]
                  [plugin :as plugin]))

  (:require [clojure.java.io :as io]
            [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.core.async
             :as async
             :refer [chan
                     go <! >!]]
   )
  (:import [java.net
            ServerSocket
            SocketException
            ]
           [java.io
            IOException]))

(def defaults
  (atom
   {
    :figurehead-port 4321
    :writer-buffer 1
    }))

(defn populate-parse-opts-vector
  [current-parse-opts-vector]
  (init/add-to-parse-opts-vector [

                                  (let [option :figurehead-port
                                        default (option @defaults)]
                                    [nil
                                     (str "--"
                                          (name option)
                                          " [PORT]")
                                     (str "figurehead port")
                                     :default default
                                     :parse-fn (get-in @init/parse-opts-vector-helper
                                                       [:parse-fn :inet-port])])

                                  ]))

(defn init
  [options]
  (let [figurehead-port (:figurehead-port options)]
    (when (and figurehead-port)
      (plugin/set-state-entry :figurehead-port figurehead-port)
      true)))

(defn run
  [options]
  (plugin/blocked-thread-wrapper
   options
   (let [verbose (:verbose options)
         figurehead-port (:figurehead-port options)]
     (try
       (when verbose
         (prn [:figurehead :server-sock :creating figurehead-port]))
       (with-open [server-sock (ServerSocket. figurehead-port)]
         (while true
           (let [sock (.accept server-sock)]
             (.run
              (Thread.
               (fn []
                 (try
                   ;; reader
                   (go
                     (try
                       (with-open [reader (io/reader sock)]
                         (while true
                           (try
                             (when-let [line (.readLine reader)]
                               (try
                                 (let [line (read-string line)]
                                   (bus/say!! :information line verbose))
                                 (catch RuntimeException e
                                   (when verbose
                                     (prn [:figurehead :reader :RuntimeException])
                                     (print-stack-trace e)))))
                             (catch SocketException e
                               ;; !! do not let IOException mask this
                               (when verbose
                                 (prn [:figurehead :reader :SocketException])
                                 (print-stack-trace))
                               (throw e))
                             (catch IOException e
                               (when verbose
                                 (prn [:figurehead :reader :IOException])
                                 (print-stack-trace e)))
                             (catch Exception e
                               (when verbose
                                 (prn [:figurehead :reader :Exception])
                                 (print-stack-trace e))
                               (throw e)))))
                       (finally
                         (.close sock))))
                   ;; writer
                   (go
                     (try
                       (with-open [writer (io/writer sock)]
                         (let [ch (chan (:writer-buffer @defaults))]
                           (try
                             (bus/sub-topic ch :command)
                             (while true
                               (let [line (<! ch)]
                                 (.write writer (prn-str line))
                                 (.flush writer)))
                             (finally
                               (bus/unsub-topic ch :command)))))
                       (finally
                         (.close sock))))
                   (catch IOException e
                     (when verbose
                       (print-stack-trace e))
                     (.close sock)))))))))
       (catch IOException e
         (when verbose
           (prn [:figurehead :IOException])
           (print-stack-trace e)))))))

(def config-map
  "the config map"
  {
   :populate-parse-opts-vector populate-parse-opts-vector
   :init init
   :run run
   :param {:auto-restart true}})
