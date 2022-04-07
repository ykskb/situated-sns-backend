(ns situated-sns.main
  (:gen-class)
  (:require [situated-sns.system :as sns-sys]))

(defn -main [& _args]
  (sns-sys/start))
