(ns error-checker.core
  (:gen-class)
  (:require
   [clj-kondo.core :as clj-kondo]
   [clojure.string :as str]
   [error-checker.impl.checker.throw :as i.c.throw]))

(defn- print-errors
  [checked-result]
  (when-let [errors (seq (:errors checked-result))]
    (doseq [x errors]
      (doseq [line (map-indexed
                    (fn [i y]
                      (str (str/join "" (repeat i "\t"))
                           (:filename y)
                           ":" (:row y)
                           ":" (:col y)
                           ": " (:from y) "/" (:from-var y)))
                    x)]
        (println line)))
    true))

(defn- run
  [checker-fn options]
  (let [directories (set (cons "." (:directory options)))
        analysis (-> (clj-kondo/run! {:lint directories
                                      :config-dir (:config-dir options)
                                      :config {:output {:analysis {:var-definitions {:meta true}}}}})
                     (:analysis))
        result (checker-fn analysis options)]
    (or (print-errors result)
        (println "No errors"))))

(def uncatched-exception
  (partial run i.c.throw/check-uncatched-exception))

(defn -main
  [& args]
  (println "start " args))
