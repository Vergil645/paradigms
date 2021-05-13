;;=================================================================================================
;;========================================== FUNCTIONS ============================================
;;=================================================================================================

;;------------------------------------------ Factories --------------------------------------------

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

;;------------------------------------------ Macros -----------------------------------------------

(defmacro def-at-least-one-arg
  "Defines function which take at least one argument"
  [name, func]
  `(def ~name (at-least-one-arg ~func)))

(defmacro def-func-oper
  "Defines function operation"
  [name, func]
  `(def ~name (create-operation ~func)))

(defmacro def-func-oper
  "Defines function operation"
  [name, func]
  `(defn ~name [& expressions#]
     (fn [args-map#] (apply ~func (map #(% args-map#) expressions#)))))

;;------------------------------------- Evaluate functions ----------------------------------------

(defn _div
  ([x] (/ 1.0 (double x)))
  ([x & xs] (/ (double x) (apply * xs))))

(defn _sqr [x] (* x x))

(defn at-least-one-arg [f]
  (fn [x & xs] (apply f (cons x xs))))

(def-at-least-one-arg _mean #(_div (apply + %&) (count %&)))
(def-at-least-one-arg _varn #(- (apply _mean (map _sqr %&)) (_sqr (apply _mean %&))))

(def _pow #(Math/pow %1 %2))
(def _log #(Math/log %))
(def _abs #(Math/abs (double %)))

(def _arith-mean _mean)
(def-at-least-one-arg _geom-mean #(_pow (_abs (apply * %&)) (_div (count %&))))
(def-at-least-one-arg _harm-mean #(_div (count %&) (apply + (map _div %&))))

;;----------------------------------------- Expressions -------------------------------------------

(def constant constantly)
(defn variable [name]
  (fn [args-map] (args-map name)))

(def-func-oper add +)
(def-func-oper subtract -)
(def-func-oper multiply *)
(def-func-oper divide _div)
(def-func-oper negate -)
(def-func-oper mean _mean)
(def-func-oper varn _varn)

;;-------------------------------------------- Parser ---------------------------------------------

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

;;=================================================================================================
;;=========================================== OBJECTS =============================================
;;=================================================================================================

;;------------------------------------------- Import ----------------------------------------------

(load-file "proto.clj")
(use '[clojure.string :only [join]])

;;------------------------------------------  Macros ----------------------------------------------

(defmacro def-fields
  "Defines multiple fields"
  [& names]
  `(do ~@(mapv (fn [name] `(def ~name (field ~(keyword (subs (str name) 1))))) names)))

(defmacro def-methods
  "Defines multiple methods"
  [& names]
  `(do ~@(mapv (fn [name] `(def ~name (method ~(keyword (str name))))) names)))

(defmacro def-obj-expr
  "Defines object expression"
  [name, evaluate, diff, toString]
  `(def ~name (create-object-expression ~evaluate ~diff ~toString)))

(defmacro def-obj-oper
  "Defines object operation"
  [name, op, eval-func, diff-func]
  `(def ~name (create-object-operation ~op ~eval-func ~diff-func)))

(defmacro def-obj-un-oper
  "Defines object unary operation"
  [name, op, eval-func, diff-func]
  `(def ~name (create-object-unary-operation ~op ~eval-func ~diff-func)))

;;------------------------------------------- Fields ----------------------------------------------

(def-fields -value, -terms, -arg, -op, -eval-func, -diff-func)

;;------------------------------------------- Methods ---------------------------------------------

(def-methods toString, toStringSuffix, toStringInfix, evaluate, diff)

;;-------------------------------------- Object's factories ---------------------------------------

(defn create-object-expression [evaluate, diff, toString]
  (let [ctor (fn [this, value] (assoc this :value value))
        proto
        {
         :toString       toString
         :toStringSuffix toString
         :toStringInfix  toString
         :evaluate       evaluate
         :diff           diff
         }]
    (constructor ctor proto)))

(def operation-proto
  {
   :toString
   (fn [this] (str "(" (-op this) " " (join " " (map toString (-terms this))) ")"))

   :toStringSuffix
   (fn [this] (str "(" (join " " (map toStringSuffix (-terms this))) " " (-op this) ")"))

   :toStringInfix
   (fn [this]
     (let [f (first (-terms this)) r (rest (-terms this)) op (-op this)]
       (reduce #(str "(" %1 " " op " " (toStringInfix %2) ")") (toStringInfix f) r)))

   :evaluate
   (fn [this, args-map] (apply (-eval-func this) (map #(evaluate % args-map) (-terms this))))

   :diff
   (fn [this, var-name] ((-diff-func this) (-terms this) (map #(diff % var-name) (-terms this))))
   })

(defn create-object-operation
  ([overload-map, op, eval-func, diff-func]
   (let [ctor (fn [this, & terms] (assoc this :terms terms))
         proto (merge
                 {
                  :prototype operation-proto
                  :op        op
                  :eval-func eval-func
                  :diff-func diff-func
                  }
                 overload-map)]
     (constructor ctor proto)))
  ([op, eval-func, diff-func]
   (create-object-operation {}, op, eval-func, diff-func)))

(def create-object-unary-operation
  (partial create-object-operation
           {
            :toStringInfix
            (fn [this] (str (-op this) "(" (toStringInfix (first (-terms this))) ")"))
            }
           ))

(def create-bitwise-operation
  (partial create-object-operation
           {
            :evaluate
            (fn [this, args-map]
              (apply (-eval-func this) (map #(> (evaluate % args-map) 0) (-terms this))))
            }
           ))

;;----------------------------------------- Declarations ------------------------------------------

(declare
  Constant, Variable, Negate, Add, Subtract, Multiply, Divide,
  Pow, Log, ArithMean, GeomMean, HarmMean
  )
(declare const-zero, const-one, const-two)

;;------------------------------------ Differentiation functions ----------------------------------

(defn neg-diff [_, terms-diff] (apply Negate terms-diff))

(defn add-diff [_, terms-diff] (apply Add terms-diff))

(defn sub-diff [_, terms-diff] (apply Subtract terms-diff))

(defn mul-diff [terms, terms-diff]
  (if (empty? terms)
    const-zero
    (Add
      (apply Multiply (first terms-diff) (rest terms))
      (Multiply (first terms) (mul-diff (rest terms) (rest terms-diff))))))

(defn div-diff [terms, terms-diff]
  (if (== (count terms) 1)
    (Negate (Divide (first terms-diff) (first terms) (first terms)))
    (Subtract
      (apply Divide (first terms-diff) (rest terms))
      (Multiply
        (apply Divide terms)
        (apply Add (map Divide (rest terms-diff) (rest terms)))))))

(defn pow-diff [[f, g], [df, dg]]
  (Multiply
    (Pow f (Subtract g const-one))
    (Add (Multiply df g) (Multiply f (Log f) dg))))

(defn log-diff [[f], [df]] (Divide df f))

(defn arith-mean-diff [terms, terms-diff]
  (Divide (add-diff terms terms-diff) (Constant (count terms))))

(defn geom-mean-diff [terms, terms-diff]
  (Multiply
    (Divide
      (apply Multiply terms)
      (Pow (apply GeomMean terms) (Constant (dec (* (count terms) 2))))
      (Constant (count terms)))
    (mul-diff terms terms-diff)))

(defn harm-mean-diff [terms, terms-diff]
  (Multiply
    (Constant (_div (count terms)))
    (Pow (apply HarmMean terms) const-two)
    (apply Add (map #(Divide %2 %1 %1) terms terms-diff))))

;;------------------------------------------ Constructors -----------------------------------------

(def-obj-expr Constant
              (fn [this, _] (-value this))
              (fn [_, _] const-zero)
              (fn [this] (format "%.1f" (double (-value this)))))

;;~~~~~~~~~ Constants ~~~~~~~~
(def const-zero (Constant 0))
(def const-one (Constant 1))
(def const-two (Constant 2))
;;~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(def-obj-expr Variable
              (fn [this, args-map] (args-map (str (Character/toLowerCase (nth (-value this) 0)))))
              (fn [this, var-name] (if (= (str (Character/toLowerCase (nth (-value this) 0))) var-name) const-one const-zero))
              (fn [this] (-value this)))

(def-obj-un-oper Negate "negate" - neg-diff)

(def-obj-oper Add "+" + add-diff)
(def-obj-oper Subtract "-" - sub-diff)
(def-obj-oper Multiply "*" * mul-diff)
(def-obj-oper Divide "/" _div div-diff)
(def-obj-oper Pow "pow" _pow pow-diff)
(def-obj-oper Log "log" _log log-diff)
(def-obj-oper ArithMean "arith-mean" _arith-mean arith-mean-diff)
(def-obj-oper GeomMean "geom-mean" _geom-mean geom-mean-diff)
(def-obj-oper HarmMean "harm-mean" _harm-mean harm-mean-diff)

(def And (create-bitwise-operation "&&" #(apply bit-and (mapv (fn [b] (if (true? b) 1 0)) %&)) nil))
(def Or (create-bitwise-operation "||" #(apply bit-or (mapv (fn [b] (if (true? b) 1 0)) %&)) nil))
(def Xor (create-bitwise-operation "^^" #(apply bit-xor (mapv (fn [b] (if (true? b) 1 0)) %&)) nil))

;;--------------------------------------------- Parser --------------------------------------------

(def object-var-map
  {
   'x (Variable "x")
   'y (Variable "y")
   'z (Variable "z")
   })

(def object-op-map
  {
   '+          Add
   '-          Subtract
   '*          Multiply
   '/          Divide
   'negate     Negate
   'pow        Pow
   'log        Log
   'arith-mean ArithMean
   'geom-mean  GeomMean
   'harm-mean  HarmMean
   (symbol "^^") Xor
   (symbol "||") Or
   (symbol "&&") And
   })

(def parseObject (create-parser object-var-map, object-op-map, Constant))

;;=================================================================================================
;;===================================== Combinatorial parsers =====================================
;;=================================================================================================

;;------------------------------------------- Import ----------------------------------------------

(load-file "parser.clj")

;;--------------------------------------- Common elements -----------------------------------------

(def all-chars (apply str (mapv char (range 0 128))))
(def digits (apply str (filter #(Character/isDigit (char %)) all-chars)))
(def letters (apply str (filter #(Character/isLetter (char %)) all-chars)))
(def spaces (apply str (filter #(Character/isWhitespace (char %)) all-chars)))
(def op-chars
  (apply str (filter
               #(and
                  (not (or (Character/isDigit (char %)) (Character/isWhitespace (char %))))
                  (not ((set "()[]{}") %)))
               all-chars)))

(def *integer (+str (+plus (+char digits))))
(def *number (+seqf
               (comp Constant read-string str)
               (+opt (+char "-")) *integer (+opt (+seqf str (+char ".") *integer))))

(def *ws (+ignore (+star (+char spaces))))

(def *variable (+map Variable (+str (+plus (+char "xyzXYZ")))))

(def *operator (+map (comp object-op-map symbol) (+str (+plus (+char op-chars)))))

;;---------------------------------------- Suffix parser ------------------------------------------

(defparser parseObjectSuffix
           *operation (+seqf
                        #(apply %2 %1)
                        (+ignore \() (+star (delay *value)) *ws *operator *ws (+ignore \)))
           *value (+seqn 0 *ws (+or *number *variable *operation) *ws)
           *parseObjectSuffix *value)

;;----------------------------------------- Infix parser ------------------------------------------

(defparser parseObjectInfix
           (make-operation [val-1, [[op val-2], & tail]]
                           (if (empty? tail)
                             (if op (op val-1 val-2) val-1)
                             (make-operation (op val-1 val-2) tail)))
           (*spec-op [& ops]
                     (+map
                       (comp object-op-map symbol)
                       (apply +or (map #(apply +seqf str (map (comp +char str) (vec %))) ops))))
           oper-levels [["^^"] ["||"] ["&&"] ["+", "-"] ["*", "/"]]
           element-lvl (count oper-levels)
           (*level [lvl]
                   (if (== lvl element-lvl)
                     (delay *element)
                     (let [*next-level (*level (inc lvl))]
                       (+seqf
                         make-operation
                         *next-level
                         (+star (+seq (apply *spec-op (oper-levels lvl)) *next-level))))))
           *unary-op (+seqf #(%1 %2) *operator (delay *element))
           *element (+seqn 0 *ws (+or *number *variable *unary-op (+seqn 1 \( (*level 0) \))) *ws)
           *parseObjectInfix (*level 0))

;;=================================================================================================
