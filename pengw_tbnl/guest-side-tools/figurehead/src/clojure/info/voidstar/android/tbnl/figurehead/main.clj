(ns info.voidstar.android.tbnl.figurehead.main
  (:use [neko.activity :only [defactivity set-content-view!]]
        [neko.threading :only [on-ui]]
        [neko.ui :only [make-ui]]))

(defactivity info.voidstar.android.tbnl.figurehead.MainActivity
  :def a
  :on-create
  (fn [this bundle]
    (on-ui
     (set-content-view! this
      (make-ui [:linear-layout {}
                [:text-view {:text "Hello from Clojure!"}]])))))
