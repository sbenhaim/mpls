(ns com.anthropology-music.clj
  (:gen-class
   :name clj
   :init init
   :post-init post-init
   :constructors {[] []
                  [int] []
                  [int int] []
                  [int int int] []}
   :extends com.cycling74.max.MaxObject)
  (:require [clojure.tools.nrepl.server :as server]
            [clojure.string :as s]
            [clojure.core.async :refer [go alts!! timeout chan >!]]
            [clojure.core.match :refer [match]]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [overtone.music.pitch :refer :all])
  (:import [com.cycling74.max Atom]))

(declare this max-box max-patcher max-window port)

(def user-ns (atom nil))

(defn hello-max-clj! []
  (reset! user-ns *ns*))

(defn call-user-fn [sym & args]
  (if-let [f (resolve (symbol (str @user-ns "/" sym)))]
    (apply f args)
    (println (str @user-ns "/" sym) " unimplemented.\n\nCall (hello-max-clj!)")))


(def msg-chan (chan))

(defn matom [arg]
  (Atom/newAtom arg))

(defn matoms [& args]
  (if (or (nil? args) (empty? args)) nil
      (into-array Atom (map matom args))))

(defn matom-> [^Atom a]
  (cond (nil? a) nil
        (.isInt a) (.getInt a)
        (.isFloat a) (.getFloat a)
        (.isString a) (.getString a)
        :else nil))

(defn matoms-> [as]
  (map matom-> as))

(defn start-nrepl [port]
  (defonce server (server/start-server :port port :handler cider-nrepl-handler))
  (println "nrepl server running on port " port))

(defn -init
  ([] (-init 51580))
  ([_ _] (-init))
  ([port _ _] (-init port))
  ([port]
   (defonce port port)
     [[] nil]))

(defn -post-init
  ([this] (-post-init this 1 1))
  ([this _] (-post-init this))
  ([this _ in out] (-post-init this in out))
  ([this in out] 
     (.declareIO this in out)
     (defonce this this)
     (defonce max-box (.getMaxBox this))
     (defonce max-patcher (.getPatcher max-box))
     (defonce max-window (.getWindow max-patcher))))

(defn -notifyDeleted [this]
  (try
    (call-user-fn 'shutdown))
  (println "nrepl shutting down")
  (server/stop-server server))

(defn -bang [this]
  (call-user-fn 'bang))

(defn -inlet-int [this i]
  (call-user-fn 'int-msg i))

(defn -inlet-Atom<> [this as]
  (call-user-fn 'list-msg (matoms-> as)))

(defn -inlet-float [this f]
  (call-user-fn 'float-msg f))

(defn -anything [this ^String message ^Atom args]
  (let [args (vec (cons message (matoms-> args)))]
    (match args
           ["nrepl" "start"] (start-nrepl port)
           ["nrepl" "start" port] (start-nrepl port)
           :else (call-user-fn 'msg ))))

(defn msend [object message & args]
  (.send object message (apply matoms args)))

(defn lsend [object k message & args]
  (apply msend (object k) message args))

(defn lchildren [o]
  (msend (o :path) "getchildren"))

(defn mnew [name x y & args]
  (.newDefault max-patcher x y name (apply matoms args)))

(defn lnew [& path]
  (let [lp (apply mnew "live.path" 200 100 path)
        lo (mnew "live.object" 200 140)]
    (.setName lp (str path " path"))
    (.setName lo (str path " object"))
    (.connect max-patcher lp 0 lo 1)
    (.connect max-patcher lo 0 max-box 0)
    (fn [msg & args]
      (condp = msg
        :path lp
        :obj lo
        "goto" (apply msend lp "goto" (mapcat #(s/split % #" ") args))
        "getchildren" (msend lp "getchildren")
        "get" (do
                (apply msend lo msg args)
                (let [[v c] (alts!! [msg-chan (timeout 100)])]
                  v))
        (do
          (apply msend lo msg args))))))





























