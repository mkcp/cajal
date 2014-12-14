(ns cajal.util
  (:require [play-clj.core :refer :all]))

;; World
(def vertical-tiles 20)
(def pixels-per-tile 32)

;; Physics
(def gravity -1)
(def acceleration 0.005)
(def deceleration 0.9)
(def jump-acceleration 24)

;; Input
(defn jump-input? [] (or (key-pressed? :dpad-up) (game :touched?)))
