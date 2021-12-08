(ns error-checker.impl.analysis)

;; var-definitions keys
;;   (:fixed-arities
;;    :end-row
;;    :meta
;;    :name-end-col
;;    :name-end-row
;;    :name-row
;;    :ns
;;    :name
;;    :defined-by
;;    :filename
;;    :col
;;    :name-col
;;    :end-col
;;    :test
;;    :row)

;; var-usages keys
;;   (:fixed-arities
;;    :end-row
;;    :name-end-col
;;    :name-end-row
;;    :name-row
;;    :name
;;    :filename
;;    :alias
;;    :from
;;    :macro
;;    :col
;;    :name-col
;;    :from-var
;;    :end-col
;;    :arity
;;    :varargs-min-arity
;;    :row
;;    :to)

(defn get-filename
  [analyzed-var]
  (:filename analyzed-var))

(defn get-ns-name
  [analyzed-var]
  (or
   ;; var-usages
   (:from analyzed-var)
   ;; var-definitions
   (:ns analyzed-var)))

(defn get-var-name
  [analyzed-var]
  (or
   ;; var-usages
   (:from-var analyzed-var)
   ;; var-definitions
   (:name analyzed-var)))

(defn same-var?
  [v1 v2]
  (and (= (get-filename v1)
          (get-filename v2))
       (= (get-var-name v1)
          (get-var-name v2))))
