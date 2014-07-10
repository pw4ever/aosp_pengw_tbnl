(ns figurehead.plugin.monitor.main
  (:require (figurehead.util [services :as services :refer [get-service]]))
  (:require (core [init :as init]
                  [state :as state]
                  [bus :as bus]
                  [plugin :as plugin]))
  (:require [clojure.string :as str]
            [clojure.core.async :as async])
  (:import
   (java.util UUID)
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
    }))

(defn populate-parse-opts-vector
  [current-parse-opts-vector]
  (init/add-to-parse-opts-vector [
                                  
                                  ["-m"
                                   "--monitor"
                                   "enter monitor mode"]

                                  ]))

(defn init
  [options]
  (when (:monitor options)
    true))

(defn run
  [options]
  (plugin/blocked-thread-wrapper
   options
   (let [activity-manager ^IActivityManager (get-service :activity-manager)
         session-id (-> (UUID/randomUUID) str keyword)
         now (Time.)
         ;; this is the meat
         activity-controller (proxy
                                 [IActivityController$Stub]
                                 []

                               (activityStarting [^Intent intent package]
                                 (locking this
                                   (locking intent
                                     (bus/say!! :activity-controller.starting
                                                {:session session-id
                                                 :timestamp (do (.setToNow now)
                                                                (.toMillis now true))
                                                 :package (-> package keyword)
                                                 :intent-action (-> intent .getAction keyword)
                                                 ;; the "/" prevents straightforward keyword-ize
                                                 :intent-component (str 
                                                                    (.. intent getComponent getPackageName)
                                                                    "/"
                                                                    (.. intent getComponent getShortClassName))
                                                 :intent-category (into #{} (map keyword
                                                                                 (.getCategories intent)))
                                                 ;; data and extras may contain non-keyword-izable content
                                                 :intent-data (-> intent .getDataString)
                                                 :intent-extras (-> intent .getExtras)
                                                 :intent-flags (-> intent .getFlags)
                                                 }))
                                   true))

                               (activityResuming [package]
                                 (locking this
                                   (bus/say!! :activity-controller.resuming
                                              {:session session-id
                                               :timestamp (do (.setToNow now)
                                                              (.toMillis now true))
                                               :package (-> package keyword)
                                               })
                                   true))

                               (appCrashed [process-name pid
                                            short-msg long-msg
                                            time-millis stack-trace]
                                 (locking this
                                   (doseq [^ActivityManager$RunningAppProcessInfo app-proc (.getRunningAppProcesses activity-manager)]
                                     (when (and (= pid (.pid app-proc))
                                                (= process-name (.processName app-proc)))
                                       (bus/say!! :activity-controller.crashed
                                                  {:session session-id
                                                   :timestamp (do (.setToNow now)
                                                                  (.toMillis now true))
                                                   :packages (into #{}
                                                                   (map keyword
                                                                        (.pkgList app-proc)))})))
                                   true))

                               (appEarlyNotResponding [process-name pid annotation]
                                 (locking this
                                   1))

                               (appNotResponding [process-name pid process-stats]
                                 (locking this
                                   1))

                               (systemNotResponding [msg]
                                 (locking this
                                   1))
                               
                               )]
     (.setActivityController activity-manager
                             activity-controller))))


(def config-map
  "the config map"
  {:populate-parse-opts-vector populate-parse-opts-vector
   :init init
   :run run
   :param {:auto-restart false}})
