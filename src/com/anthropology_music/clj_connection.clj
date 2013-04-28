(ns com.anthropology-music.clj-connection
  (:gen-class
   :name clj-connection
   :init init
   :post-init post-init
   :constructors {[] []
                  [int] []}
   :extends com.cycling74.max.MaxObject)
  (:require [clojure.tools.nrepl.server :as server]
            [clojure.reflect :as r])
  (:import [com.cycling74.max Atom]))

(declare this max-box max-patcher max-window)

(defn matom [arg]
  (Atom/newAtom arg))

(defn matoms [& args]
  (if (or (nil? args) (empty? args)) nil
      (into-array Atom (map matom args))))

(defn matom-> [^Atom a]
  (cond (.isInt a) (.getInt a)
        (.isFloat a) (.getFloat a)
        (.isString a) (.getString a)
        :else nil))

(defn matoms-> [as]
  (map matom-> as))

(defn -init
  ([] (-init 51580))
  ([port]
     (defonce server (server/start-server :port port))
     (println "nrepl server running on port " port)
     [[] nil]))

(defn -post-init [this] 
  (defonce this this)
  (defonce max-box (.getMaxBox this))
  (defonce max-patcher (.getPatcher max-box))
  (defonce max-window (.getWindow max-patcher)))

(defn -notifyDeleted [this]
  (println "nrepl shutting down")
  (server/stop-server server))

(defn msend [o m & as]
  (.send o m (apply matoms as)))

(defn lsend [o m k & as]
  (apply msend (o k) m as))

(defn lchildren [o]
  (msend (o :path) "getchildren"))

(defn mnew [name x y & args]
  (.newDefault max-patcher x y name (apply matoms args)))

(defn lnew [& path]
  (let [lp (apply mnew "live.path" 200 100 path)
        lo (mnew "live.object" 300 100)]
    (.setName lp (str path " path"))
    (.setName lo (str path " object"))
    (.connect max-patcher lp 0 lo 1)
    {:path lp
     :obj lo}))
