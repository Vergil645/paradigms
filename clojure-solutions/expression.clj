;;Factories
(defn create-operation [func]
  (fn [& expressions]
    (fn [args-map] (apply func (map #(% args-map) expressions)))))


;;Operations
(defn _div
  ([x] (/ x))
  ([x & xs] (reduce #(/ %1 (double %2)) x xs)))


;;Expressions
(defn constant [value]
  (fn [_] value))
(defn variable [name]
  (fn [args-map] (args-map name)))

(def add (create-operation +))
(def subtract (create-operation -))
(def multiply (create-operation *))
(def divide (create-operation _div))
(def negate subtract)


;;Parser
(def variable-names #{'x, 'y, 'z})
(def operators-map {'+ add, '- subtract, '* multiply, '/ divide, 'negate negate})
(defn parse-lexeme [lexeme]
  (cond
    (number? lexeme) (constant lexeme)
    (contains? variable-names lexeme) (variable (str (variable-names lexeme)))
    (list? lexeme) (apply (operators-map (first lexeme)) (mapv parse-lexeme (rest lexeme)))
    :else nil))
(defn parseFunction [expr]
  (parse-lexeme (read-string expr)))
