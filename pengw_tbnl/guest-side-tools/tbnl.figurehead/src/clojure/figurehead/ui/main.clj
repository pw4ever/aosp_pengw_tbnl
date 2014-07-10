(ns figurehead.ui.main
  (:use [neko.activity :only [defactivity set-content-view!]]
        [neko.threading :only [on-ui]]
        [neko.ui :only [make-ui]]
        [neko.find-view :only [find-view]]
        [neko.notify :only [toast notification fire cancel]]))

(def main-activity-layout [:linear-layout {:orientation :vertical
                                           :layout-width :fill_parent
                                           :id-holder true}
                           [:linear-layout {:orientation :horizontal
                                            :layout-width :fill_parent}
                            [:edit-text {:id ::server-ip
                                         :layout-weight 5
                                         :hint "mastermind IP address"}]
                            [:edit-text {:id ::server-port
                                         :layout-weight 1
                                         :hint "port"}]]
                           [:button {
                                     :text "connect"
                                     :layout-gravity :right
                                     :on-click (fn [_] (toast "Clicked!"))}]])

(defactivity figurehead.ui.MainActivity
  :def a
  :key main
  :on-create
  (fn [this saved-instance-state]
    (on-ui
     (set-content-view! this
                        (make-ui main-activity-layout)))))
