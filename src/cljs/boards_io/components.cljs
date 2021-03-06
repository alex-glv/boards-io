(ns boards-io.components
  (:require [bidi.bidi :as b]
            [boards-io.handlers :as h]
            [boards-io.router :as router]
            [boards-io.modals :as m]
            [boards-io.drawcanvas :as cnv]
            [om.dom :as dom]
            [goog.events :as events]
            [om.next :as om :refer-macros [defui ui]]
            [goog.log :as glog]
            [boards-io.logger :as l]))

(declare get-root-query ColumnItem)

(def modal (om/factory m/Modal {:keyfn :ref}))

(defui BoardItem
  static om/Ident
  (ident [_ item]
         [:board/by-id (:db/id item)])
  static om/IQuery
  (query [this]
         [:db/id :board/name :board/description])
  Object
  (render [this]         
          (let [{:keys [db/id board/name board/description]} (om/props this)]
            (dom/div 
             #js {:className "board-item" }
             (dom/a #js{:href (b/path-for router/router :columns :board/by-id id)} name)
             
             ))))

(def board-item (om/factory BoardItem {:keyfn :db/id}))


(defui BoardList
  static om/IQuery
  (query [this]
         `[{:board/list ~(om/get-query BoardItem)}
           [:app/local-state _]])
  
  Object
  (render [this]
          (dom/div #js {:className "board-list"}
                   [(dom/div #js {:key "board-list-div1"}
                             (apply
                              dom/div #js {:key "board-list-div1"}
                              (-> (map #(board-item %) (:board/list (om/props this)))
                                  vec)))
                    (let [{:keys [app/local-state]} (om/props this)]
                      (m/overlay-handler
                       {:title "New board"
                        :placement "right"
                        :show (= 1 (-> local-state :board/new-board-modal :state))
                        :hide-fn #(h/modal-close {:reconciler this :ref :board/new-board-modal} )
                        :show-fn #(h/modal-open {:reconciler this :ref :board/new-board-modal} )
                        :id "new-board-mod"}
                       (m/new-board-form {:root this})))
                    ])))

(defui ColumnTask
  static om/Ident
  (ident [_ item]
         [:task/by-id (:db/id item)])
  static om/IQuery
  (query [this]
         [:db/id :task/name :task/order])
  Object
  (componentWillUnmount [this])
  
  (render [this]
          (let [is-moving? (:moving (om/props this))
                task (om/props this)
                task-item-m (clj->js
                             (cond-> {:className "board-column-task-item"
                                      :draggable "true"
                                      :key (:db/id task)
                                      :style #js {:order (:task/order task) }
                                      :onDrop (fn [e]
                                                (h/drag-end-task {:reconciler (om/get-reconciler this)}))
                                      :onDragStart (fn [e]
                                                     (let [ ;e (.-nativeEvent e)
                                                           _ (.setData (.-dataTransfer e) "text/plain" "")
                                        ;_ (.setDragImage (.-dataTransfer e) (cnv/get-image) 10 10)
                                                           _ (aset (.-dataTransfer e) "dropEffect" "move")
                                                           _ (aset (.-dataTransfer e) "effectAllowed" "move")
                                                           drag-data-map {:component this
                                                                          :reconciler (om/get-reconciler this)
                                                                          :entity :task/moving
                                                                          :ident {:task-id (-> (:db/id task))}}]
                                                       #_(aset (.-target e) "style" "transform: rotate(2deg);")
                                                       (h/drag-start drag-data-map)
                                                       (.stopPropagation e)))
                                      :onDragOver (fn [e]
                                                    (let [height (-> e (.-nativeEvent ) (.-target) (.-clientHeight ))
                                                          offset (-> e (.-nativeEvent ) (.-offsetY))
                                                          dragbuf (/ height 10)
                                                          dir (if (> (- height offset) dragbuf) :top :bottom)]
                                                      (h/update-order {:reconciler (om/get-reconciler this)
                                                                       :component this
                                                                       :entity :target-task-id
                                                                       :extra {:direction dir}
                                                                       :entity-id (:db/id task)})
                                                      (.stopPropagation e)
                                                      (.preventDefault e)))}
                               is-moving? (update :className #(str % " moving"))))]
            (if (not= nil (:task/order task))
              (dom/div task-item-m (:task/name (om/props this)))))))

(def column-task (om/factory ColumnTask {:keyfn :db/id}))

(defui ColumnItem
  static om/Ident
  (ident [_ item]
         [:column/by-id (-> item :db/id)])
  
  static om/IQuery
  (query [this]
         `[:db/id :column/name :column/order {:column/board ~(om/get-query BoardItem)}
           {:column/tasks ~(om/get-query ColumnTask)}
           [:app/local-state _]])
  

  Object
  (render [this]
          (let [is-moving? (-> this om/props :moving)
                local-state (:app/local-state (om/props this))
                mov-task-id (:task/moving (om/props this))
                class-name (str "board-column " (if is-moving? "moving" ""))
                style #js {:order (-> this om/props :column/order) }
                column-id (:db/id (om/props this))
                drag-data-map {:component this
                               :reconciler (om/get-reconciler this)
                               :entity :column/moving
                               :ident {:column-id column-id}}
                column-tasks (mapv #(cond-> %
                                      (and (= (:db/id %) mov-task-id))
                                      (assoc :moving true)) (:column/tasks (om/props this)))
                js-map (cond-> {:className class-name
                                :key (str "item-" column-id)
                                :style style
                                :draggable "true"
                                :onDragStart (fn [e]
                                               (let [e (.-nativeEvent e)
                                                     _ (.setData (.-dataTransfer e) "text/plain" "")
                                        ;_ (.setDragImage (.-dataTransfer e) (cnv/get-image) 10 10)
                                                     _ (aset (.-dataTransfer e) "dropEffect" "move")
                                        ;                                                     _ (aset (.-dataTransfer e) "effectAllowed" "move")
                                                     ]
                                                 (h/drag-start drag-data-map)))
                                :onDrop (fn [e] )
                                :onDragOver (fn [e]
                                              #_(.stopPropagation e)
                                              (.preventDefault e))
                                :onDragEnd (fn [e] 
                                             (.preventDefault e)
                                             (h/drag-end-task {:reconciler (om/get-reconciler this)})
                                             (h/drag-end-column drag-data-map))}
                         (not is-moving?)
                         (assoc :onDragEnter
                                (fn [e]
                                  (h/update-order {:reconciler (om/get-reconciler this) :component this :entity :target-column-id :entity-id column-id})
                                  (.preventDefault e))))]
            (dom/div (clj->js js-map) 
                     [(dom/div #js {:className "board-column-title" :key (str "item-title-" column-id)} (str (:column/name (om/props this))))
                      (dom/div #js {:className "board-column-tasks" :key (str "board-column-tasks-" column-id)}
                               (mapv column-task column-tasks))
                      
                      (dom/div
                       #js {:className "board-column-new-item" :key (str "new-item-div-" column-id)}
                       (m/overlay-handler
                        {:title "New task"
                         :placement "right"
                         :show (= 1 (-> local-state :column/new-task-modal :state))
                         :hide-fn #(h/modal-close {:reconciler this :ref :column/new-task-modal} )
                         :show-fn #(h/modal-open {:reconciler this :ref :column/new-task-modal
                                                  :ident {:column-id column-id}}  )
                         :id "new-task-mod"}
                        (m/new-task-form {:root this
                                          :extras (-> local-state :field-idents :column/new-task-modal)})
                        ))]))))

(def column-item (om/factory ColumnItem {:keyfn :db/id}))

(defui ColumnList
  static om/IQuery
  (query [this]
         `[{:column/list ~(om/get-query ColumnItem)}
           [:app/local-state _]
           :app/route
           {:board/by-id ~(om/get-query BoardItem)}])

  Object
  (render
   [this]
   (let [
         {:keys [app/local-state board/by-id]} (om/props this)
         {:keys [board/name]} (first by-id)
         board-id (js/parseInt (-> (om/props this) :app/route second :board/by-id))
         proc-col-item (fn [col]
                         (let [mov-col-id (-> local-state :field-idents :column/moving :column-id)
                               mov-task-id (-> local-state :field-idents :task/moving :task-id)
                               column-moving (-> local-state :column/moving :state)
                               task-moving (-> local-state :task/moving :state)
                               col (cond-> col
                                     (and (= column-moving :drag-start) (= (:db/id col) mov-col-id))
                                     (assoc :moving true)
                                     (= :drag-start task-moving) (assoc :task/moving mov-task-id))]
                           (column-item col)))]
     (dom/div
      nil
      (dom/h5 #js {:className "board-column-board-name"} name)
      (dom/div #js {:className "board-wrap" :key (str "board-wrap-" board-id)}
               (merge
                (into [] (map proc-col-item (:column/list (om/props this))))
                (dom/div
                 #js {:style #js {:order 9999}}
                 (m/overlay-handler
                  {:title "New column"
                   :placement "bottom"
                   :show (= 1 (-> local-state :column/new-column-modal :state))
                   :hide-fn #(h/modal-close {:reconciler this :ref :column/new-column-modal} )
                   :show-fn #(h/modal-open {:reconciler this :ref :column/new-column-modal
                                            :ident {:board/by-id board-id}}  )
                   :id "new-col-mod"}
                  (m/new-column-form
                   {:root this
                    :extras (-> local-state :field-idents :column/new-column-modal)})))
                ))))))

(defui AuthHeader
  static om/IQuery
  (query [this]
         [{:oauth/user [:user/email :user/userid :user/token]}])

  Object
  (render [this]
          (let [{:keys [user/email user/userid user/token] :as lp} (get (om/props this) :oauth/user)]
            (if token
              (dom/div nil "")
              (dom/div #js {:className "navbar-auth-blob"}
                       (dom/a #js {:href "/oauth" :target "_self"} "Google"))))))


(defui Header
  Object
  (render
   [this]
   (dom/nav #js {:className "navbar navbar-default" :id "header"}
            (dom/div #js {:className "container-fluid"}
                     [(dom/div #js {:className "col-md-10" :key "nav-col-10"}
                               [(dom/div #js {:className "navbar-header" :key "navbar-head"}
                                         (dom/a #js {:className "navbar-brand" :href "/"} "treesie.io"))
                                (dom/div #js {:className  "collapse navbar-collapse" :key "navbar-collapse"}
                                                                    (dom/ul #js {:className "nav navbar-nav"}
                                                                            (dom/li #js {:id "boards-list"} 
                                                                                    (dom/a #js {:href "#"} ""))))])
]))))

(def route->component
  {:columns ColumnList
   :boards  BoardList
   :auth BoardList})

(def route->factory
  (zipmap (keys route->component)
          (map (fn [c] (om/factory c {:keyfn #(str (-> % keys first))})) (vals route->component))))


(defn get-root-query []  
  {:route/data (mapv hash-map (keys route->component)
                     (map om/get-query (vals route->component)))})
