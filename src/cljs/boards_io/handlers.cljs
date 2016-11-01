(ns boards-io.handlers
  (:require [om.next :as om]
            [goog.dom :as gdom]
            [goog.dom.forms :as forms]))

(defn change-route! [{:keys [reconciler matcher query-root this]} route]
  (let [{:keys [handler route-params]} (matcher route)
        ;;root (:root @(:state reconciler))
        ]
    (om/transact! this `[(change/route! {:route [~handler ~route-params]})
                         ~query-root
                         ])))


(defn new-board [{:keys [reconciler]} ]
  (om/transact! reconciler '[(change/toggle-field! {:field :board/new-board-modal :field-state 1})]))

(defn modal-close [{:keys [reconciler]} ref]
  (om/transact! reconciler `[(change/toggle-field! {:field ~ref :field-state 0})]))

(defn new-board-save [{:keys [reconciler root-query save-btn-field]}]
  (println "Root-query " root-query save-btn-field)
  (let [form (gdom/getElement "new-board-form")
        title (forms/getValueByName form "board-title")
        description (forms/getValueByName form "board-description")]
    (om/transact! reconciler
                  `[(change/toggle-field! {:field ~save-btn-field :field-state :off})
                    (save/new-board! {:title ~title :description ~description})
                    ~root-query
                    (change/toggle-field! {:field ~save-btn-field :field-state :on})
                    ])))
