(ns boards-io.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [boards-io.parser :as parser]
            [boards-io.transit :as transit]
            [om.dom :as dom]
            [om.util :as util]
            [goog.log :as glog]
            [boards-io.logger :as l]
            [cljs.core.async :as async]
            [bidi.bidi :as b] 
            [clojure.string :as string]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [boards-io.router :as router]
            [boards-io.navigation :as nav]
            [boards-io.components :as c]
            [boards-io.handlers :as h])
  (:import goog.History ))


(enable-console-print!)

(defonce state (atom {:app/route []}))

(def reconciler
  (om/reconciler
   {:state state
    :parser (om/parser {:read parser/read :mutate parser/mutate})
    :normalize true
    :id-key :db/id
    :merge  (fn [r s n q]
             {:keys [:route/data]
              :next (let [route (-> s :app/route first)
                          cmpn (get c/route->component route) 
                          cur-rd (get s :route/data)
                          new-rd (merge cur-rd (om/tree->db cmpn (get n route) true))]
                      (assoc-in s [:route/data route] new-rd))})
    :send (transit/transit-post "/api")}))

(def env {:reconciler reconciler
          :matcher (partial b/match-route router/router)
          :query-root (c/get-root-query)
          })

(defui Root
  static om/IQuery
  (query [this]
         `[:app/route
           ~(c/get-root-query)])

  Object
  (componentDidMount
   [this]
   (nav/wire-up (nav/new-history) #(h/change-route! (assoc env :this this) %)))

  (render [this]
          (let [{:keys [app/route route/data app/local-state]} (om/props this)
                pr (first route)
                comp-data (if (not= nil pr)
                            (let [component ((c/route->factory pr) (get data pr))]
                              component))]
            comp-data)))

(om/add-root! reconciler Root (js/document.getElementById "app"))
