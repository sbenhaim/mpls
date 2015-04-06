(defproject mpls "0.1.0-SNAPSHOT"
  :description "Live code Max and Max4Live with Clojure."
  :url "https://github.com/sbenhaim/mpls"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha3"]
                 [cider/cider-nrepl "0.9.0-SNAPSHOT"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/tools.nrepl "0.2.9"]]
  :profiles {:provided {:dependencies [[com.cycling74/max "6.1.0"]]}}
  :aot [mpls]
  :jar-name "mpls-small.jar"
  :uberjar-name "mpls.jar")
