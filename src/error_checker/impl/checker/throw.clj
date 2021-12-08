(ns error-checker.impl.checker.throw
  (:require
   [error-checker.impl.checker :as i.checker]
   [error-checker.impl.hierarchy :as i.hierarchy]
   [rewrite-clj.zip :as z]))

(defn- throw-handled?
  [target-var & [options]]
  (let [extra-sym-set (->> (:extra-handling-symbols options [])
                           (map symbol)
                           (set))]
    (if-let [zloc (i.checker/find-var-zloc target-var)]
      (if-let [zloc (z/find zloc z/up #(i.checker/form? % #{'try}))]
        (some? (z/find (z/down zloc) z/right #(i.checker/form? % #{'catch})))
        (if (seq extra-sym-set)
          (some? (z/find zloc z/up #(i.checker/form? % extra-sym-set)))
          false))
      false)))

(defn check-uncatched-exception
  [analysis & [options]]
  (let [hierarchies (i.hierarchy/call-hierarchies analysis 'clojure.core/throw)]
    (reduce
     (fn [accm hierarchy]
       (let [result (i.checker/check-hierarchy
                     throw-handled?
                     hierarchy
                     options)]
         (if result
           accm
           (update accm :errors conj hierarchy))))
     {:errors []}
     hierarchies)))
