(ns error-checker.main
  (:gen-class)
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [error-checker.core :as core]))

(defn- concat-assoc-fn
  [opt k v]
  (update opt k concat (str/split v #":")))

(def cli-options
  [[nil "--exclude=EXCLUDE" :default [] :assoc-fn concat-assoc-fn]
   ["-d" "--directory=DIRECTORY" :default ["."] :assoc-fn concat-assoc-fn]])

(defn -main
  [& args]
  (let [{:keys [arguments options errors]} (cli/parse-opts args cli-options)
        subcommand (first arguments)]
    (cond
      errors
      (do (doseq [e errors]
            (println e))
          (System/exit 1))
      (= "uncatched-exception" subcommand)
      (core/uncatched-exception options)

      :else
      (do (println "Unknown subcommand:" subcommand)
          (System/exit 1)))))
