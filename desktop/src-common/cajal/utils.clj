(ns cajal.util)

(def vertical-tiles 20)
(def pixels-per-tile 32)
(def gravity -1)
(def acceleration 0.005)
(def deceleration 0.9)
(def jump-acceleration 24)

(defn jump-input? [] (or (key-pressed? :dpad-up) (game :touched?)))
