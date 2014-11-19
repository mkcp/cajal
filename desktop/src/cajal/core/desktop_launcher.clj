(ns cajal.core.desktop-launcher
  (:require [cajal.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. cajal "cajal" 800 600)
  (Keyboard/enableRepeatEvents true))
