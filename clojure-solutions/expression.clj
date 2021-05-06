;;===========================================FUNCTIONS=============================================

;;-------------------------------------------Factories---------------------------------------------
(defn create-operation [func]
  (fn [& expressions]
    (fn [args-map] (apply func (map #(% args-map) expressions)))))

(defn create-parser [var-map op-map const]
  (letfn [(parse-element [elem]
            (cond
              (number? elem) (const elem)
              (contains? var-map elem) (var-map elem)
              (list? elem) (apply (op-map (first elem)) (mapv parse-element (rest elem)))))]
    (fn [expr]
      (parse-element (read-string expr)))))
;;------------------------------------------Operations---------------------------------------------
(defn _div
  ([x] (/ x))
  ([x & xs] (/ (double x) (apply * xs))))

(defn _sqr [x] (* x x))

(defn at-least-one-arg [f]
  (fn [x & xs] (apply f (cons x xs))))

(def _mean (at-least-one-arg
             #(/ (apply + %&) (count %&))))
(def _varn (at-least-one-arg
             #(- (apply _mean (map _sqr %&)) (_sqr (apply _mean %&)))))
;;------------------------------------------Expressions--------------------------------------------
(def constant constantly)
(defn variable [name]
  (fn [args-map] (args-map name)))

(def add      (create-operation +))
(def subtract (create-operation -))
(def multiply (create-operation *))
(def divide   (create-operation _div))
(def negate   subtract)
(def mean     (create-operation _mean))
(def varn     (create-operation _varn))
;;---------------------------------------------Parser----------------------------------------------
(def func-var-map
  {
   'x (variable "x")
   'y (variable "y")
   'z (variable "z")
   })

(def func-op-map
  {
   '+      add
   '-      subtract
   '*      multiply
   '/      divide
   'negate negate
   'mean   mean
   'varn   varn
   })

(def parseFunction (create-parser func-var-map, func-op-map, constant))

;;============================================OBJECTS==============================================
(load-file "proto.clj")
;;--------------------------------------------Fields-----------------------------------------------
(def _value (field :value))
(def _terms (field :terms))
;;--------------------------------------------Methods----------------------------------------------
(def evaluate (method :evaluate))
(def diff     (method :diff))
(def toString (method :toString))
;;---------------------------------------Object's factories----------------------------------------
(defn create-object-expression [evaluate, diff, toString]
  (let [ctor (fn [this, value]
               (assoc this :value value))
        proto
        {
         :toString toString
         :evaluate evaluate
         :diff     diff
         }]
    (constructor ctor proto)))

(defn create-object-operation [oper, eval-func, diff-func]
  (let [ctor (fn [this, & terms]
               (assoc this :terms terms))
        proto
        {
         :toString (fn [this]
                     (str "(" oper (apply str (map #(str " " (toString %)) (_terms this))) ")"))
         :evaluate (fn [this, args-map]
                     (apply eval-func (map #(evaluate % args-map) (_terms this))))
         :diff     (fn [this, var-name]
                     (diff-func (_terms this) (map #(diff % var-name) (_terms this))))
         }]
    (constructor ctor proto)))
;;------------------------------------------Declarations-------------------------------------------
(declare Constant, Variable, Negate, Add, Subtract, Multiply, Divide)
(declare -zero, -one)
;;------------------------------------Differentiation functions------------------------------------
(defn _neg-diff [_, terms-diff] (apply Negate terms-diff))
(defn _add-diff [_, terms-diff] (apply Add terms-diff))
(defn _sub-diff [_, terms-diff] (apply Subtract terms-diff))

(defn _mul-diff [terms, terms-diff]
  (if (empty? terms)
    -zero
    (Add (apply Multiply (first terms-diff) (rest terms))
         (Multiply (first terms) (_mul-diff (rest terms) (rest terms-diff))))))

(defn _div-diff [terms, terms-diff]
  (Subtract (apply Divide (first terms-diff) (rest terms))
            (Multiply (apply Divide terms)
                      (apply Add (map Divide (rest terms-diff) (rest terms))))))
;;-------------------------------------------Constructors------------------------------------------
(def Constant
  (create-object-expression
    (fn [this, _] (_value this))
    (fn [_, _] -zero)
    (fn [this] (format "%.1f" (double (_value this))))))

;;~~~~~~~Constants~~~~~~~~
(def -zero (Constant 0))
(def -one (Constant 1))
;;~~~~~~~~~~~~~~~~~~~~~~~~

(def Variable
  (create-object-expression
    (fn [this, args-map] (args-map (_value this)))
    (fn [this, var-name] (if (= (_value this) var-name) -one -zero))
    (fn [this] (_value this))))

(def Negate   (create-object-operation "negate"   -     _neg-diff))
(def Add      (create-object-operation "+"        +     _add-diff))
(def Subtract (create-object-operation "-"        -     _sub-diff))
(def Multiply (create-object-operation "*"        *     _mul-diff))
(def Divide   (create-object-operation "/"        _div  _div-diff))
;;----------------------------------------------Parser---------------------------------------------
(def object-var-map
  {
   'x (Variable "x")
   'y (Variable "y")
   'z (Variable "z")
   })

(def object-op-map
  {
   '+      Add
   '-      Subtract
   '*      Multiply
   '/      Divide
   'negate Negate
   })

(def parseObject (create-parser object-var-map, object-op-map, Constant))
;;=================================================================================================
