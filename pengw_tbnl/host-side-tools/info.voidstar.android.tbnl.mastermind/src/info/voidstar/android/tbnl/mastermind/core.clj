(ns info.voidstar.android.tbnl.mastermind.core
  (:use 
   info.voidstar.android.tbnl.mastermind.cmd-probe
   info.voidstar.android.tbnl.mastermind.cmd-visualize
   [alembic.still :only (distill load-project)]
   clojopts.core
   )
  (:require
   [clojure.string :as str]
   )
  (:gen-class))

(declare
 ^:dynamic ^:private *opts* 
 ^:dynamic ^:prviate *cmds*
 )

(def ^:dynamic ^:private *cmds*
  {
   :probe
   (fn []
     (cmd-probe *opts*))
   
   :visualize
   (fn []
     (cmd-visualize *opts*))

   :usage
   (fn []
     (binding [*out* *err*]
       (println (str/join "\n\n"
                          [
                           (str/join "\n\t" [
                                         "--cmd=visaulize"
                                         "[--output-name-root=<root|default: output>]"
                                         "[--monitor-trace=<tbnl monitor trace file|example: \"trace.txt\" or a FIFO connected with a TBNL session>]+"
                                         ])
                           (str/join "\n\t" [
                                         "--cmd=probe"
                                         "[--output-name-root=<root|default: output>]"
                                         "[--monitor-trace=<tbnl monitor trace file|example: \"trace.txt\" or a FIFO connected with a TBNL session>]+"
                                         "[--getinfo-trace=<tbnl getinfo trace file|example: \"trace.txt\" or a FIFO connected with a TBNL session>]+"
                                         "[--adb-cmd=<the adb cmd|default: adb>]"
                                         "[--adb-opts=<options to adb|default: -a>]"
                                         "[--target-app=<target app|example: com.example.app>]"
                                         ])
                           (str/join "\n\t" ["--cmd=usage"])
                           ]
                          ))))
   })

(def ^:dynamic ^:private *opts*)

(defn -main
  ""
  [& args]
  (binding [
            *opts*
            ;; https://github.com/amalloy/clojopts
            (clojopts "tbnl-mastermind" args
                      (with-arg cmd "command to execute" :type :str :default "usage")
                      (optional-arg getinfo-trace "getinfo trace filename" :type :file :group :map)
                      (optional-arg monitor-trace "monitor trace filename" :type :file :group :map)
                      (optional-arg output-name-root "output file name (root part)" :type :str :default "output")
                      (optional-arg adb-cmd "the adb command" :type :str :default "adb")
                      (optional-arg adb-opts "ADB connection options" :type :str :default "-a")
                      (optional-arg target-app "target app's package name" :type :str)
                      (optional-arg verbose "being verbose?" :type :boolean :default false)
                      )
            ]
    (let [cmd (:cmd *opts*)]
      (if cmd
        (let [cmd (get *cmds* (-> cmd str/lower-case keyword))]
          (if cmd
            (apply cmd [])
            (apply (:usage *cmds*) [])))        
        (apply (:usage *cmds*) []))))
  (shutdown-agents) ; reason: http://clojuredocs.org/clojure_core/clojure.java.shell/sh
  )
