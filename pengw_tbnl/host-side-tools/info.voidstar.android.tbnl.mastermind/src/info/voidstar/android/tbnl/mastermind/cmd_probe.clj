(ns info.voidstar.android.tbnl.mastermind.cmd-probe
  (:require 
   [clojure.string :as str]
   [clojure.core.async :as async]
   )
  (:gen-class))

(defn cmd-probe [opts])


;; (defn- visualize-getinfo-trace [trace-getinfo adb-path adb-opts pkgname output-name-root]
;;     (let [trace-lines (if trace-getinfo
;;                           (line-seq (io/reader trace-getinfo))
;;                           (let [result
;;                                 (sh 
;;                                  adb-path adb-opts "shell"
;;                                  "tbnl" "getinfo" pkgname)]
;;                             (str/split-lines (str (:out result) (:err result)))))]
;;       (visualize 
;;        (map #(parse-normal-trace-line %) 
;;             (retain-normal-or-over-trace trace-lines))
;;        nil ; for now, we do not have edges
;;        "getinfo"
;;        output-name-root
;;        )))


