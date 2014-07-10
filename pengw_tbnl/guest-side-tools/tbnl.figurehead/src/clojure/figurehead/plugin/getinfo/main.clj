(ns figurehead.plugin.getinfo.main
  (:require (figurehead.plugin.getinfo [util
                                        :refer [get-package-info
                                                get-all-packages]]))
  (:require (figurehead.util [services :as services :refer [get-service]]))
  (:require (core [init :as init]
                  [state :as state]
                  [bus :as bus]
                  [plugin :as plugin]))
  (:require [clojure.string :as str]
            [clojure.core.async
             :as async
             :refer [<!! >!! timeout]])
  (:import
   (java.util UUID)
   (android.content.pm IPackageManager
                       PackageManager
                       
                       ActivityInfo
                       ServiceInfo
                       ProviderInfo
                       
                       PackageInfo)))

(def defaults
  (atom
   {
    :repeat-interval 5000
    :wait 1000
    }))

(defn populate-parse-opts-vector
  [current-parse-opts-vector]
  (init/add-to-parse-opts-vector [
                                  
                                  ["-i"
                                   "--getinfo"
                                   "enter getinfo mode"]

                                  [nil
                                   "--package PACKAGE"
                                   "specify a target package"]

                                  ]))

(defn init
  [options]
  (when (:getinfo options)
    true))

(defn run
  [options]
  (let [verbose (:verbose options)]
    (let [session-id (-> (UUID/randomUUID) str keyword)]
      (let [package (:package options)]
        (if package
          (bus/say!! :package-manager.get-package-info 
                     (assoc (get-package-info package)
                       :session session-id)
                     verbose)
          (bus/say!! :package-manager.get-all-packages
                     (assoc {:packages (into #{} (map (fn [^PackageInfo package-info]
                                                        (-> package-info .packageName keyword))
                                                      (get-all-packages)))}
                       :session session-id)
                     verbose))))))

(def config-map
  "the config map"
  {:populate-parse-opts-vector populate-parse-opts-vector
   :init init
   :run run
   :param {:auto-restart false
           :wait (:wait @defaults)}})
