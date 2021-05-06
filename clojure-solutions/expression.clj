;;===========================FUNCTIONS=========================================

;;Factories
(defn create-operation [func]
  (fn [& expressions]
    (fn [args-map] (apply func (map #(% args-map) expressions)))))


;;Operations
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


;;Expressions
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


;;Parser
(def variables-map
  {
   'x (variable "x")
   'y (variable "y")
   'z (variable "z")
   })

(def operators-map
  {
   '+      add
   '-      subtract
   '*      multiply
   '/      divide
   'negate negate
   'mean   mean
   'varn   varn
   })

(defn parse-element [elem]
  (cond
    (number? elem) (constant elem)
    (contains? variables-map elem) (variables-map elem)
    (list? elem) (apply (operators-map (first elem)) (mapv parse-element (rest elem)))))

(defn parseFunction [expr]
  (parse-element (read-string expr)))



;;===============================OBJECTS=======================================
(load-file "proto.clj")

;;Fields
(def _value (field :value))
(def _name (field :name))
(def _term (field :term))
(def _terms (field :terms))


;;Methods
(def evaluate (method :evaluate))
(def diff (method :diff))
(def toString (method :toString))


;;Constructors declaration
(declare
  Constant
  Variable
  Negate
  Add
  Subtract
  Multiply
  Divide
  )


;;Prototypes
(def ConstantPrototype
  {
   :evaluate (fn [this _] (_value this))
   :diff     (fn [_ _] (Constant 0))
   :toString (fn [this] (format "%.1f" (_value this)))
   })
(def VariablePrototype
  {
   :evaluate (fn [this args-map] (args-map (_name this)))
   :diff     (fn [this var-name]
               (Constant (if (= (_name this) var-name) 1 0)))
   :toString (fn [this] (_name this))
   })
(def NegatePrototype
  {
   :evaluate (fn [this args-map] (- (evaluate (_term this) args-map)))
   :diff     (fn [this var-name] (Negate (diff (_term this) var-name)))
   :toString (fn [this] (str "(negate " (toString (_term this)) ")"))
   })
(def AddPrototype
  {
   :evaluate (fn [this args-map] (apply + (map #(evaluate % args-map) (_terms this))))
   :diff     (fn [this var-name] (apply Add (map #(diff % var-name) (_terms this))))
   :toString (fn [this] (str "(+" (apply str (map #(str " " (toString %)) (_terms this))) ")"))
   })
(def SubtractPrototype
  {
   :evaluate (fn [this args-map] (apply - (map #(evaluate % args-map) (_terms this))))
   :diff     (fn [this var-name] (apply Subtract (map #(diff % var-name) (_terms this))))
   :toString (fn [this] (str "(-" (apply str (map #(str " " (toString %)) (_terms this))) ")"))
   })
(def MultiplyPrototype
  {
   :evaluate (fn [this args-map] (apply * (map #(evaluate % args-map) (_terms this))))
   :diff     (fn [this var-name]
               (let [this-terms (_terms this)
                     terms-count (count this-terms)]
                 (apply Add
                        (for [i (range terms-count)]
                          (apply Multiply
                                 (for [j (range terms-count)]
                                   (if (== i j)
                                     (diff (this-terms j) var-name)
                                     (this-terms j))))))))
   :toString (fn [this] (str "(*" (apply str (map #(str " " (toString %)) (_terms this))) ")"))
   })
(def DividePrototype
  {
   :evaluate (fn [this args-map] (apply _div (map #(evaluate % args-map) (_terms this))))
   :diff     (fn [this var-name]
               (let [this-terms (_terms this)
                     terms-count (count this-terms)]
                 (Divide
                   (apply Subtract
                          (for [i (range terms-count)]
                            (apply Multiply
                                   (for [j (range terms-count)]
                                     (if (== i j)
                                       (diff (this-terms j) var-name)
                                       (this-terms j))))))
                   (apply Multiply (map #(Multiply % %) (rest this-terms))))))
   :toString (fn [this] (str "(/" (apply str (map #(str " " (toString %)) (_terms this))) ")"))
   })


;;Constructors
(defn _Constant [this value]
  (assoc this :value (double value)))
(def Constant (constructor _Constant ConstantPrototype))

(defn _Variable [this name]
  (assoc this :name name))
(def Variable (constructor _Variable VariablePrototype))

(defn _Negate [this term]
  (assoc this :term term))
(def Negate (constructor _Negate NegatePrototype))

(defn _Add [this & terms]
  (assoc this :terms (vec terms)))
(def Add (constructor _Add AddPrototype))

(defn _Subtract [this & terms]
  (assoc this :terms (vec terms)))
(def Subtract (constructor _Subtract SubtractPrototype))

(defn _Multiply [this & terms]
  (assoc this :terms (vec terms)))
(def Multiply (constructor _Multiply MultiplyPrototype))

(defn _Divide [this & terms]
  (assoc this :terms (vec terms)))
(def Divide (constructor _Divide DividePrototype))


;;Parser
(def object-variables-map
  {
   'x (Variable "x")
   'y (Variable "y")
   'z (Variable "z")
   })
(def object-operators-map
  {
   '+      Add
   '-      Subtract
   '*      Multiply
   '/      Divide
   'negate Negate
   })

(defn object-parse-element [elem]
  (cond
    (number? elem) (Constant elem)
    (contains? object-variables-map elem) (object-variables-map elem)
    (list? elem) (apply (object-operators-map (first elem)) (mapv object-parse-element (rest elem)))))

(defn parseObject [expr]
  (object-parse-element (read-string expr)))
