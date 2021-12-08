(ns error-checker.impl.hierarchy
  (:require
   [error-checker.impl.analysis :as i.analysis]))

(defn- find-first
  [pred coll]
  (some #(and (pred %) %) coll))

(defn- extract-using-vars-by-symbol
  [analysis qualified-symbol]
  (let [ns-sym (symbol (namespace qualified-symbol))
        name-sym (symbol (name qualified-symbol))]
    (->> (:var-usages analysis)
         (filter #(and (= ns-sym (:to %))
                       (= name-sym (:name %))))
         (distinct))))

(defn- extract-using-vars-by-vars
  [analysis target-vars]
  (->> (:var-usages analysis)
       (filter
        (fn [v]
          (some #(and (= (:to v) (i.analysis/get-ns-name %))
                      (= (:name v) (i.analysis/get-var-name %)))
                target-vars)))
       (distinct)))

(defn call-hierarchies
  [analysis qualified-symbol]
  (loop [vars (->> (extract-using-vars-by-symbol analysis qualified-symbol)
                   (map vector))]
    (if (every? #(= ::no-more-hierarchy (first %)) vars)
      (->> vars
           (map #(drop 1 %))
           (remove #(= 1 (count %))))
      (recur (->> vars
                  (mapcat
                   (fn [[v :as coll]]
                     (let [no-more-hierarchy? (= ::no-more-hierarchy v)
                           using-vars (when-not no-more-hierarchy?
                                        (extract-using-vars-by-vars analysis [v]))]
                       (cond
                         no-more-hierarchy?
                         [coll]

                         (seq using-vars)
                         (map (fn [new-var]
                                (-> (if (some? (find-first #(i.analysis/same-var? new-var %) coll))
                                      ::no-more-hierarchy
                                      new-var)
                                    (cons coll)
                                    (vec)))
                              using-vars)

                         :else
                         [(vec (cons ::no-more-hierarchy coll))]))))
                  (distinct))))))
