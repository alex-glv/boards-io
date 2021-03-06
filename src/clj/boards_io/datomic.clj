(ns boards-io.datomic)

(def schema-tx
  '[
    {:db/id #db/id[:db.part/db]
     :db/ident :user/email
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "User email"
     :db.install/_attribute :db.part/db}

    {:db/id #db/id[:db.part/db]
     :db/ident :user/token
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/unique :db.unique/identity
     :db/fulltext true
     :db/doc "Active token code"
     :db.install/_attribute :db.part/db}

    {:db/id #db/id[:db.part/db]
     :db/ident :user/userid
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/unique :db.unique/identity
     :db/fulltext true
     :db/doc "User id"
     :db.install/_attribute :db.part/db}

    {:db/id #db/id[:db.part/db]
     :db/ident :board/user
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/one
     :db/doc "Board owner"
     :db.install/_attribute :db.part/db}
    
    ;; board
    {:db/id #db/id[:db.part/db]
     :db/ident :board/name
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "A board name"
     :db.install/_attribute :db.part/db}

    {:db/id #db/id[:db.part/db]
     :db/ident :board/description
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "A board description"
     :db.install/_attribute :db.part/db}

    ;; column
    {:db/id #db/id[:db.part/db]
     :db/ident :column/name
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "A column name"
     :db.install/_attribute :db.part/db}
    
    {:db/id #db/id[:db.part/db]
     :db/ident :column/board
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "A column board"
     :db.install/_attribute :db.part/db}

    {:db/id #db/id[:db.part/db]
     :db/ident :column/order
     :db/valueType :db.type/long
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "A column order"
     :db.install/_attribute :db.part/db}

    {:db/id #db/id[:db.part/db]
     :db/ident :column/tasks
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/many
     :db/fulltext true
     :db/unique :db.unique/identity
     :db/doc "A column tasks"
     :db.install/_attribute :db.part/db}
    
    ;; task
    {:db/id #db/id[:db.part/db]
     :db/ident :task/name
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "A task name"
     :db.install/_attribute :db.part/db}

    {:db/id #db/id[:db.part/db]
     :db/ident :task/order
     :db/valueType :db.type/long
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "A task order"
     :db.install/_attribute :db.part/db}
    
])



(def initial-data [{:db/id #db/id[:db.part/user -987]
                    :user/email "test@123.abc"
                    :user/userid "123"
                    :user/token "yada-yada-token"}
                   
                   {:db/id #db/id[:db.part/user]
                    :board/name "Work"
                    :board/user #db/id[:db.part/user -987]
                    :board/description "Work board"}
                    
                   {:db/id #db/id[:db.part/user -100001]
                    :board/name "Personal"
                    :board/user #db/id[:db.part/user -987]
                    :board/description "Personal board"}

                   {:db/id #db/id[:db.part/user -20001]
                    :column/board #db/id[:db.part/user -100001]
                    :column/name "To-Doskis"
                    :column/order 2}

                   {:db/id #db/id[:db.part/user -20002]
                    :column/board #db/id[:db.part/user -100001]
                    :column/name "Doing"
                    :column/order 3}
                    
                   {:db/id #db/id[:db.part/user]
                    :column/board #db/id[:db.part/user -100001]
                    :column/name "Backlog"
                    :column/tasks [#db/id[:db.part/user -555]]
                    :column/order 4}

                   {:db/id #db/id[:db.part/user]
                    :column/board #db/id[:db.part/user -100001]
                    :column/name "Archived"
                    :column/tasks [#db/id[:db.part/user -666] #db/id[:db.part/user -777]]
                    :column/order 5}

                   {:db/id #db/id[:db.part/user -555]
                    :task/name "Do the laundry"
                    :task/order 1
                    }

                   {:db/id #db/id[:db.part/user -777]
                    :task/name "Take a shower"
                    :task/order 2
                    }

                   {:db/id #db/id[:db.part/user -666]
                    :task/name "Do some work"
                    :task/order 1
                    }
                    
                   ])
