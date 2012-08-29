(ns patterned.t-sweet
  (:use patterned.sweet)
  (:use midje.sweet clojure.pprint))
  
  

(defpatterned match-literal
   [1] "one")

(fact
  (match-literal 1) => "one"
  (match-literal) => (throws Error))

(defpatterned match-1-arg
   [1] "one"
   [n] (str "not one: " n))

(fact
  (match-1-arg 1) => "one"
  (match-1-arg 2) => "not one: 2"
  (match-1-arg 2 :args) => (throws Error))


(defpatterned match-2-args
  [1 n] (str "one with " n)
  [n 2] (str n " and two")
  [n o] (str n " and " o))

(fact
  (match-2-args 1 2) => "one with 2"
  (match-2-args 3 2) => "3 and two"
  (match-2-args 'a '[b]) => "a and [b]")


(defpatterned sequence-match
  [[1 n]] (str "one embedded with " n)
  [catchall] (str "caught " catchall))


(fact
  (sequence-match [1 2]) => "one embedded with 2"
  (sequence-match [1 [2]]) => "one embedded with [2]"
  (sequence-match [3 5]) => "caught [3 5]"
  (sequence-match [3]) => "caught [3]"
  (sequence-match [1 2 3]) => "caught [1 2 3]")


(def oops!
     (fn [reason & args]
       (with-meta (merge {:reason reason}
                         (apply hash-map args))
                  {:type :error})))

(defpatterned ssecond
  [[_]] (oops! "only one argument")
  [[one two & rest]] two)

(fact
  (ssecond [1]) => {:reason "only one argument"}
  (ssecond [1 2]) => 2
  (ssecond ["a" "b" "c"]) => "b")

(defpatterned count-sequence
  [[]] 0
  [[head & tail]] (inc (count-sequence tail)))

(fact
  (count-sequence []) => 0
  (count-sequence [1 2 3]) => 3)

(defpatterned factorial
  [(:when (partial > 0) :bind n)] (oops! "Bad number" :n n)
  [(:in [0 1])] 1
  [n] (* n (factorial (dec n))))

(fact
  (factorial -1) => {:reason "Bad number" :n -1}
  (factorial 0) => 1
  (factorial 1) => 1
  (factorial 5) => 120)


(defpatterned factorial
  [so-far 1] so-far
  [so-far n] (factorial (* n so-far) (dec n))
  [n] (factorial 1 n))

(fact
  (factorial 1) => 1
  (factorial 5) => 120)

(defpatterned deep
  [:a [1]]          "a simple match"
  [s [1 [1 [n]]]]   (str "a match of " s " and " n))

(fact
  (deep :a [1]) => "a simple match"
  (deep :b [1 [1 [3]]]) => "a match of :b and 3")

(defpatterned resty
  [:a :b & rest] (str ":a and :b with " (pr-str rest))
  [:a & rest] (str ":a with " (pr-str rest))
  [& catchall] catchall)


(fact
  (resty :a :b) => ":a and :b with ()"
  (resty :a :b :c) => ":a and :b with (:c)"
  (resty :a :c) => ":a with (:c)"
  (resty :b) => '(:b))
