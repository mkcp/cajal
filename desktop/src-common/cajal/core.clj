(ns cajal.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]))

;; Physics constants
(def gravity 800)
(def acceleration 10)
(def max-jump-velocity 24)
(def gravity -1.5)
(def deceleration 0.9)

;; This should probably be stored on the screen-map
(def conditions (atom {:min-speed 100 :max-speed 200}))

(defn create-player [image x y]
  (assoc image
    :player? true
    :x x
    :y y
    :x-velocity 0
    :y-velocity 0
    :x-change 0
    :y-change 0
    :can-jump? false
    :jump-sound (sound "jump.wav")))

(defn touched? [key]
  (and (game :touched?)
       (case key
         :down (> (game :y) (* (game :height) (/ 2 3)))
         :up (< (game :y) (/ (game :height) 3))
         :left (< (game :x) (/ (game :width) 3))
         :right (> (game :x) (* (game :width) (/ 2 3)))
         false)))

;; FIXME
(defn get-touching-tile [])

;; FIXME Accelerate constantly
#_(defn get-x-velocity [{:keys [player? x-velocity]}]
    (if player?
      (cond
       (or (key-pressed? :dpad-left (touched? :left))))
      x-velocity))

(defn get-y-velocity [{:keys [player? y-velocity can-jump?]}]
  (if player?
    (cond (and can-jump? (or (key-pressed? :dpad-up) (touched? :up)))
          max-jump-velocity
          :else
          y-velocity)
    y-velocity))

(defn move
  "The player should constantly accelerate to the right"
  [{:keys [delta-time]} {:keys [x y can-jump?] :as entity}]
  (let [x-velocity #_(get-x-velocity entity)
        x-velocity (get-x-velocity entity)
        x-change (* x-velocity delta-time)
        y-change (* y-velocity delta-time)]))

(defn prevent-move
  "Stops the player when she collides with wall tiles"
  [screen {:keys [x y x-change y-change] :as entity}]
  (let [old-x (- x x-change)
        old-y (- y y-change)
        entity-x (assoc entity :y old-y) ;; These are flipped, why?
        entity-y (assoc entity :x old-x) ;; These are flipped?
        up? (> y-change 0)]
    (merge entity
           (when (get-touching-tile screen entity-x "walls")
             {:x-velocity 0 :x-change 0 :x old-x})
           (when-let [tile (get-touching-tile screen entity-y "walls")]
             {:y-velocity 0 :y-change 0 :y old-y :can-jump? (not up?)}))))

;; FIXME
(defn accelerate
  "Increases the player's x-velocity by the level's default acceleration"
  [screen entity]
  (if (:player? entity)
    (let [x-velocity (:x-velocity entity)]
      (assoc entity
        :x-velocity (+ acceleration x-velocity)))
    entity))

;; FIXME
(defn slow [x-velocity deceleration] (- x-velocity deceleration))

;; TODO Check that the bounds of the X of primary contacts the bounds of the other. I really gotta look this shit up
(defn touching?
  "If the first entity is touching hte second entity, return the second entity. Otherwise return nil."
  [primary secondary]
  (if primary
    secondary
    nil))

;; FIXME
(defn player-collide
  "The player should slow down on collision with an obstacle"
  [screen entity entities]
  (if (and (touching? entity entities)
           (:player? entity))
     entity))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage))
    [(create-player (texture "runner.png")
                    10
                    10)])

  ;; FIXME
  :on-render
  (fn [screen entities]
    (clear! (/ 135 255) (/ 206 255) (/ 235 255) 100)

    (->> entities
         (map (fn [entity]
                (->> entity
                     ;; FIXME
                     #_(accelerate screen)
                     #_(player-collide screen entities)
                     (move screen)
                     (prevent-move screen)
                     )))
         (render! screen))))

(defscreen blank-screen
  :on-render
  (fn [screen entities]
    (clear!)))

(defgame cajal
  :on-create
  (fn [this]
    (set-screen! this main-screen)))


(set-screen-wrapper! (fn [screen screen-fn]
                       (try (screen-fn) (catch Exception e
                                          (.printStackTrace e)
                                          (set-screen! cajal blank-screen)))))
