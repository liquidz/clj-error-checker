(ns foo.core
  (:require
   [foo.sub :as sub]))

(defn- sample1
  []
  (sub/throwing 1))

(defn- sample2
  []
  (try
    (sub/throwing 1)
    (catch Exception ex
      (.printStackTrace ex))))

(defn- sample3
  []
  (try
    (sub/throwing 1)
    (finally
      (println "foo"))))

(defn -main
  []
  (sample1)
  (sample2)
  (sample3))

