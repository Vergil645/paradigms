;Utility
(defn check-near [f arr]
  (first (reduce
           (fn [[res a] b] (vector (and res (f a b)) b))
           [true (first arr)] (rest arr))))


;Vector-utility
(defn vecs? [v & vs]
  (let [vvs (cons v vs)]
    (and (every? vector? vvs) (every? #(every? number? %) vvs) (apply == (mapv count vvs)))))
(defn v-vs [f]
  (fn [v & vs]
    {:pre [(apply vecs? v vs)]}
    (apply mapv f v vs)))
(defn v-nums [f]
  (fn [v & nums]
    {:pre [(vecs? v) (every? number? nums)]}
    (mapv #(apply f % nums) v)))

;Vectors
(def v+ (v-vs +))
(def v- (v-vs -))
(def v* (v-vs *))
(def vd (v-vs /))
(defn scalar [v & vs]
  {:pre [(apply vecs? v vs)]}
  (apply + (apply v* v vs)))
(defn vect [v & vs]
  {:pre [(apply vecs? v vs) (apply == 3 (count v) (mapv count vs))]}
  (letfn [(minor [va vb i j]
            (- (* (va i) (vb j)) (* (va j) (vb i))))]
    (reduce #(vector (minor %1 %2 1 2) (minor %1 %2 2 0) (minor %1 %2 0 1)) v vs)))
(def v*s (v-nums *))


;Matrix-utility
(defn matrix? [m] (and (vector? m) (apply vecs? m)))
(defn m-height [m] (count m))
(defn m-width [m] (count (m 0)))
(defn m-ms [f]
  (fn [m & ms]
    {:pre [(let [mms (cons m ms)]
             (and (every? matrix? mms) (apply == (mapv m-height mms)) (apply == (mapv m-width mms))))]}
    (apply mapv f m ms)))
(defn m-v [f]
  (fn [m v]
    {:pre [(matrix? m) (vecs? v)]}
    (mapv #(f % v) m)))
(defn m-nums [f]
  (fn [m & nums]
    {:pre [(matrix? m) (every? number? nums)]}
    (mapv (fn [mx] (mapv #(apply f % nums) mx)) m)))

;Matrices
(def m+ (m-ms v+))
(def m- (m-ms v-))
(def m* (m-ms v*))
(def md (m-ms vd))
(def m*s (m-nums *))
(def m*v (m-v scalar))
(defn transpose [m]
  {:pre [(matrix? m)]}
  (apply mapv vector m))
(defn m*m [m & ms]
  {:pre [(check-near #(and (matrix? %1) (matrix? %2) (== (m-width %1) (m-height %2))) (cons m ms))]}
  (reduce (fn [ma mb] (let [bt (transpose mb)] (mapv #(m*v bt %) ma))) m ms))


;Tensor-utility
(defn form [t]
  (if (vector? t)
    (apply vector (count t) (form (first t)))
    []))
(defn tensor? [t]
  (cond
    (number? t) true
    (vector? t) (and (not (empty? t)) (every? tensor? t) (apply = (mapv form t)))))
(defn t-ts [f]
  (letfn [(ts-op [t & ts]
            (if (number? t)
              (apply f t ts)
              (apply mapv ts-op t ts)))]
    ts-op))


;Broadcast-utility
(defn major-form [fma fmb]
  (let [ca (count fma) cb (count fmb)]
    (cond
      (and (<= ca cb) (= fma (subvec fmb 0 ca))) fmb
      (and (< cb ca) (= fmb (subvec fma 0 cb))) fma)))
(defn make-form [num fm]
  (if (empty? fm)
    num
    (vec (repeat (first fm) (make-form num (rest fm))))))
(defn do-broadcast [ta fmb]
  (letfn [(rec [t fm] (if (number? t)
                        (make-form t fm)
                        (mapv #(rec % fm) t)))]
    (rec ta (subvec fmb (count (form ta))))))
(defn do-broadcasts [t & ts]
  (let [f (reduce major-form (form t) (mapv form ts))]
    (mapv #(do-broadcast % f) (cons t ts))))
(defn tb-tbs [f]
  (fn [t & ts]
    {:pre [(every? tensor? (cons t ts)) (reduce major-form (form t) (mapv form ts))]}
    (let [tb-op (t-ts f)]
      (apply tb-op (apply do-broadcasts t ts)))))

;Broadcasts
(def tb+ (tb-tbs +))
(def tb- (tb-tbs -))
(def tb* (tb-tbs *))
(def tbd (tb-tbs /))
