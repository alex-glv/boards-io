(ns boards-io.router
  (:require
   [cljs.core.async :as async]
   [om.next :as om]
   [bidi.bidi :as b]))

(def router
  [""
   {"/" :boards
    "/boards/" {[:board/by-id "/board"] :columns}
    "/auth" :boards}])
