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


(defn partition-for-rest [pattern arg]
  (let [required-arg-count (- (count pattern) 2)]
    [ [(take required-arg-count pattern)
       (take required-arg-count arg)]
      [(last pattern)
       (drop required-arg-count arg)]]))


;;; Multimethods  

(defn pattern-classification [pattern & rest]
  (cond (symbol? pattern)
        :symbol

        (has-rest? pattern)
        :nested-with-rest

        (has-guard? pattern)
        :guard

        (sequential? pattern)
        :nested
        
        :else
        :literal))



(defmulti match-one? pattern-classification)
(defmulti match-map pattern-classification)
(defmulti symbols-in pattern-classification)

(defmethod match-one? :literal [pattern arg] (= pattern arg))
(defmethod match-map :literal [pattern arg] {})
(defmethod symbols-in :literal [pattern] [])

(defmethod match-one? :symbol [pattern arg] true)
(defmethod match-map :symbol [pattern arg] {pattern arg})
(defmethod symbols-in :symbol [pattern] [pattern])

(defmethod match-one? :nested [pattern arg]
  (and (= (count pattern) (count arg))
       (every? true? (map match-one? pattern arg))))
(defmethod match-map :nested [pattern arg]
  (merge {}  ; for some reason (merge) returns nil
         (apply merge (map match-map pattern arg))))
(defmethod symbols-in :nested [pattern] (mapcat symbols-in pattern))

(defmethod match-one? :nested-with-rest [pattern arg]
  (let [ [[pattern-required-part arg-required-part] _] (partition-for-rest pattern arg)]
    (match-one? pattern-required-part arg-required-part)))
(defmethod match-map :nested-with-rest [pattern arg]
  (let [ [[pattern-required-part arg-required-part]
          [rest-symbol rest-data]] (partition-for-rest pattern arg)]
    (assoc (match-map pattern-required-part arg-required-part) rest-symbol rest-data)))
(defmethod symbols-in :nested-with-rest [pattern]
  (symbols-in (remove-rest pattern)))

(defmethod match-one? :guard [pattern arg]
  ( (eval (guard-arg pattern)) arg))

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
