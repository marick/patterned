(defproject patterned "0.2.0"
  :description "Adds a `defpatterned` macro"
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :profiles {:dev {:dependencies [[midje "1.4.0"]]
                   :plugins [[lein-midje "2.0.1"]]}})
