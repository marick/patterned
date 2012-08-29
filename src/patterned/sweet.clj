(ns patterned.sweet
  (:use patterned.util))

(defmacro defpatterned [name & pairs]
  (let [args-symbol (gensym "args-")
        match-symbol (gensym "matching-pattern-")]
    `(defn ~name [& ~args-symbol]
       ~(reduce (patterned-body args-symbol match-symbol)
                (catchall-clause args-symbol)
                (reverse (partition 2 pairs))))))
