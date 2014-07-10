(ns core.plugin
  (:require [clojure.core.async :as async :refer [thread <!! chan timeout]]))

(declare list-all-plugins
         get-plugin get-plugin-main-entry
         get-config-map get-config-map-entry update-config-map-entry set-config-map-entry
         get-param get-param-entry update-param-entry set-param-entry
         get-state get-state-entry update-state-entry set-state-entry
         load-plugin unload-plugin
         populate-parse-opts-vector
         init-plugin run-plugin init-and-run-plugin
         block-thread blocked-thread-wrapper)

(def ^:dynamic *current-plugin*
  "bound to current plugin by the context")

(def defaults
  "the defaults"
  (atom {
         :auto-restart-retry-interval 1000
         }))

(def plugins
  "all loaded plugins and their config map"
  (atom {}))


(defn list-all-plugins
  "list all plugins"
  []
  (keys @plugins))

(defn get-plugin
  "get the named plugin"
  ([] (get-plugin *current-plugin*))
  ([plugin]
     (keyword plugin)))

(defn get-plugin-main-entry
  "get the main entry to the plugin"
  ([] (get-plugin-main-entry *current-plugin*))
  ([plugin]
     (symbol (str plugin ".main"))))

;;; config-map

(defn get-config-map 
  "get the config map"
  ([] (get-config-map *current-plugin*))
  ([plugin]
     (get @plugins (get-plugin plugin))))

(defn get-config-map-entry 
  "get a config entry"
  ([key] (get-config-map-entry *current-plugin* key))
  ([plugin key]
     (get (get-config-map plugin) key)))

(defn update-config-map-entry
  "update a config entry to (f <current entry> & args)"
  ([plugin key f & args]
     (apply swap! plugins update-in 
            [(get-plugin plugin) key]
            f args)))

(defn set-config-map-entry
  "set a config entry to val"
  ([key val] (set-config-map-entry *current-plugin* key val))
  ([plugin key val]
     (update-config-map-entry plugin key (constantly val))))

;;; config-map/param
(defn get-param
  "get the params"
  ([] (get-param *current-plugin*))
  ([plugin]
     (get-config-map-entry plugin :param)))

(defn get-param-entry
  "get a param entry"
  ([] (get-param-entry *current-plugin*))
  ([plugin key]
     (get (get-param plugin) key)))

(defn update-param-entry
  "update a param entry to (f <current entry> & args)"
  ([plugin key f & args]
     (apply swap! plugins update-in
            [(get-plugin plugin) :param key]
            f args)))

(defn set-param-entry
  "set a parameter entry to val"
  ([key val] (set-param-entry *current-plugin* key val))
  ([plugin key val]
     (update-param-entry plugin key (constantly val))))

;;; config-map/state
(defn get-state
  "get the states"
  ([] (get-state *current-plugin*))
  ([plugin]
     (get-config-map-entry plugin :state)))

(defn get-state-entry
  "get a state entry"
  ([key] (get-state-entry *current-plugin* key))
  ([plugin key]
     (get (get-state plugin) key)))

(defn update-state-entry
  "update a state entry to (f <current entry> & args)"
  ([plugin key f & args]
     (apply swap! plugins update-in
            [(get-plugin plugin) :state key]
            f args)))

(defn set-state-entry
  "set a state entry to val"
  ([key val] (set-state-entry *current-plugin* key val))
  ([plugin key val]
     (update-state-entry plugin key (constantly val))))

;;; load/unload plugin

(defn load-plugin
  "load a plugin"
  [plugin]
  (binding [*current-plugin* plugin]
    (require (get-plugin-main-entry plugin))
    (swap! plugins
           assoc
           (get-plugin plugin)
           @(ns-resolve (get-plugin-main-entry plugin)
                        'config-map))))

(defn unload-plugin
  "unload the plugins"
  [plugin]
  (binding [*current-plugin* plugin]
    (when-let [unload (get-config-map-entry plugin :unload)]
      (unload))
    (swap! plugins
           dissoc
           (get-plugin plugin))))

;;; populate parse-opts vector
(defn populate-parse-opts-vector 
  "populate parse-opts vector for the plugin"
  [plugin current-parse-opts-vector]
  (binding [*current-plugin* plugin]
    (when-let [populate-parse-opts-vector (get-config-map-entry plugin :populate-parse-opts-vector)]
      (populate-parse-opts-vector current-parse-opts-vector))))

;;; init/run plugin
(defn init-plugin 
  "initialize the plugin with the options; return false to abort running the plugin"
  [plugin options]
  (binding [*current-plugin* plugin]
    (if-let [init (get-config-map-entry plugin :init)]
      (init options)
      true)))

(defn run-plugin 
  "run the plugin with the options in a separte thread"
  [plugin options]
  (binding [*current-plugin* plugin]
    (when-let [run (get-config-map-entry plugin :run)]
      (if (get-param-entry plugin :sync)
        (run options)
        (thread
          ;; only :async plugin can auto-restart
          (loop []
            (run options)
            (when (get-param-entry plugin :auto-restart)
              (Thread/sleep (:auto-restart-retry-interval @defaults))
              (recur))))))))

(defn init-and-run-plugin
  "init the plugin and, if successful, run it"
  [plugin options]
  (binding [*current-plugin* plugin]
    (let [verbose (:verbose options)]
      (when verbose
        (prn (list :init-plugin plugin options)))
      (when (init-plugin plugin options)
        (when verbose
          (prn (list :run-plugin plugin options)))
        (run-plugin plugin options)
        (when-let [wait (get-param-entry plugin :wait)]
          (when verbose
            (prn (list :wait wait)))
          (<!! (async/timeout wait)))))))

(defn block-thread
  "block the plugin thread so will not keep restarting the plugin"
  ([] (<!! (chan)))
  ([duration] (<!! (timeout duration))))

(defmacro blocked-thread-wrapper
  "plugin body wrapper that blocks the plugin thread at the end"
  [options & body]
  `(let [verbose# (:verbose ~options)]
     (try
       ~@body
       (block-thread)
       (catch Exception e#
         (when verbose#
           (clojure.stacktrace/print-stack-trace e#))
         (throw e#)))))
