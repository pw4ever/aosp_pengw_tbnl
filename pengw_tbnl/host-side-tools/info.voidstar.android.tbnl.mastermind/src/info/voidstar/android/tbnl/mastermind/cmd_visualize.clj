(ns info.voidstar.android.tbnl.mastermind.cmd-visualize
  (:use
   info.voidstar.android.tbnl.mastermind.util-trace
   dorothy.core
   [clojure.core.async :as async
    :only [chan 
           close!
           go thread 
           >! <! >!! <!!
           alts! alts!!
           timeout]]
   [clojure.java.shell :only [sh]]
   )
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   )
  (:gen-class))

(declare visualize visualize-monitor-trace)

(defn cmd-visualize [opts]
  (let [
        monitor-trace (:monitor-trace opts) ; a list of input files
        output-name-root (if (:output-name-root opts) (:output-name-root opts) "output")
        verbose? (:verbose opts)
        ]
    (work-on-monitor-trace monitor-trace
                           (fn [model news news-count]
                             ;;; news-callback
                             (let [g (atom [])
                                   packages (-> model (get :packages) keys)
                                   cur (atom 0)]
                               (doseq [package packages]
                                 (let [pkg (get-in model [:packages package])]
                                   (swap! cur inc)
                                   ;; subgraphs by packages
                                   (swap! g conj
                                          (subgraph package
                                                    (apply vector 
                                                           {:label (:name pkg)}
                                                           (node-attrs {:style :filled
                                                                        :color (str/join " "
                                                                                         [(double (* @cur (/ 1 (count packages))))
                                                                                          1
                                                                                          1])})
                                                           (->> (:activities pkg)
                                                                (map (fn [[id {:keys [name flags]}]]
                                                                       (vector
                                                                        id
                                                                        {
                                                                         :label name
                                                                         :shape 
                                                                         (cond
                                                                          (contains? flags
                                                                                     :category-home)
                                                                          :house

                                                                          (contains? flags
                                                                                     :category-launcher)
                                                                          :box

                                                                          :other
                                                                          :ellipse
                                                                          )
                                                                         })))))))))
                               ;;; edges
                               (let [edges (get model :edges)]
                                 (doseq [from (keys edges)
                                         to (-> edges (get-in [from :to]) keys)]
                                   (apply swap! g conj (repeat
                                                        (get-in edges [from :to to])
                                                        [from to]))))
                               
                               ;;; draw the whole graph
                               (let [d (dot (digraph @g))
                                     fname-root (str output-name-root "_" news-count)]
                                 (spit (str fname-root ".dot") d)
                                 (save! d (str fname-root ".pdf") {:format :pdf}) 
                                 (save! d (str fname-root ".png") {:format :png}))))
                           verbose?)))


