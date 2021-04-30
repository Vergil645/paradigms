;;Factories
(defn create-operation [func]
  (fn [& expressions]
    (fn [args-map] (apply func (map #(% args-map) expressions)))))


;;Operations
(defn _div
  ([x] (/ x))
  ([x & xs] (reduce #(/ %1 (double %2)) x xs)))
(defn _mean
  [x & xs] (/ (apply + x xs) (inc (count xs))))
(defn _sqr
  [x] (* x x))
(defn _varn
  [x & xs] (- (apply _mean (_sqr x) (map #(* % %) xs)) (_sqr (apply _mean x xs))))


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
(def mean (create-operation _mean))
(def varn (create-operation _varn))


;;Parser
(def variable-names #{'x, 'y, 'z})
(def operators-map {'+ add, '- subtract, '* multiply, '/ divide, 'negate negate, 'mean mean, 'varn varn})
(defn parse-lexeme [lexeme]
  (cond
    (number? lexeme) (constant lexeme)
    (contains? variable-names lexeme) (variable (str (variable-names lexeme)))
    (list? lexeme) (apply (operators-map (first lexeme)) (mapv parse-lexeme (rest lexeme)))
    :else nil))
(defn parseFunction [expr]
  (parse-lexeme (read-string expr)))
