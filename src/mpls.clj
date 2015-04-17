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

(defn hello-mpls!
  "Registers calling namespace as the place to look for inlet functions `bang', `int-msg', etc."
  []
  (reset! user-ns *ns*)
  (println (str "Looking for functions in `" (str *ns*) "'")))

(defn call-user-fn [sym & args]
  (if-let [f (resolve (symbol (str @user-ns "/" sym)))]
    (let [inlet (.getInlet mpls)]
      (apply f inlet args))
    (println (str @user-ns "/" sym) " unimplemented.")))


(defn matom
  "Converts Clojure int/float/string to com.cycling74.max.Atom-wrapped value for use with the java API."
  [arg]
  (Atom/newAtom arg))

(defn matoms
  "Converts heterogeneous vec of Clojure values into an array of Atoms."
  [arg-vec]
  (if (or (nil? arg-vec) (empty? arg-vec)) nil
      (into-array Atom (map matom arg-vec))))

(defn ms [& args]
  (matoms args))

(defn matom->
  "Converts an Atom to a Clojure value."
  [^Atom a]
  (cond (nil? a) nil
        (.isInt a) (.getInt a)
        (.isFloat a) (.getFloat a)
        (.isString a) (.getString a)
        :else nil))

(defn matoms->
  "Converts an array of Atoms into a vec of Clojure values."
  [as]
  (mapv matom-> as))

(defn start-nrepl [port]
  (def server (server/start-server :port port :handler cider-nrepl-handler))
  (println "nrepl server running on port " (:port server)))

(defn -init
  ([] (-init 0))
  ([_ _] (-init))
  ([port _ _] (-init port))
  ([port]
   (def port port)
     [[] nil]))

(defn -post-init
  ([this] (-post-init this 5 5))
  ([this _] (-post-init this))
  ([this _ in out] (-post-init this in out))
  ([this in out]
     (.declareIO this in out)
     (def mpls this)
     (def box (.getMaxBox this))
     (def patcher (.getPatcher box))
     (def window (.getWindow patcher))))

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
  (if (string? message)
    (.send object message (matoms args))
    (.send object message)))

(defmacro defer
  "Asynchronously execute `body' in the low-priority thread. Returns nil immediately."
  [& body]
  `(MaxSystem/defer (reify Executable
                      (execute [this]
                        ~@body))))

(defmacro defer-sync
  "Execute `body' in the low-priority thread. Block until it completes, and return the result."
  [& body]
  `(let [p# (promise)]
     (MaxSystem/defer (reify Executable
                        (execute [this]
                          (deliver p# (do ~@body)))))
     @p#))

(defn mnew
  "Create a new Max object of type `name' at coords `x',`y' with args `args'."
  [name x y & args]
  (defer-sync (.newDefault patcher x y name (matoms args))))

(defn connect
  "Connect `outlet' of MaxBox object `o1' to `inlet' of MaxBox object `o2'."
  [o1 outlet o2 inlet]
  (.connect patcher o1 outlet o2 inlet))

(defn disconnect
  "Disconnect `outlet'  of MaxBox object `o1' to `inlet' of MaxBox object `o2'."
  [o1 outlet o2 inlet]
  (defer (.disconnect patcher o1 outlet o2 inlet)))

(defn return [x]
  (if (coll? x) x [x]))

(defn dict-dict
  "Convert Clojure dictionary to serialized dictionary, suitable input for Max's [dict.deserialize] object."
  [d]
  (s/join " " (map (fn [[k v]] (str (name k) " : " (s/join " " (return v)))) d)))

(defn out
  "Sends `(matoms what)' to `n'th outlet. "
  [n & what] (.outlet mpls n (matoms what)))

(defn parse
  "Parse space-delimited string into array of Atoms."
  [s]
  (Atom/parse s))

(defn post
  "Print message to Max window."
  [msg]
  (MaxSystem/post msg))

(defn error
  "Print error to Max window."
  [msg]
  (MaxSystem/error msg))

(defn mremove
  "Remove MaxBox `what' from patcher."
  [what]
  (defer (.remove what)))
