(defproject com.anthropology-music/max-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.cycling74/max "6.1.0"]
                 [org.clojure/tools.nrepl "0.2.2"]]
  :aot [com.anthropology-music.clj-connection
        com.anthropology-music.clj])


