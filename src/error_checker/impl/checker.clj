(ns error-checker.impl.checker
  (:require
   [clojure.string :as str]
   [rewrite-clj.zip :as z]))

(def zloc-of-file
  (memoize z/of-file))

(defn- find-var-zloc*
  [target-var]
  (let [var-pos [(:name-row target-var)
                 (:name-col target-var)]
        zloc (zloc-of-file (:filename target-var) {:track-position? true})
        target-sym (if-let [a (:alias target-var)]
                     (symbol (str a) (str (:name target-var)))
                     (:name target-var))]
    (loop [zloc zloc]
      (when-let [zloc (z/find-value zloc z/next target-sym)]
        (if (= var-pos (z/position zloc))
          zloc
          (recur (z/next zloc)))))))

(def find-var-zloc
  (memoize find-var-zloc*))

(defn form?
  [zloc sym-set]
  (and (z/list? zloc)
       (contains? sym-set (-> zloc z/down z/sexpr))))

(defn check-hierarchy
  [handled-pred hierarchy & [options]]
  (reduce
   (fn [falsy target-var]
     (cond
       (some #(str/includes? (:from target-var) %) (:exclude options))
       (reduced true)

       (handled-pred target-var options)
       (reduced true)

       :else
       falsy))
   false
   hierarchy))
