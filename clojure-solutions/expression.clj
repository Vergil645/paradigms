(defn constant [value] (fn [_] value))

(defn variable [name] (fn [args-map] (args-map name)))

(println "constant --->" ((constant 2) {"x" 1}))
(println "variable --->" ((variable "x") {"x" 1}))
