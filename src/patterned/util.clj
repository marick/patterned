(ns patterned.util)

;;; Low level utils

(defn flat-has? [element]
  (fn [pattern]
    (and (sequential? pattern)
         (some #{element} pattern))))

(defn flat-arg [element]
  (fn [sequence]
    (second (drop-while (complement #{element}) sequence))))

(defn flat-remove [element]
  (fn [sequence]
    (remove #{element} sequence)))

(def has-rest? (flat-has? '&))
(def rest-arg (flat-arg '&))
(def remove-rest (flat-remove '&))

(def has-guard? (flat-has? :when))
(def guard-arg (flat-arg :when))

(def has-name? (flat-has? :bind))
(def name-arg (flat-arg :bind))

(def has-choice? (flat-has? :in))
(def choice-arg (flat-arg :in))


(defn partition-for-rest [pattern arg]
  (let [required-arg-count (- (count pattern) 2)]
    [ [(take required-arg-count pattern)
       (take required-arg-count arg)]
      [(last pattern)
       (drop required-arg-count arg)]]))

(defn truthy? [x]
  (not (not x)))

;;; Multimethods

(derive ::guard ::nameable)
(derive ::choice ::nameable)

(defn pattern-classification [pattern & rest]
  (cond (symbol? pattern)
        ::symbol

        (has-rest? pattern)
        ::nested-with-rest

        (has-guard? pattern)
        ::guard

        (has-choice? pattern)
        ::choice

        (sequential? pattern)
        ::nested
        
        :else
        ::literal))


;;; match-one?
(defmulti match-one? pattern-classification)
(defmethod match-one? ::literal [pattern arg] (= pattern arg))
(defmethod match-one? ::symbol [pattern arg] true)

(defmethod match-one? ::nested [pattern arg]
  (and (= (count pattern) (count arg))
       (every? truthy? (map match-one? pattern arg))))

(defmethod match-one? ::nested-with-rest [pattern arg]
  (let [ [[pattern-required-part arg-required-part] _] (partition-for-rest pattern arg)]
    (match-one? pattern-required-part arg-required-part)))

;; Using `eval` here is a hack.
(defmethod match-one? ::guard [pattern arg]
  ( (eval (guard-arg pattern)) arg))

(defmethod match-one? ::choice [pattern arg]
  (truthy? (some #{arg} (choice-arg pattern))))
  

;;; match-map
(defmulti match-map pattern-classification)
(defmethod match-map ::literal [pattern arg] {})
(defmethod match-map ::symbol [pattern arg] {pattern arg})

(defmethod match-map ::nested [pattern arg]
  (merge {}  ; for some reason (merge) returns nil
         (apply merge (map match-map pattern arg))))

(defmethod match-map ::nested-with-rest [pattern arg]
  (let [ [[pattern-required-part arg-required-part]
          [rest-symbol rest-data]] (partition-for-rest pattern arg)]
    (assoc (match-map pattern-required-part arg-required-part) rest-symbol rest-data)))

(defmethod match-map ::nameable [pattern arg]
  (if (has-name? pattern)
    {(name-arg pattern) arg}
    {}))


;;; symbols-in
(defmulti symbols-in pattern-classification)
(defmethod symbols-in ::literal [pattern] [])
(defmethod symbols-in ::symbol [pattern] [pattern])
(defmethod symbols-in ::nested [pattern] (mapcat symbols-in pattern))

(defmethod symbols-in ::nested-with-rest [pattern]
  (symbols-in (remove-rest pattern)))

(defmethod symbols-in ::nameable [pattern]
  (if (has-name? pattern)
    [(name-arg pattern)]
    []))



;;; Form construction

(defn catchall-clause [args-symbol]
  `(throw (Error. (str "No pattern matched these arguments: "
                       (pr-str ~args-symbol)))))


(defn let-steps [pattern match-symbol]
  (reduce (fn [so-far pattern-symbol]
            (concat so-far [pattern-symbol `(~match-symbol '~pattern-symbol)]))
          []
          (symbols-in pattern)))

(defn pattern-match [pattern args]
  (if (match-one? pattern args)
    (match-map pattern args)
    nil))

(defn patterned-body [args-symbol match-symbol]
  (fn [so-far [pattern action]]
    `(if-let [~match-symbol (pattern-match '~pattern ~args-symbol)]
       (let [~@(let-steps pattern match-symbol)] ~action)
       ~so-far)))
