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
  (:import [com.cycling74.max MaxSystem Atom Executable]))

(declare mpls box patcher window port server)

(def user-ns
  "holds the ns that user functions (`bang', `msg' will be defined)"
  (atom nil))

(defn hello-mpls! []
  (reset! user-ns *ns*)
  (println "Looking for functions in" (str *ns*)))

(defn call-user-fn [sym & args]
  (if-let [f (resolve (symbol (str @user-ns "/" sym)))]
    (let [inlet (.getInlet mpls)]
      (apply f mpls inlet args))
    (println (str @user-ns "/" sym) " unimplemented.")))


(defn matom [arg]
  (Atom/newAtom arg))

(defn matoms [arg-vec]
  (if (or (nil? arg-vec) (empty? arg-vec)) nil
      (into-array Atom (map matom arg-vec))))

(defn matom-> [^Atom a]
  (cond (nil? a) nil
        (.isInt a) (.getInt a)
        (.isFloat a) (.getFloat a)
        (.isString a) (.getString a)
        :else nil))

(defn matoms-> [as]
  (mapv matom-> as))

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
     (defonce box (.getMaxBox this))
     (defonce patcher (.getPatcher box))
     (defonce window (.getWindow patcher))))

(defn -notifyDeleted [this]
  (try
    (call-user-fn 'shutdown))
  (when (bound? #'server)
    (do
      (println "nrepl shutting down")
      (server/stop-server server))))

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
           :else (call-user-fn 'msg args))))

(defn -dblclick [this]
  (call-user-fn 'dblclick))

(defn msend [object message & args]
  (.send object message (matoms args)))

(defmacro defer [& body]
  `(MaxSystem/defer (reify Executable
                      (execute [this]
                        ~@body))))

(defmacro defer-sync [body]
  `(let [p# (promise)]
     (MaxSystem/defer (reify Executable
                        (execute [this]
                          (deliver p# ~body))))
     @p#))

(defn mnew [name x y & args]
  (defer-sync (.newDefault patcher x y name (matoms args))))

(defn connect [o1 i1 o2 i2]
  (.connect patcher o1 i1 o2 i2))

(defn disconnect [o1 i1 o2 i2]
  (defer (.disconnect patcher o1 i1 o2 i2)))

(defn return [x]
  (if (coll? x) x [x]))

(defn dict-dict [d]
  (s/join " " (map (fn [[k v]] (str (name k) " : " (s/join " " (return v)))) d)))

(defn out
  ([what] (out 0 what))
  ([n what] (out mpls n what))
  ([who n what] (.outlet who n what)))

(defn parse [s]
  (Atom/parse s))

(defn post [msg]
  (MaxSystem/post msg))

(defn error [msg]
  (MaxSystem/error msg))

(defn ouch [msg]
  (MaxSystem/ouch msg))

(defn mremove [what]
  (defer (.remove what)))
