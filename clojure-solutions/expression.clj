;Factories
(defn binary-operation [func]
  (fn [expr-1 expr-2]
    (fn [args-map] (func (double (expr-1 args-map)) (double (expr-2 args-map))))))

(defn constant [value] (fn [_] value))

(defn variable [name] (fn [args-map] (args-map name)))

(defn negate [expr-1]
  (fn [args-map] (- (expr-1 args-map))))

(def add (binary-operation +))
(def subtract (binary-operation -))
(def multiply (binary-operation *))
;(def divide (binary-operation /))
(defn divide [expr-1 expr-2]
  (fn [args-map] (/ (double (expr-1 args-map)) (double (expr-2 args-map)))))


(println "constant --->" ((constant 2) {"x" 1}))
(println "variable --->" ((variable "x") {"x" 1}))
