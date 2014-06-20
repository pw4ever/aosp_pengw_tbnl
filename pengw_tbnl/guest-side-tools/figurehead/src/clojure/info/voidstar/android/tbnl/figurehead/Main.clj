(ns info.voidstar.android.tbnl.figurehead.Main
  (:import
   (android.os ServiceManager
               IServiceManager
               UserHandle)
   (android.app ActivityManager
                ActivityManagerNative
                IActivityController
                IActivityManager)
   (android.content.pm IPackageManager
                       IPackageManager$Stub
                       PackageManager
                       PackageInfo
                       
                       ActivityInfo
                       ServiceInfo
                       ProviderInfo
                       )
   (android.view InputDevice
                 KeyEvent)
   )
  (:require
   [clojure.tools.macro :refer :all]
   [clojure.string :as str]
   [clojure.pprint :refer :all]
   [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(defn say [type what]
  (let [tags {
              :activity-controller "ActivityController"
              :package-manager "PackageManager"
              :error "*error*"
              :normal "*normal*"
              :over "*over*"
              }]
    (println (str "<" (type tags) ">" what))))

(def cli-options
  [["-h" "--help" "show help"]
   ["-p" "--package PACKAGE" "specify a target package"
    :id :package]])

(def ^:dynamic policy
  {:obtain-service-handles-wait-interval 500})

(def services (atom {}))

(defn obtain-service-handles []
  (macrolet [(get-handle [service obtain-method]
                         `(loop [h# ~obtain-method]
                            (if h#
                              (swap! services assoc ~service h#)
                              (do
                                (say :error (str "obtaining " ~service " handler..."))
                                (Thread/sleep (:obtain-service-handles-wait-interval policy))
                                (recur ~obtain-method)))))]
            (get-handle :activity 
                        (ActivityManagerNative/getDefault))
            (get-handle :package 
                        (-> 
                         (ServiceManager/getService "package") 
                         (IPackageManager$Stub/asInterface))))
  services)

(defn get-package-info [services package]
  (let [pkg-info (.getPackageInfo ^IPackageManager (:package @services)
                                  package
                                  (bit-or PackageManager/GET_ACTIVITIES
                                          PackageManager/GET_PROVIDERS
                                          PackageManager/GET_RECEIVERS
                                          PackageManager/GET_SERVICES
                                          PackageManager/GET_PERMISSIONS
                                          PackageManager/GET_CONFIGURATIONS)
                                  0)]
    (when pkg-info
      
      (when-let [activities (.activities pkg-info)]
        (doseq [^ActivityInfo activity activities]
          (say :package-manager
               (str "type=activity|package=" (.packageName pkg-info)  "|name=" (.name activity)))))

      (when-let [services (.services pkg-info)]
        (doseq [^ServiceInfo service services]
          (say :package-manager
               (str "type=service|package=" (.packageName pkg-info)  "|name=" (.name service)))))

      (when-let [providers (.providers pkg-info)]
        (doseq [^ProviderInfo provider providers]
          (say :package-manager
               (str "type=provider|package=" (.packageName pkg-info)  "|name=" (.name provider)))))

      (when-let [receivers (.receivers pkg-info)]
        (doseq [^ActivityInfo receiver receivers]
          (say :package-manager
               (str "type=receiver|package=" (.packageName pkg-info)  "|name=" (.name receiver))))))))


(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (let [services (obtain-service-handles)]
      (get-package-info services (:package options)))))
