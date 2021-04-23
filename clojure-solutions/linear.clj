;Utility
(defn check-near [f arr]
  (first (reduce
           (fn [[res a] b]
             (vector (and res (f a b)) b))
           [true (first arr)] (rest arr))))


;Vector-utility
(defn vecs? [& vecs]
  (and (every? vector? vecs) (every? #(every? number? %) vecs) (apply == (mapv count vecs))))
(defn vec-vecs [f]
  (fn [& vs]
    {:pre [(apply vecs? vs)]}
    (apply mapv f vs)))
(defn vec-nums [f]
  (fn [v & nums]
    {:pre [(vecs? v) (every? number? nums)]}
    (mapv #(apply f % nums) v)))

;Vectors
(def v+ (vec-vecs +))
(def v- (vec-vecs -))
(def v* (vec-vecs *))
(def vd (vec-vecs /))
(defn scalar [& vs]
  {:pre [(apply vecs? vs)]}
  (apply + (apply v* vs)))
(defn vect [& vs]
  {:pre [(apply vecs? vs) (apply == 3 (mapv count vs))]}
  (letfn [(minor [va vb i j]
            (- (* (va i) (vb j)) (* (va j) (vb i))))]
    (reduce #(vector (minor %1 %2 1 2) (minor %1 %2 2 0) (minor %1 %2 0 1)) vs)))
(def v*s (vec-nums *))


;Matrix-utility
(defn matrix? [m] (and (vector? m) (apply vecs? m)))
(defn m-height [m] (count m))
(defn m-width [m] (count (m 0)))
(defn m-ms [f]
  (fn [& ms]
    {:pre [(every? matrix? ms) (apply == (mapv m-height ms)) (apply == (mapv m-width ms))]}
    (apply mapv f ms)))
(defn m-vec [f]
  (fn [m vec]
    {:pre [(matrix? m) (vecs? vec)]}
    (mapv #(f % vec) m)))
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
(def m*v (m-vec scalar))
(defn transpose [m]
  {:pre [(matrix? m)]}
  (apply mapv vector m))
(defn m*m [& ms]
  {:pre [(check-near #(and (matrix? %1) (matrix? %2) (== (m-width %1) (m-height %2))) ms)]}
  (reduce (fn [ma mb] (let [bt (transpose mb)] (mapv #(m*v bt %) ma))) ms))


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
  (letfn [(ts-op [& ts]
            (if (number? (first ts))
              (apply f ts)
              (apply mapv ts-op ts)))]
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
(defn do-broadcasts [& ts]
  (let [f (reduce major-form (mapv form ts))]
    (mapv #(do-broadcast % f) ts)))
(defn tb-tbs [f]
  (fn [& tbs]
    {:pre [(every? tensor? tbs) (reduce major-form (mapv form tbs))]}
    (let [tb-op (t-ts f)]
      (apply tb-op (apply do-broadcasts tbs)))))

;Broadcasts
(def tb+ (tb-tbs +))
(def tb- (tb-tbs -))
(def tb* (tb-tbs *))
(def tbd (tb-tbs /))
