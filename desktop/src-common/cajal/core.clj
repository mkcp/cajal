(ns cajal.core
  (:require [cajal.camera :as c]
            [cajal.util :as u]
            [cajal.entity :as e]
            [clojure.pprint :refer :all]
            [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]))

(declare cajal main-screen fps-screen)

(def loss-velocity (atom 3))
(def loss-acceleration 0.003)
(def win-velocity 200)

(defn increase-loss-velocity []
  (swap! loss-velocity (+ loss-velocity loss-acceleration)))

(defn win? [{:keys [x-velocity]}] (<= x-velocity win-velocity))
(defn lose? [{:keys [x-velocity]}] (<= x-velocity loss-velocity))

(defn reset-screen! [] (on-gl (set-screen! cajal main-screen fps-screen)))
(defn out-of-bounds? [y height] (< y (- height)))

(defn update-screen!
  "Used in the render function to focus the camera on the player and reset
  the screen if the player goes out of bounds."
  [screen entities]
  (doseq [{:keys [x y height player? x-velocity]} entities]
    (if player?
      (do (c/move-camera! screen x y)
          (when (or (out-of-bounds? y height)
                    (win? x-velocity)
                    (lose? x-velocity))
            (reset-screen!)))
      entities))
  entities)

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen
             :camera (orthographic)
             :renderer (orthogonal-tiled-map "cajal-tilemap.tmx" (/ 1 u/pixels-per-tile)))
    [(e/make-player (texture "runner.png") 0 2)
     (e/spawn-obstacle :rock)])

  :on-render
  (fn [screen entities]
    (clear! (/ 135 255) (/ 206 255) (/ 235 255) 100)
    (->> entities
         (map (fn [entity]
                (->> entity
                     (e/move screen)
                     (e/prevent-move screen))))
         e/play-sounds!
         (render! screen)
         (update-screen! screen)))

  :on-resize
  (fn [screen entities]
    (height! screen u/vertical-tiles)))

(defscreen fps-screen
  :on-show
  (fn [screen entities]
    (update! screen :camera (orthographic) :renderer (stage))
    [(assoc (label "0" (color :white))
            :id :fps
            :x 5)])

  :on-render
  (fn [screen entities]
    (render! screen
             (for [entity entities]
               (case (:id entity)
                 :fps (doto entity (label! :set-text (str (game :fps))))
                 entity))))

  :on-resize
  (fn [{:keys [width height] :as screen} entities]
    (height! screen (:height screen))
    nil))

(defscreen blank-screen
  :on-render
  (fn [screen entities]
    (clear!)))

(defgame cajal
  :on-create
  (fn [this]
    (set-screen! this main-screen fps-screen)))

;; Repl helpers
(defn reload!
  "Reimports namespaces and resets the game"
  []
  (do
    (require 'cajal.camera
             'cajal.util
             'cajal.entity
             'cajal.core
             :reload)
    (reset-screen!)))

;; Catches exceptions, clears screen, and prints the result
(set-screen-wrapper! (fn [screen screen-fn]
                       (try (screen-fn) (catch Exception e
                                          (.printStackTrace e)
                                          (set-screen! cajal blank-screen)))))
