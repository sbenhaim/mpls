(defproject com.anthropology-music/max-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha3"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cycling74/max "6.1.0"]
                 [cider/cider-nrepl "0.8.1"]
                 [org.clojure/core.match "0.2.1"]
                 [org.clojure/tools.nrepl "0.2.5"]
                 [overtone "0.9.1"]]
  :aot [com.anthropology-music.clj]
  :jar-name "max-clj-small.jar"
  :uberjar-name "max-clj.jar")
