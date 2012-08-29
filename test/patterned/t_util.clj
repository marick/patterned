(ns patterned.t-util
  (:use patterned.util)
  (:use midje.sweet clojure.pprint))
  

;;; Low level utilities

(fact
  ((flat-arg :item) '(1 2 :item 3 4)) => 3)

;;; Multimethods

(fact
  (fact
    (match-one? 'n 1) => truthy 
    (match-one? 1 1) => truthy
    (match-one? 1 2) => falsey)

  (fact "nested"
    (match-one? '[n] [1]) => truthy
    (match-one? [1] [1]) => truthy
    (match-one? [1] [2]) => falsey
    (match-one? '[n 1] [2 1]) => truthy
    (match-one? '[n 1] [2 2]) => falsey
    (match-one? '[] []) => truthy)

  (fact "nested length checks"
    (match-one?  '[n 1] []) => falsey
    (match-one?  '[n 1] [1]) => falsey
    (match-one?  '[n 1] [1 1 1]) => falsey)

  (fact "rest args in patterns"
    (let [pattern '[1 n & rest]]
      (match-one? pattern [1]) => falsey
      (match-one? pattern [1 2]) => truthy
      (match-one? pattern [1 2 3]) => truthy
      (match-one? pattern [1 2 3 4]) => truthy
      (match-one? pattern [2 2 3 4]) => falsey))

  (fact "guards"
    (match-one? '(:when (partial = 1)) 1) => truthy
    (match-one? '(:when odd?) 2) => falsey))

(future-fact "guards can use their lexical environment")
;;; Currently, I hacklishly use eval, so the following doesn't work.
;;    (let [a 1]
;;      (match-one? '(:when (partial = a)) 1) => truthy)))
  
  

(fact
  (match-map 'n 1) => {'n 1}
  (match-map 1 1) => {}
  (match-map '[n 1] [2 1]) => {'n 2}
  (match-map '[n [1 m]] [2 [1 3]]) => {'n 2, 'm 3}
  (match-map [] []) => {}

  (fact "rest args in patterns"
    (match-map '[n & rest] [1 2 3]) => {'n 1, 'rest '(2 3)}
    (match-map '[n m & rest] [1 2 3]) => {'n 1, 'm 2, 'rest '(3)}
    (match-map '[n & rest] [1 2]) => {'n 1, 'rest '(2)}
    (match-map '[n & rest] [1]) => {'n 1, 'rest '()}
    (match-map '[& rest] [1]) => {'rest '(1)}))

(fact
  (symbols-in 1) => []
  (symbols-in 'n) => '[n]
  (symbols-in '[1]) => []
  (symbols-in '[m n]) => '[m n]
  (symbols-in '[1 n 1]) => '[n]
  (symbols-in '[1 n & rest]) => '[n rest])


;;; Code-builders

(fact
  (let-steps '[1] 'match) => []
  (let-steps '[n] 'match) => '[n (match 'n)]
  (let-steps '[m n] 'match) => '[m (match 'm)
                                 n (match 'n)])

(fact
  (pattern-match [1] [1]) => {}
  (pattern-match '[m] [1]) => {'m 1}
  (pattern-match '[m 3 n] [1 3 2]) => {'m 1, 'n 2}

  (pattern-match [1] [2]) => nil
  (pattern-match '[m 3 n] [1 2 3]) => nil)
