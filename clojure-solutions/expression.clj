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
  ([name, evaluate, diff, toString]
   `(def ~name (create-obj-expr ~evaluate ~diff ~toString)))
  ([name, overload-map, evaluate, diff, toString]
   `(def ~name (create-obj-expr ~overload-map ~evaluate ~diff ~toString))))

(defmacro def-obj-oper
  "Defines object operation"
  [name, op, eval-func, diff-func]
  `(def ~name (create-obj-oper ~op ~eval-func ~diff-func)))

(defmacro def-obj-un-oper
  "Defines object unary operation"
  [name, op, eval-func, diff-func]
  `(def ~name (create-obj-un-oper ~op ~eval-func ~diff-func)))

(defmacro def-obj-bool-oper
  "Defines object boolean operation"
  [name, op, eval-func, diff-func]
  `(def ~name (create-obj-bool-oper ~op ~eval-func ~diff-func)))

(defmacro def-obj-right-assoc
  "Defines right associative operation"
  [name, op, eval-func, diff-func]
  `(def ~name (create-obj-right-assoc ~op ~eval-func ~diff-func)))

;;------------------------------------------- Fields ----------------------------------------------

(def-fields -value, -terms, -arg, -op, -eval-func, -diff-func)

;;------------------------------------------- Methods ---------------------------------------------

(def-methods toString, toStringSuffix, toStringInfix, evaluate, diff,
             get-first)

;;------------------------------------- Secondary functions ---------------------------------------

(def join clojure.string/join)

(defn int-bool-int [x] (if (> x 0) 1 0))
(defn bool-int [b] (if (true? b) 1 0))

;;-------------------------------------- Object's factories ---------------------------------------

(defn create-obj-expr
  ([overload-map, evaluate, diff, toString]
   (let [ctor (fn [this, value] (assoc this :value value))
         proto (merge {
                       :toString       toString
                       :toStringSuffix toString
                       :toStringInfix  toString
                       :evaluate       evaluate
                       :diff           diff
                       }
                      overload-map)]
     (constructor ctor proto)))
  ([evaluate, diff, toString]
   (create-obj-expr {}, evaluate, diff, toString)))

(def oper-proto
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

(defn create-obj-oper
  ([overload-map, op, eval-func, diff-func]
   (let [ctor (fn [this, & terms] (assoc this :terms terms))
         proto (merge
                 {
                  :prototype oper-proto
                  :op        op
                  :eval-func eval-func
                  :diff-func diff-func
                  }
                 overload-map)]
     (constructor ctor proto)))
  ([op, eval-func, diff-func]
   (create-obj-oper {}, op, eval-func, diff-func)))

(def create-obj-un-oper
  (partial create-obj-oper
           {:toStringInfix
            (fn [this] (str (-op this) "(" (toStringInfix (first (-terms this))) ")"))
            }))

(def create-obj-right-assoc
  (partial create-obj-oper
           {:toStringInfix
            (fn [this]
              (let [rev (reverse (-terms this)) l (first rev) h (rest rev) op (-op this)]
                (reduce #(str "(" (toStringInfix %2) " " op " " %1 ")") (toStringInfix l) h)))
            }))

(def create-obj-bool-oper
  (partial create-obj-oper
           {:evaluate
            (fn [this, args-map]
              (apply (-eval-func this) (map #(int-bool-int (evaluate % args-map)) (-terms this))))
            }))

;;----------------------------------------- Declarations ------------------------------------------

(declare
  Constant, Variable, Negate, Add, Subtract, Multiply, Divide, ArithMean, GeomMean, HarmMean
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

(defn arith-mean-diff [terms, terms-diff]
  (Divide (add-diff terms terms-diff) (Constant (count terms))))

(defn geom-mean-diff [terms, terms-diff]
  (Multiply
    (Constant (_div (count terms)))
    (apply GeomMean terms)
    (apply Add (map #(Divide %2 %1) terms terms-diff))))

(defn harm-mean-diff [terms, terms-diff]
  (Multiply
    (Constant (_div (count terms)))
    (apply HarmMean terms)
    (apply HarmMean terms)
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
              {:get-first (fn [this] (str (Character/toLowerCase (char (nth (-value this) 0)))))}
              (fn [this, args-map] (args-map (get-first this)))
              (fn [this, var-name] (if (= (get-first this) var-name) const-one const-zero))
              (fn [this] (-value this)))

(def-obj-un-oper Negate "negate" - neg-diff)

(def-obj-oper Add "+" + add-diff)
(def-obj-oper Subtract "-" - sub-diff)
(def-obj-oper Multiply "*" * mul-diff)
(def-obj-oper Divide "/" _div div-diff)

(def-obj-oper ArithMean "arith-mean" _arith-mean arith-mean-diff)
(def-obj-oper GeomMean "geom-mean" _geom-mean geom-mean-diff)
(def-obj-oper HarmMean "harm-mean" _harm-mean harm-mean-diff)

(def-obj-bool-oper And "&&" bit-and nil)
(def-obj-bool-oper Or "||" bit-or nil)
(def-obj-bool-oper Xor "^^" bit-xor nil)
(def-obj-bool-oper Iff "<->" #(bool-int (apply == %&)) nil)
(def-obj-right-assoc Impl "->" (fn [& xs] (reduce #(if (and (> %1 0) (<= %2 0)) 0 1) xs)) nil)

;;--------------------------------------------- Parser --------------------------------------------

(def object-var-map
  {
   'x (Variable "x")
   'y (Variable "y")
   'z (Variable "z")
   })

(def object-op-map
  {
   '+             Add
   '-             Subtract
   '*             Multiply
   '/             Divide
   'negate        Negate
   'arith-mean    ArithMean
   'geom-mean     GeomMean
   'harm-mean     HarmMean
   (symbol "^^")  Xor
   (symbol "||")  Or
   (symbol "&&")  And
   (symbol "<->") Iff
   (symbol "->")  Impl
   })

(def parseObject (create-parser object-var-map, object-op-map, Constant))

;;=================================================================================================
;;===================================== Combinatorial parsers =====================================
;;=================================================================================================

;;------------------------------------------- Import ----------------------------------------------

(load-file "parser.clj")

;;------------------------------------- Secondary functions ---------------------------------------

(defn is-digit [ch] (Character/isDigit (char ch)))
(defn is-letter [ch] (Character/isLetter (char ch)))
(defn is-whitespace [ch] (Character/isWhitespace (char ch)))

;;--------------------------------------- Common elements -----------------------------------------

(def all-chars (apply str (mapv char (range 0 128))))
(def digits (apply str (filter is-digit all-chars)))
(def letters (apply str (filter is-letter all-chars)))
(def spaces (apply str (filter is-whitespace all-chars)))

(def *ws (+ignore (+star (+char spaces))))

(def *integer (+str (+plus (+char digits))))
(def *number
  (+seqf
    (comp Constant read-string str)
    (+opt (+char "-")) *integer (+opt (+seqf str (+char ".") *integer))))

(def *variable (+map Variable (+str (+plus (+char "xyzXYZ")))))
(def *operator
  (+map
    (comp object-op-map symbol)
    (apply +or (map #(apply +seqf str (map (comp +char str) (vec (str %)))) (keys object-op-map)))))

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
           (make-right-assoc [val-1, [[op val-2], & tail]]
                             (if (empty? tail)
                               (if op (op val-1 val-2) val-1)
                               (op val-1 (make-right-assoc val-2 tail))))
           (*spec-op [ops]
                     (+map
                       (comp object-op-map symbol)
                       (apply +or (map #(apply +seqf str (mapv (comp +char str) (vec %))) ops))))
           oper-levels [["<->"] ["->"] ["^^"] ["||"] ["&&"] ["+", "-"] ["*", "/"]]
           element-lvl (count oper-levels)
           (*level [lvl]
                   (if (== lvl element-lvl)
                     (delay *element)
                     (let [*next-level (*level (inc lvl))]
                       (+seqf
                         (if (== lvl 1) make-right-assoc make-operation)
                         *next-level
                         (+star (+seq (*spec-op (oper-levels lvl)) *next-level))))))
           *unary-op (+seqf #(%1 %2) (*spec-op ["negate"]) (delay *element))
           *element (+seqn 0 *ws (+or *number *variable *unary-op (+seqn 1 \( (*level 0) \))) *ws)
           *parseObjectInfix (*level 0))

;(defparser parseObjectInfix
;           oper-levels [["<->"] ["->"] ["^^"] ["||"] ["&&"] ["+", "-"] ["*", "/"]]
;           element-lvl (count oper-levels)
;           (*left-assoc [*op, *next-level]
;                        (+or (+seqf #()
;                                    (+or))
;                             *element))
;           (*level [lvl]
;                   (cond
;                     (== lvl element-lvl) *element
;                     (= (first (oper-levels lvl)) left) (+or () *element)
;                     :else ()
;                     ))
;           *element (+seqn 0 *ws (+or *number *variable *unary-op (+seqn 1 \( (*level 0) \))) *ws)
;           *parseObjectInfix (*level 0))
;;=================================================================================================
