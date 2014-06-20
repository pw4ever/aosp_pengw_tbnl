(defproject info.voidstar.android.tbnl/mastermind "0.5.0-SNAPSHOT"
  :description "the host side of TBNL; mastermind of driving the Android-side TBNL"
  :url "https://github.com/pw4ever/aosp_pengw_tbnl"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"] 
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [alembic "0.2.1"]
                 [clojopts "0.3.4"]
                 [dorothy "0.0.5"]
                 ]
  :main ^:skip-aot info.voidstar.android.tbnl.mastermind.core
  :target-path "target/%s"
  :profiles {
             :uberjar {:aot :all}
             :dev {:dependencies [
                                  ]}
             }
)
