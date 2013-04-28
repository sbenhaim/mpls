(ns com.anthropology-music.max-clj
  (:gen-class
   :name clj
   :init init
   :post-init post-init
   :constructors {[] []
                  [int int] []}
   :state "dispatch"
   :extends com.cycling74.max.MaxObject)
  (:require [clojure.tools.nrepl.server :as server]
            [clojure.reflect :as r])
  (:import [com.cycling74.max MaxObject Atom]))

(declare bang int-msg float-msg list-msg msg)

(defn -init
  ([] (-init 51580))
  ([_ _] (-init))
  ([port _ _] (-init port))
  ([port]
     (defonce server (server/start-server :port port))
     (println "nrepl server running on port " port)
     [[] nil]))

(defn -post-init
  ([this] (-post-init this 1 1))
  ([this in out] 
     (.declareIO this in out)))

(defn -bang [this]
  (if (fn? bang)
    (bang this)
    (println "`bang' unimplemented")))

(defn -inlet-int [this i]
  (if (fn? int-msg)
    (int-msg this i)
    (println "`int-msg' unimplemented")))

(defn -inlet-Atom<> [this as]
  (if (fn? list-msg)
    (list-msg this (matoms-> as))
    (println "`list-msg' unimplemented")))

(defn -inlet-float [this f]
  (if (fn? float-msg)
    (float-msg this f)
    (println "`float-msg' unimplemented")))

(defn -anything [this ^String message ^Atom args]
  (if (fn? msg)
    (msg this message (matoms-> args))
    (println "`msg' unimplemented")))
