(ns foo.sub)

(defn throwing
  [n]
  (if (odd? n)
    (throw (ex-info "test" {}))
    ::ok))
