(ns patterned.util)

(defn catchall-clause [args-symbol]
  `(throw (Error. (str "No pattern matched these arguments: "
                       (pr-str ~args-symbol)))))

(def rest-symbol? (partial = '&))

(defn pattern-classification [pattern & rest]
  (cond (symbol? pattern)
        :symbol

        (and (sequential? pattern)
             (some rest-symbol? pattern))
        :nested-with-rest

        (sequential? pattern)
        :nested
        
        :else
        :literal))


(defn partition-for-rest [pattern arg]
  (let [required-arg-count (- (count pattern) 2)]
    [ [(take required-arg-count pattern)
       (take required-arg-count arg)]
      [(last pattern)
       (drop required-arg-count arg)]]))


(defmulti match-one? pattern-classification)
(defmethod match-one? :symbol [pattern arg] true)
(defmethod match-one? :literal [pattern arg] (= pattern arg))
(defmethod match-one? :nested [pattern arg]
  (and (= (count pattern) (count arg))
       (every? true? (map match-one? pattern arg))))
(defmethod match-one? :nested-with-rest [pattern arg]
  (let [ [[pattern-required-part arg-required-part] _] (partition-for-rest pattern arg)]
    (match-one? pattern-required-part arg-required-part)))
            

(defmulti match-map pattern-classification)
(defmethod match-map :symbol [pattern arg] {pattern arg})
(defmethod match-map :literal [pattern arg] {})
(defmethod match-map :nested [pattern arg]
  (merge {}  ; for some reason (merge) returns nil
         (apply merge (map match-map pattern arg))))

(defmethod match-map :nested-with-rest [pattern arg]
  (let [ [[pattern-required-part arg-required-part]
          [rest-symbol rest-data]] (partition-for-rest pattern arg)]
    (assoc (match-map pattern-required-part arg-required-part) rest-symbol rest-data)))
  


(defmulti symbols-in pattern-classification)
(defmethod symbols-in :symbol [pattern] [pattern])
(defmethod symbols-in :literal [pattern] [])
(defmethod symbols-in :nested [pattern] (mapcat symbols-in pattern))
(defmethod symbols-in :nested-with-rest [pattern]
  (symbols-in (remove rest-symbol? pattern)))



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
