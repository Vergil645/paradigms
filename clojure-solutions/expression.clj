;;Factories
(defn create-operation [func]
  (fn
    ([expr]
     (fn [args-map] (func (expr args-map))))
    ([expr & expressions]
     (fn [args-map] (reduce func (expr args-map) (mapv #(% args-map) expressions))))))
(defn create-unary-operation [func]
  (fn [expr]
    (fn [args-map] (func (expr args-map)))))


;;Operations
(defn _div
  ([x] (/ (double x)))
  ([x y] (/ (double x) (double y))))


;;Expressions
(defn constant [value]
  (fn [_] value))
(defn variable [name]
  (fn [args-map] (args-map name)))

(def negate (create-unary-operation -))
(def add (create-operation +))
(def subtract (create-operation -))
(def multiply (create-operation *))
(def divide (create-operation _div))


;;Parser
(def operators-map {'+ add, '- subtract, '* multiply, '/ divide, 'negate negate})
(defn parseFunction [expr]
  (letfn [(parse [lexeme]
            (cond
              (list? lexeme) (apply
                               (operators-map (first lexeme))
                               (mapv parse (rest lexeme)))
              (number? lexeme) (constant lexeme)
              :else (variable (str lexeme))))]
    (parse (read-string expr))))
