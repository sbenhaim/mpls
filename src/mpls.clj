(ns mpls
  (:gen-class
   :name mpls
   :init init
   :post-init post-init
   :constructors {[] []
                  [int] []
                  [int int] []
                  [int int int] []}
   :extends com.cycling74.max.MaxObject)
  (:require [clojure.tools.nrepl.server :as server]
            [clojure.string :as s]
            [clojure.core.match :refer [match]]
            [cider.nrepl :refer [cider-nrepl-handler]])
  (:import [com.cycling74.max Atom]))

(declare mpls max-box max-patcher max-window port)

(def user-ns (atom nil))

(defn hello-max-clj! []
  (reset! user-ns *ns*))

(defn call-user-fn [sym & args]
  (if-let [f (resolve (symbol (str @user-ns "/" sym)))]
    (apply f args)
    (println (str @user-ns "/" sym) " unimplemented.")))


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
     (defonce mpls this)
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

(defn mnew [name x y & args]
  (.newDefault max-patcher x y name (apply matoms args)))
