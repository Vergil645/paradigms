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

(def add (create-operation +))
(def subtract (create-operation -))
(def multiply (create-operation *))
(def divide (create-operation _div))
(def negate subtract)
(def mean (create-operation _mean))
(def varn (create-operation _varn))
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
(def -value (field :value))
(def -terms (field :terms))
(def -oper (field :oper))
(def -eval-func (field :eval-func))
(def -diff-func (field :diff-func))
;;--------------------------------------------Methods----------------------------------------------
(def toString (method :toString))
(def evaluate (method :evaluate))
(def diff (method :diff))
;;---------------------------------------Object's factories----------------------------------------
(defn create-object-expression [evaluate, diff, toString]
  (let [ctor (fn [this, value] (assoc this :value value))
        proto
        {
         :toString toString
         :evaluate evaluate
         :diff     diff
         }]
    (constructor ctor proto)))

(def operation-proto
  {
   :toString (fn [this]
               (str "(" (-oper this) (reduce #(str %1 " " (toString %2)) "" (-terms this)) ")"))
   :evaluate (fn [this, args-map]
               (apply (-eval-func this) (map #(evaluate % args-map) (-terms this))))
   :diff     (fn [this, var-name]
               ((-diff-func this) (-terms this) (map #(diff % var-name) (-terms this))))
   })

(defn create-object-operation [oper, eval-func, diff-func]
  (let [ctor (fn [this, & terms] (assoc this :terms terms))
        proto
        {
         :prototype operation-proto
         :oper      oper
         :eval-func eval-func
         :diff-func diff-func
         }]
    (constructor ctor proto)))
;;------------------------------------------Declarations-------------------------------------------
(declare Constant, Variable, Negate, Add, Subtract, Multiply, Divide)
(declare const-zero, const-one)
;;-------------------------------------------Constructors------------------------------------------
(def Constant
  (create-object-expression
    (fn [this, _] (-value this))
    (fn [_, _] const-zero)
    (fn [this] (format "%.1f" (double (-value this))))))

;;~~~~~~~Constants~~~~~~~~
(def const-zero (Constant 0))
(def const-one (Constant 1))
;;~~~~~~~~~~~~~~~~~~~~~~~~

(def Variable
  (create-object-expression
    (fn [this, args-map] (args-map (-value this)))
    (fn [this, var-name] (if (= (-value this) var-name) const-one const-zero))
    (fn [this] (-value this))))

(def Negate
  (create-object-operation
    "negate"
    -
    (fn [_, terms-diff] (apply Negate terms-diff))))

(def Add
  (create-object-operation
    "+"
    +
    (fn [_, terms-diff] (apply Add terms-diff))))

(def Subtract
  (create-object-operation
    "-"
    -
    (fn [_, terms-diff] (apply Subtract terms-diff))))

(def Multiply
  (create-object-operation
    "*"
    *
    (fn rec [terms, terms-diff]
      (if (empty? terms)
        const-zero
        (Add
          (apply Multiply (first terms-diff) (rest terms))
          (Multiply (first terms) (rec (rest terms) (rest terms-diff))))))))

(def Divide
  (create-object-operation
    "/"
    _div
    (fn [terms, terms-diff]
      (Subtract
        (apply Divide (first terms-diff) (rest terms))
        (Multiply
          (apply Divide terms)
          (apply Add (map Divide (rest terms-diff) (rest terms))))))))
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
