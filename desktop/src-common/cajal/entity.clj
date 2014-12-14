(ns cajal.entity
  (:require [cajal.util :as u]
            [play-clj.ui :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.core :refer :all]))

(defn random-position [] {:x (rand-int 300) :y (rand-int 9)})

(defn make-player [image x y]
  (assoc image
    :player? true
    :x x
    :y y
    :x-velocity 4
    :y-velocity 0
    :x-change 0
    :y-change 0
    :width 1
    :height 1
    :can-jump? false
    :jump-sound (sound "jump.wav")))

(defn make-obstacle [image x y]
  (assoc image
         :x x
         :y y
         :width 1
         :height 1
         :x-velocity 0
         :y-velocity 0
         :obstacle? true
         :collision-sound nil))

(defn spawn-obstacle [kind]
  (case kind
    :rock (make-obstacle (image "rock.png")
                         (:x (random-position))
                         1)))

(defn get-touching-tile
  "Compares entity position against tile walls to determine if entity collides"
  [screen {:keys [x y width height]} & layer-names]
  (let [layers (map #(tiled-map-layer screen %) layer-names)]
    (->> (for [tile-x (range (int x) (+ x height))
               tile-y (range (int y) (+ y height))]
           (some #(when (tiled-map-cell % tile-x tile-y)
                    [tile-x tile-y])
                 layers))
         (drop-while nil?)
         first)))

(defn get-x-velocity
  "Adds the acceleration value to the player's x velocity every render call"
  [{:keys [player? x-velocity]}]
  (if player?
    (+ x-velocity u/acceleration)
    x-velocity))

;; FIXME Jump velocity == duration of input
(defn get-y-velocity
  [{:keys [player? y-velocity can-jump?]}]
  (if player?
    (if (and can-jump? (u/jump-input?))
      u/jump-acceleration
      y-velocity)
    y-velocity))

(defn enable-jump? [y-velocity can-jump?]
  (if (pos? y-velocity)
    false
    can-jump?))

(defn decelerate [velocity] (* velocity u/deceleration))

(defn move
  [{:keys [delta-time]} {:keys [x y can-jump?] :as entity}]
  (let [x-velocity (get-x-velocity entity)
        y-velocity (+ (get-y-velocity entity) u/gravity)
        x-change (* x-velocity delta-time)
        y-change (* y-velocity delta-time)]
    (if (or (not= 0 x-change)
            (not= 0 y-change))
      (assoc entity
             :x-velocity x-velocity
             :y-velocity (decelerate y-velocity)
             :x-change x-change
             :y-change y-change
             :x (+ x x-change)
             :y (+ y y-change)
             :can-jump? (enable-jump? y-velocity can-jump?))
      entity)))

(defn prevent-move
  [screen {:keys [x y x-change y-change] :as entity}]
  (let [old-x (- x x-change)
        old-y (- y y-change)
        entity-x (assoc entity :y old-y)
        entity-y (assoc entity :x old-x)
        up? (> y-change 0)]
    (merge entity
           (when (get-touching-tile screen entity-x "walls")
             {:x-velocity 0 :x-change 0 :x old-x})
           (when-let [tile (get-touching-tile screen entity-y "walls")]
             {:y-velocity 0 :y-change 0 :y old-y :can-jump? (not up?)}))))

;; FIXME Actually play sounds
(defn play-sounds! [entities]
  (doseq [{:keys [play-sound]} entities]
    (when play-sound
      (sound! play-sound :play)))
  (map #(dissoc % :play-sound) entities))
