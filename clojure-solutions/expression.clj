;;Factories
(defn create-operation [func]
  (fn [& expressions]
    (fn [args-map] (apply func (map #(% args-map) expressions)))))


;;Operations
(defn _div
  ([x] (/ x))
  ([x & xs] (reduce #(/ %1 (double %2)) x xs)))
(defn _mean [x & xs]
  (let [all (cons x xs)]
    (/ (apply + all) (count all))))
(defn _sqr [x]
  (* x x))
(defn _varn [x & xs]
  (let [all (cons x xs)]
    (- (apply _mean (map _sqr all)) (_sqr (apply _mean all)))))


;;Expressions
(defn constant [value]
  (constantly value))
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
(def variables-map
  {'x (variable "x"),
   'y (variable "y"),
   'z (variable "z")})
(def operators-map
  {'+      add
   '-      subtract,
   '*      multiply,
   '/      divide,
   'negate negate,
   'mean   mean,
   'varn   varn})

(defn parse-element [elem]
  (cond
    (number? elem) (constant elem)
    (contains? variables-map elem) (variables-map elem)
    (list? elem) (apply (operators-map (first elem)) (mapv parse-element (rest elem)))))

(defn parseFunction [expr]
  (parse-element (read-string expr)))
