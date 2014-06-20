(ns info.voidstar.android.tbnl.mastermind.util-trace
  (:use
   [clojure.set :only [union]]
   [clojure.core.async :as async
    :only [chan 
           close!
           go thread 
           >! <! >!! <!!
           alts! alts!!
           timeout]]
   )
  (:require 
   [clojure.string :as str]
   [clojure.java.io :as io]
   )
  (:gen-class))

(declare println-debug trace-tag?
         compname-str-2-kwd compname-kwd-2-str)

(defn println-debug [msg verbose?]
  (when verbose?
    (binding [*out* *err*]
      (println msg))))

(defn trace-tag? [line tag]
  (re-matches (re-pattern (str "^<" tag ">.*$")) line))

(defmacro ^:private generate-tag-predicates []
  "NOTE: need to match TBNL tags"
  `(do ~@(map (fn [[type tag]]
                `(defn- ~(symbol (str (name type) "-trace?")) [line#]
                   (trace-tag? line# ~tag)))
              {
               :activity-controller "ActivityController"
               :normal "*normal*"
               :over "*over*"
               :error "*error*"
               })))

(generate-tag-predicates)

(defn remove-trace-tag [line]
  (str/replace-first line #"^<[^>]+>" ""))

(defn parse-trace-line [line]
  (when (activity-controller-trace? line)
    (into {}
          (map (fn [[_ k v]] (vector (keyword k) v)) 
               (re-seq #"(?:([^|=]+)=([^|=]+))" (remove-trace-tag line)))))  )

(defmacro ^:private convert-parsed-line-entry [parsed-line key transform]
  `(let [parsed-line# ~parsed-line
         key# ~key
         transform# ~transform
         transformed-key# (-> @parsed-line# key# transform#)]
     (swap! parsed-line# assoc key# transformed-key#)))

(defn parse-activity-controller-trace-line [line]
  (let [parsed-line (atom (parse-trace-line line))]
    ;; further parsing
    (convert-parsed-line-entry parsed-line :action keyword)
    (case (-> @parsed-line :action)
      :start 
      (do
        ;; example: timestamp=1402261579000|action=start|package=com.android.launcher|intent-action=android.intent.action.MAIN|intent-component=com.android.launcher/com.android.launcher2.Launcher|intent-category=android.intent.category.HOME|intent-data=null|intent-extras=null|intent-flags=00000000
        (convert-parsed-line-entry parsed-line :session keyword)
        (convert-parsed-line-entry parsed-line :timestamp (fn [num] (Long. num)))
        (convert-parsed-line-entry parsed-line :intent-action compname-str-2-kwd)
        (convert-parsed-line-entry parsed-line :package compname-str-2-kwd)
        (convert-parsed-line-entry parsed-line :intent-component compname-str-2-kwd)
        (convert-parsed-line-entry parsed-line :intent-category
                                   (fn [category]
                                     (if (= category "null")
                                       nil
                                       (do
                                         ;; example: android.intent.category.HOME;android.intent.category.APP_BROWSER
                                         (set (map compname-str-2-kwd
                                                   (str/split category #";")))))))
        (convert-parsed-line-entry parsed-line :intent-data
                                   (fn [data]
                                     (if (= data "null")
                                       nil
                                       (do
                                         ;; TODO: what is the form?
                                         data
                                         ))))
        (convert-parsed-line-entry parsed-line :intent-extras
                                   (fn [extras]
                                     (if (= extras "null")
                                       nil
                                       (do
                                         ;; TODO: what is the form?
                                         extras
                                         ))))
        (convert-parsed-line-entry parsed-line :intent-flags 
                                   (fn [flag]
                                     (Long/parseUnsignedLong flag 16))))

      :resume
      (do
        ;; example: timestamp=1402261890000|action=resume|package=com.android.mms
        (convert-parsed-line-entry parsed-line :session keyword)
        (convert-parsed-line-entry parsed-line :timestamp (fn [num] (Long. num)))
        (convert-parsed-line-entry parsed-line :package compname-str-2-kwd))

      :crashed
      (do
        (convert-parsed-line-entry parsed-line :session keyword)
        (convert-parsed-line-entry parsed-line :timestamp (fn [num] (Long. num)))
        (convert-parsed-line-entry parsed-line :packages (fn [package]
                                                           (if (= package "")
                                                             nil
                                                             (do
                                                               (set (map compname-str-2-kwd
                                                                         (str/split package #";"))))))))

      ;; defensive programming: should not reach here
      nil)
    @parsed-line))

(defn compname-str-2-kwd [compname]
  (when compname
    (-> compname (str/replace #"/" "+") (str/replace #"\." "*") keyword)))

(defn compname-kwd-2-str [compname]
  (when compname
    (-> compname name (str/replace #"\+" "/") (str/replace #"\*" "."))))

(defn- create-input-chans [traces]
  (let [input-chans (atom {})]
    (doseq [trace traces]
      (let [c (chan)
            session (keyword (gensym "input-chans"))]
        (swap! input-chans assoc session c)
        (go
          (let [rdr (io/reader trace)]
            (loop [news (.readLine rdr)]
              (when news
                (>! c [session news])
                (recur (.readLine rdr)))))
          ;; need to remove itself from input-chans
          (swap! input-chans dissoc session)
          (close! c))))
    input-chans))

(defn- init-model []
  "create a new model

{
;; package - activity forms an hierarchy
:packages
{
pkg1
{
:name pkg1
:flags #{:category-home}
:activities {
 :a1 {:name \"a1\" :flags #{:category-home}}
 :a2 {:name \"a2\" :flags #{}} 
 ...}
}
pkg2
{
:name pkg2
:flags #{}
:activities {...}
}

:edges
{
:act1 {:act2 3 :act3 1}
...
}

}"
  {:packages {}
   :edges {}})

(defn- update-model! [model state news]
  (let [
        parsed-news (parse-activity-controller-trace-line news)
        session (-> parsed-news :session)
        action (-> parsed-news :action)
        package-kwd (-> parsed-news :package)
        package-str (compname-kwd-2-str package-kwd)
        ]

    ;; stack is per session-package
    ;; (when (nil? (get @state session))
    ;;   (swap! state assoc-in [session]
    ;;          {:current {
    ;;                     :package nil
    ;;                     :component nil 
    ;;                     }
    ;;           :packages {}}))

    ; ensure the stack is a vector
    (swap! state update-in [session :packages package-kwd]
           vec)

    (let [
          prev-current (get-in @state [session :current])
          prev-current-package (:package prev-current)
          prev-current-component (:component prev-current)
          stack (get-in @state [session :packages package-kwd])
          ]

      ;; update state per-session current package pointer
      (swap! state assoc-in [session :current :package]
             package-kwd)

      (case action
        :start
        (let [
              intent-component (-> parsed-news :intent-component)
              prev-intent-component (peek stack)
              intent-action (-> parsed-news :intent-action)
              intent-category (-> parsed-news :intent-category)
              ]

          (swap! state assoc-in [session :current :component]
                 intent-component)

          (cond  ;; based on category

           (contains? intent-category
                      (compname-str-2-kwd "android.intent.category.HOME"))
           (when intent-component

             ;; update model>packages flags
             (swap! model assoc-in [:packages package-kwd :name]
                    package-str)
             (swap! model update-in [:packages package-kwd :flags]
                    union
                    #{:category-home})
             (swap! model assoc-in [:packages package-kwd :activities intent-component :name]
                    (compname-kwd-2-str intent-component))
             (swap! model update-in [:packages package-kwd :activities intent-component :flags]
                    union
                    #{:category-home})

             false ; no model update
             )


           :other ;; other cases
           (when intent-component

             ;; update per-session+package state when "new" or "not from launcher"
             (when (or (nil? prev-intent-component)
                       (not (contains? intent-category
                                       (compname-str-2-kwd "android.intent.category.LAUNCHER"))))
               (swap! state update-in [session :packages package-kwd]
                      conj
                      intent-component))

             ;; update model>packages
             (swap! model assoc-in [:packages package-kwd :name]
                    package-str)
             (swap! model update-in [:packages package-kwd :flags]
                    union
                    (cond
                     :default
                     nil
                     ))
             (swap! model assoc-in [:packages package-kwd :activities intent-component :name]
                    (compname-kwd-2-str intent-component))
             (swap! model update-in [:packages package-kwd :activities intent-component :flags]
                    union
                    (cond
                     (contains? intent-category 
                                (compname-str-2-kwd "android.intent.category.LAUNCHER"))
                     #{:category-launcher}

                     :default
                     nil
                     ))

             ;; update model>edges
             (when prev-current-component
               (swap! model assoc-in [:edges prev-current-component :name]
                      (compname-kwd-2-str prev-current-component))
               (swap! model update-in [:edges prev-current-component :to intent-component]
                      #(if (nil? %)
                         1
                         (inc %))))

             true ; model updated
             )))

        :resume  ; state is changed but no model update
        (when (peek stack)
          (when (= prev-current-package package-kwd)
            (swap! state update-in [session :packages package-kwd]
                   pop))
          (swap! state assoc-in [session :current :component]
                 (peek (get-in @state [session :packages package-kwd])))
          false ; no model update
          )

        :crashed
        (let [
              packages (-> parsed-news :packages)
              ]
          ;; these packages have crashed --> clear their stacks
          (doseq [package packages]
            (swap! state update-in [session :packages]
                   (dissoc package))))
        
        false ; no model update by default
        ))))

(defn work-on-monitor-trace [monitor-trace news-callback verbose?]
  "upon news arrival, news-callback will be called with [model news news-count]"
  (let [model (atom (init-model))
        state (atom {})
        input-chans (create-input-chans monitor-trace)
        news-count (atom 0)
        finished (chan)]
    (go
      (loop [input @input-chans]
        (when (not-empty input)
          (let [[[_session news] _] (alts! (vals input))]
            (when (and news (activity-controller-trace? news))
              (println-debug news verbose?)
              (when (update-model! model state news)
                (swap! news-count inc)
                (apply news-callback [@model news @news-count])
                )))
          (recur @input-chans)))
      (close! finished))
    ;; this block is deliberate
    (<!! finished)))
