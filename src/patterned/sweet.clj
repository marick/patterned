(ns patterned.sweet
  (:use patterned.util))

(defmacro patterned [& pairs]
  (tagged-body 'fn pairs))

(defmacro defpatterned [name & pairs]
  `(def ~name (patterned ~@pairs)))

(defmacro letpatterned [patterns & body]
  (let [expanded (map (fn [ [name & rest] ]
                        (tagged-body name rest))
                      patterns)]
    `(letfn [~@expanded] ~@body)))
                     
