;Vector-utility
(defn vecs? [& vecs]
  (and (every? vector? vecs) (every? #(every? number? %) vecs) (apply == (mapv count vecs))))
(defn vec-vecs [f]
  (fn [& vs]
    {:pre [(apply vecs? vs)]}
    (apply mapv f vs)))
(defn vec-nums [f]
  (fn [v & nums]
    {:pre [(and (vecs? v) (every? number? nums))]}
    (mapv #(apply f % nums) v)))


;Matrix-utility
(defn matrix? [m] (and (vector? m) (apply vecs? m)))
(defn m-height [m] (count m))
(defn m-width [m] (count (nth m 0)))
(defn m-ms [f]
  (fn [& ms]
    {:pre [(and (every? matrix? ms) (apply == (mapv m-height ms)) (apply == (mapv m-width ms)))]}
    (apply mapv f ms)))
(defn m-vec [f]
  (fn [m vec]
    {:pre [(and (matrix? m) (vecs? vec))]}
    (mapv #(f % vec) m)))
(defn m-nums [f]
  (fn [m & nums]
    {:pre [(and (matrix? m) (every? number? nums))]}
    (let [vec-op (vec-nums f)]
      (mapv #(apply vec-op % nums) m))))


;Vectors
(def v+ (vec-vecs +))
(def v- (vec-vecs -))
(def v* (vec-vecs *))
(def vd (vec-vecs /))
(defn scalar [& vs]
  {:pre [(apply vecs? vs)]}
  (apply + (apply v* vs)))
(defn vect [& vs]
  {:pre [(and (apply vecs? vs) (apply == 3 (mapv count vs)))]}
  (letfn [(minor [va vb i j]
            (- (* (nth va i) (nth vb j)) (* (nth va j) (nth vb i))))]
    (reduce #(vector (minor %1 %2 1 2) (minor %1 %2 2 0) (minor %1 %2 0 1)) vs)))
(def v*s (vec-nums *))


;Matrices
(def m+ (m-ms v+))
(def m- (m-ms v-))
(def m* (m-ms v*))
(def md (m-ms vd))
(def m*s (m-nums *))
(def m*v (m-vec scalar))
(defn transpose [m]
  {:pre [(matrix? m)]}
  (reduce #(mapv conj %1 %2) (vec (repeat (m-width m) [])) m))
(defn m*m [& ms]
  (letfn [(m*m-bin [ma mb]
            {:pre [(and (matrix? ma) (matrix? mb) (== (m-width ma) (m-height mb)))]}
            (let [bt (transpose mb)] (mapv #(m*v bt %) ma)))]
    (reduce m*m-bin ms)))
