(ns cljss.mixins)

;;
;; Theme
;;

(def -default-theme
  {:space     [0 8 16 32 64]
   :font-size [12 14 16 20 24 32 48 64 72]})

(def theme* (atom -default-theme))

;;
;; Utils
;;

(defn -px [val]
  (str val "px"))

(defn -% [val]
  (str val "%"))

;;
;; Spacing (margin, padding)
;;

(def -space-mapping
  {:m  [:margin]
   :mt [:margin-top]
   :mr [:margin-right]
   :mb [:margin-bottom]
   :ml [:margin-left]
   :mx [:margin-right :margin-left]
   :my [:margin-top :margin-bottom]

   :p  [:padding]
   :pt [:padding-top]
   :pr [:padding-right]
   :pb [:padding-bottom]
   :pl [:padding-left]
   :px [:padding-right :padding-left]
   :py [:padding-top :padding-bottom]})

(def -space-keys
  (keys -space-mapping))

(defn -transform-space-attr [space [attr idx]]
  (let [val (->> idx (nth space) -px)
        attrs (->> (-space-mapping attr)
                   (map #(vector % val)))]
    attrs))

(defn -transform-space [attrs]
  (let [space (:space @theme*)]
    (->> (select-keys attrs -space-keys)
         (mapcat #(-transform-space-attr space %)))))

(comment
  (-transform-space {:mx 2 :mt 1}))

;;
;; Font Size
;;

(defn -transform-font-size [attrs]
  (let [fs (:font-size @theme*)
        idx (:font-size attrs)
        font-size (->> idx (nth fs) -px)]
    [[:font-size font-size]]))

(comment
  (-transform-font-size {:font-size 3}))

;;
;; Width
;;

(defn -transform-width [attrs]
  (let [width (:width attrs)
        width
        (if (>= 1 width)
          (->> width (* 100) int -%)
          (-px width))]
    [[:width width]]))

(comment
  (-transform-width {:width 0.4}))

;;
;; Generic transformer
;;

(defn transform-attrs [attrs & fns]
  (->> fns
       (mapcat #(% attrs))
       (into {})))

(comment
  (transform-attrs
    {:width     0.1
     :font-size 2
     :mx        4}
    space
    font-size
    width))

;;
;; Public Mixins
;;

(def space -transform-space)

(def font-size -transform-font-size)

(def width -transform-width)
