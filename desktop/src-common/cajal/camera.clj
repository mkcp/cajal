(ns cajal.camera
  (:require [play-clj.core :refer :all]))

(def camera-height 6)
(def camera-offset 10)

(defn move-camera! [screen x y]
  (let [camera-x (+ camera-offset x)]
    (if (< y camera-height)
      (position! screen camera-x camera-height)
      (position! screen camera-x y))))
