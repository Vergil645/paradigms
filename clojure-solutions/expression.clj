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

;;------------------------------------- Secondary functions ---------------------------------------

(def join clojure.string/join)

(defn int-bool-int [x] (if (> x 0) 1 0))
(defn bool-int [b] (if (true? b) 1 0))

;;----------------------------------------- Declarations ------------------------------------------
(declare _Constant _Variable _Negate _Add _Subtract _Multiply _Divide
         _ArithMean _GeomMean _HarmMean)

(declare const-zero const-one const-two)

(declare Constant Variable Negate Add Subtract Multiply Divide ArithMean GeomMean HarmMean)

;;-------------------------------------- Object's factories ---------------------------------------

(defclass _Expression
          _
          [value]
          [toString [] (_value this)]
          [toStringSuffix [] (_toString this)]
          [toStringInfix [] (_toString this)]
          )

(defclass _Constant
          _Expression
          []
          [evaluate [_] (_value this)]
          [diff [_] const-zero]
          [toString [] (format "%.1f" (double (_value this)))]
          )
(defclass _Variable
          _Expression
          []
          [first-letter [] (str (Character/toLowerCase (char (nth (_value this) 0))))]
          [evaluate [args-map] (args-map (_first-letter this))]
          [diff [var-name] (if (= (_first-letter this) var-name) const-one const-zero)]
          )

(defclass _Left-operation
          _
          [terms]
          [op [] nil]
          [eval-func [] nil]
          [diff-func [terms terms-diff] nil]
          [evaluate [args-map]
           (apply _eval-func this (map #(_evaluate % args-map) (_terms this)))]
          [diff [var-name]
           (_diff-func this (_terms this) (map #(_diff % var-name) (_terms this)))]
          [toString []
           (str "(" (_op this) " " (join " " (map _toString (_terms this))) ")")]
          [toStringSuffix []
           (str "(" (join " " (map _toStringSuffix (_terms this))) " " (_op this) ")")]
          [toStringInfix []
           (let [f (first (_terms this)) r (rest (_terms this)) op (_op this)]
             (reduce #(str "(" %1 " " op " " (_toStringInfix %2) ")") (_toStringInfix f) r))]
          )
(defclass _Un-operation
          _Left-operation
          []
          [toStringInfix [] (str (_op this) "(" (_toStringInfix (first (_terms this))) ")")]
          )

(defclass _Negate
          _Un-operation
          []
          [op [] "negate"]
          [eval-func [& args] (apply - args)]
          [diff-func [terms terms-diff] (_Negate terms-diff)]
          )
(defclass _Add
          _Left-operation
          []
          [op [] "+"]
          [eval-func [& args] (apply + args)]
          [diff-func [terms terms-diff] (_Add terms-diff)]
          )
(defclass _Subtract
          _Left-operation
          []
          [op [] "-"]
          [eval-func [& args] (apply - args)]
          [diff-func [terms terms-diff] (_Subtract terms-diff)]
          )
(defclass _Multiply
          _Left-operation
          []
          [op [] "*"]
          [eval-func [& args] (apply * args)]
          [diff-func [terms terms-diff]
           (second (reduce (fn [[f df] [g dg]]
                             [(_Multiply [f g])
                              (_Add [(_Multiply [f dg]) (_Multiply [df g])])])
                           (map vector terms terms-diff)))]
          )
(defclass _Divide
          _Left-operation
          []
          [op [] "/"]
          [eval-func [& args] (apply _div args)]
          [diff-func [terms terms-diff]
           (if (== (count terms) 1)
             (_Negate [(_Divide [(first terms-diff) (first terms) (first terms)])])
             (_Subtract
               [(_Divide (cons (first terms-diff) (rest terms)))
                (_Multiply
                  [(_Divide terms)
                   (_Add (map Divide (rest terms-diff) (rest terms)))])]))]
          )

(defclass _ArithMean
          _Left-operation
          []
          [op [] "arith-mean"]
          [eval-func [& args] (apply _arith-mean args)]
          [diff-func [terms terms-diff]
           (_Multiply [(_Add terms-diff) (_Constant (_div (count terms)))])]
          )
(defclass _GeomMean
          _Left-operation
          []
          [op [] "geom-mean"]
          [eval-func [& args] (apply _geom-mean args)]
          [diff-func [terms terms-diff]
           (_Multiply
             [(_Constant (_div (count terms)))
              (_GeomMean terms)
              (_Add (map #(_Divide [%2 %1]) terms terms-diff))])]
          )
(defclass _HarmMean
          _Left-operation
          []
          [op [] "harm-mean"]
          [eval-func [& args] (apply _harm-mean args)]
          [diff-func [terms terms-diff]
           (_Multiply
             [(_Constant (_div (count terms)))
              (_HarmMean terms)
              (_HarmMean terms)
              (_Add (map #(_Divide [%2 %1 %1]) terms terms-diff))])]
          )

(defclass _Bool-operation
          _Left-operation
          []
          [evaluate [args-map]
           (apply _eval-func this (map #(int-bool-int (_evaluate % args-map)) (_terms this)))]
          )
(defclass _Right-operation
          _Left-operation
          []
          [toStringInfix []
           (let [rev (reverse (_terms this)) l (first rev) h (rest rev) op (_op this)]
             (reduce #(str "(" (_toStringInfix %2) " " op " " %1 ")") (_toStringInfix l) h))]
          )

(defclass _And
          _Bool-operation
          []
          [op [] "&&"]
          [eval-func [& args] (apply bit-and args)]
          )
(defclass _Or
          _Bool-operation
          []
          [op [] "||"]
          [eval-func [& args] (apply bit-or args)]
          )
(defclass _Xor
          _Bool-operation
          []
          [op [] "^^"]
          [eval-func [& args] (apply bit-xor args)]
          )
(defclass _Iff
          _Bool-operation
          []
          [op [] "<->"]
          [eval-func [& args] (bool-int (apply == args))]
          )
(defclass _Impl
          _Right-operation
          []
          [op [] "->"]
          [eval-func [& args] (reduce #(if (and (> %1 0) (<= %2 0)) 0 1) args)])

;;------------------------------------------- Bindings --------------------------------------------
(def const-zero (_Constant 0))
(def const-one (_Constant 1))
(def const-two (_Constant 2))

(defmacro bind-ctors [& names]
  `(do ~@(mapv (fn [name] `(defn ~name [& terms#] (~(to-symbol name) terms#))) names)))

(defmacro bind-methods [& names]
  `(do ~@(mapv (fn [name] `(def ~name ~(to-symbol name))) names)))

(def Constant _Constant)
(def Variable _Variable)
(bind-ctors Negate Add Subtract Multiply Divide ArithMean GeomMean HarmMean And Or Xor Iff Impl)
(bind-methods evaluate diff toString toStringSuffix toStringInfix)

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
(defn left-assoc? [str-op] (:left-assoc (obj-op-map (symbol str-op))))

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
