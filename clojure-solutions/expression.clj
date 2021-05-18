;;=================================================================================================
;;========================================== FUNCTIONS ============================================
;;=================================================================================================

;;------------------------------------------ Factories --------------------------------------------

(defn create-operation [func]
  (fn [& expressions]
    (fn [args-map] (apply func (map #(% args-map) expressions)))))

(defmacro def-func-oper
  "Defines function operation"
  [name, func]
  `(def ~name (create-operation ~func)))

(defn create-parser [var-map get-op const]
  (letfn [(parse-element [elem]
            (cond
              (number? elem) (const elem)
              (contains? var-map elem) (var-map elem)
              (list? elem) (apply (get-op (first elem)) (mapv parse-element (rest elem)))))]
    (fn [expr]
      (parse-element (read-string expr)))))

;;------------------------------------- Evaluate functions ----------------------------------------

(defn _div
  ([x] (/ 1.0 (double x)))
  ([x & xs] (/ (double x) (apply * xs))))

(defn _sqr [x] (* x x))

(defn at-least-one-arg [f]
  (fn [x & xs] (apply f (cons x xs))))

(defmacro def-at-least-one-arg
  "Defines function which take at least one argument"
  [name, func]
  `(def ~name (at-least-one-arg ~func)))

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

;;------------------------------------------- Fields ----------------------------------------------

(defmacro def-fields
  "Defines multiple fields"
  [& names]
  `(do ~@(mapv (fn [name] `(def ~name (field ~(keyword (subs (str name) 1))))) names)))

(def-fields -value, -terms, -arg, -op, -eval-func, -diff-func)

;;------------------------------------------- Methods ---------------------------------------------

(defmacro def-methods
  "Defines multiple methods"
  [& names]
  `(do ~@(mapv (fn [name] `(def ~name (method ~(keyword (str name))))) names)))

(def-methods toString, toStringSuffix, toStringInfix, evaluate, diff, get-first)

;;------------------------------------- Secondary functions ---------------------------------------

(def join clojure.string/join)

(defn int-bool-int [x] (if (> x 0) 1 0))
(defn bool-int [b] (if (true? b) 1 0))

;;-------------------------------------- Object's factories ---------------------------------------

;; Expression
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

(defmacro def-obj-expr
  "Defines object expression"
  ([name, evaluate, diff, toString]
   `(def ~name (create-obj-expr ~evaluate ~diff ~toString)))
  ([name, evaluate, diff, toString, overload-map]
   `(def ~name (create-obj-expr ~overload-map ~evaluate ~diff ~toString))))

;; Operation
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

(defmacro def-obj-oper
  "Defines object operation"
  ([name, op, eval-func, diff-func]
   `(def ~name (create-obj-oper ~op ~eval-func ~diff-func)))
  ([name, op, eval-func, diff-func, overload-map]
   `(def ~name (create-obj-oper ~overload-map ~op ~eval-func ~diff-func))))

;; Unary operation
(def create-obj-un-oper
  (partial create-obj-oper
           {:toStringInfix
            (fn [this] (str (-op this) "(" (toStringInfix (first (-terms this))) ")"))
            }))

(defmacro def-obj-un-oper
  "Defines object unary operation"
  [name, op, eval-func, diff-func]
  `(def ~name (create-obj-un-oper ~op ~eval-func ~diff-func)))

;; Boolean operation
(def create-obj-bool-oper
  (partial create-obj-oper
           {:evaluate
            (fn [this, args-map]
              (apply (-eval-func this) (map #(int-bool-int (evaluate % args-map)) (-terms this))))
            }))

(defmacro def-obj-bool-oper
  "Defines object boolean operation"
  [name, op, eval-func, diff-func]
  `(def ~name (create-obj-bool-oper ~op ~eval-func ~diff-func)))

;; Right associative operation
(def create-obj-right-assoc
  (partial create-obj-oper
           {:toStringInfix
            (fn [this]
              (let [rev (reverse (-terms this)) l (first rev) h (rest rev) op (-op this)]
                (reduce #(str "(" (toStringInfix %2) " " op " " %1 ")") (toStringInfix l) h)))
            }))

(defmacro def-obj-right-assoc
  "Defines right associative operation"
  [name, op, eval-func, diff-func]
  `(def ~name (create-obj-right-assoc ~op ~eval-func ~diff-func)))

;;----------------------------------------- Declarations ------------------------------------------

(declare Constant Variable Negate Add Subtract Multiply Divide ArithMean GeomMean HarmMean)
(declare const-zero const-one const-two)

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
              (fn [this, args-map] (args-map (get-first this)))
              (fn [this, var-name] (if (= (get-first this) var-name) const-one const-zero))
              (fn [this] (-value this))
              {:get-first (fn [this] (str (Character/toLowerCase (char (nth (-value this) 0)))))})

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

(def obj-var-map
  {
   'x (Variable "x")
   'y (Variable "y")
   'z (Variable "z")
   })

(defn make-map
  ([ctor] (make-map ctor :left-assoc))
  ([ctor & flags] (reduce #(assoc %1 %2 true) {:ctor ctor} flags)))

(defmacro def-op-map
  "Defines operations map"
  [name & terms]
  (let [res-map (reduce #(assoc %1 `(symbol ~(first %2)) (apply make-map (rest %2))) {} terms)]
    `(def ~name ~res-map)))

(def-op-map obj-op-map
            ["+" Add]
            ["-" Subtract]
            ["*" Multiply]
            ["/" Divide]
            ["negate" Negate]
            ["arith-mean" ArithMean]
            ["geom-mean" GeomMean]
            ["harm-mean" HarmMean]
            ["^^" Xor]
            ["||" Or]
            ["&&" And]
            ["<->" Iff]
            ["->" Impl :right-assoc]
            )

(def get-ctor (comp :ctor obj-op-map))

(def parseObject (create-parser obj-var-map, get-ctor, Constant))

;;=================================================================================================
;;===================================== Combinatorial parsers =====================================
;;=================================================================================================

;;------------------------------------------- Import ----------------------------------------------

(load-file "parser.clj")

;;------------------------------------- Secondary functions ---------------------------------------

(defn is-digit [ch] (Character/isDigit (char ch)))
(defn is-letter [ch] (Character/isLetter (char ch)))
(defn is-whitespace [ch] (Character/isWhitespace (char ch)))

;;----------------------------------------- Infix parser ------------------------------------------
(defparser parseObjectInfix
           oper-levels [["<->"] ["->"] ["^^"] ["||"] ["&&"] ["+", "-"] ["*", "/"]]
           unary-ops ["negate"]
           element-lvl (count oper-levels)

           all-chars (apply str (mapv char (range 0 128)))
           spaces (apply str (filter is-whitespace all-chars))
           digits (apply str (filter is-digit all-chars))

           *ws (+ignore (+star (+char spaces)))

           *integer (+str (+plus (+char digits)))
           *number (+seqf (comp Constant read-string str)
                          (+opt (+char "-")) *integer (+opt (+seqf str (+char ".") *integer)))

           *variable (+map Variable (+str (+plus (+char "xyzXYZ"))))
           *unary-op (+seqf #(%1 %2) (*spec-op unary-ops) (delay *element))

           *element (+seqn 0 *ws (+or *number *variable *unary-op (+seqn 1 \( (*level 0) \))) *ws)

           (left-assoc? [str-op] (:left-assoc (obj-op-map (symbol str-op))))

           (*spec-op [ops]
                     (+map (comp get-ctor symbol)
                           (apply +or (map #(apply +seqf str (map (comp +char str) %)) ops))))

           (make-obj [is-left-assoc]
                     (fn rec [expr-1, [[ctor expr-2] & tail]]
                       (if (empty? tail)
                         (if ctor (ctor expr-1 expr-2) expr-1)
                         (if is-left-assoc
                           (rec (ctor expr-1 expr-2) tail)
                           (ctor expr-1 (rec expr-2 tail))))))
           (*level [lvl]
                   (if (== lvl element-lvl)
                     (delay *element)
                     (let [*next-level (*level (inc lvl))]
                       (+seqf (make-obj (left-assoc? (first (oper-levels lvl))))
                              *next-level
                              (+star (+seq (*spec-op (oper-levels lvl)) *next-level))))))

           *parseObjectInfix (*level 0))

;;=================================================================================================

;(make-left [expr-1, [[ctor expr-2], & tail]]
;           (if (empty? tail)
;             (if ctor (ctor expr-1 expr-2) expr-1)
;             (make-left (ctor expr-1 expr-2) tail)))
;(make-right [expr-1, [[ctor expr-2], & tail]]
;            (if (empty? tail)
;              (if ctor (ctor expr-1 expr-2) expr-1)
;              (ctor expr-1 (make-right expr-2 tail))))

;(defparser parseObjectInfix
;           oper-levels [["<->"] ["->"] ["^^"] ["||"] ["&&"] ["+" "-"] ["*" "/"]]
;           element-lvl (count oper-levels)
;           (make-left-assoc [val-1, [[ctor val-2], & tail]]
;                            (if (empty? tail)
;                              (ctor val-1 val-2)
;                              (make-left-assoc (ctor val-1 val-2) tail)))
;           (*left-assoc [*operator, *next-level]
;                        (+seqf make-left-assoc
;                               *next-level
;                               (+plus (+seq *operator (*left-assoc [*operator, *next-level])))))
;           (*level [lvl]
;                   (if (== lvl element-lvl)
;                     *element
;                     (let [is-left-assoc (:left-assoc (obj-op-map (first (oper-levels lvl))))
;                           *next-level (*level (inc lvl))
;                           *operator (*spec-op (oper-levels lvl))]
;                       (+or (if is-left-assoc
;                              (*left-assoc *operator *next-level)
;                              (*right-assoc *operator *next-level))
;                            *element))
;                     ))
;           *element (+seqn 0 *ws (+or *number *variable *unary-op (+seqn 1 \( (*level 0) \))) *ws)
;           *parseObjectInfix (*level 0))
