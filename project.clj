(defproject situated-sns "0.1.0"
  :description ""
  :url "https://github.com/ykskb/situated-sns-backend"
  :min-lein-version "2.0.0"
  :aot :all
  :main situated-sns.main
  :dependencies [[alekcz/charmander "1.0.3"]
                 [bidi "2.1.6"]
                 [cheshire "5.10.1"]
                 [eftest "0.5.9"]
                 [hawk "0.2.11"]
                 [kerodon "0.9.1"]
                 [integrant/repl "0.3.2"]
                 [metosin/reitit "0.5.15"]
                 [ch.qos.logback/logback-classic "1.1.1"]
                 [org.postgresql/postgresql "42.3.0"]
                 [org.xerial/sqlite-jdbc "3.34.0"]
                 [ring-cors "0.1.13"]
                 [ring/ring-json "0.5.1"]
                 [ring/ring-jetty-adapter "1.9.3"]
                 [threatgrid/ring-graphql-ui "0.1.3"]
                 [com.github.ykskb/phrag "0.4.4"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [hikari-cp "2.14.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.github.seancorfield/honeysql "2.0.0-rc3"]
                 [ring/ring-core "1.9.5"]
                 [environ "1.2.0"]
                 [metosin/jsonista "0.3.6"]]
  :resource-paths ["resources" "target/resources"]
  :plugins [[lein-eftest "0.5.9"]
            [lein-cloverage "1.2.2"]
            [lein-environ "1.2.0"] ]
  :eftest {:report eftest.report.pretty/report
           :report-to-file "target/junit.xml"}
  :profiles
  {:dev  [:project/dev :profiles/dev]
   :repl {:repl-options {:init-ns user}}
   :profiles/dev {:env {:service-host ""
                        :service-port "3000"
                        :resource-dir ""
                        :firebase-project-id ""
                        :db-type ""
                        :db-host ""
                        :db-port "5432"
                        :db-user ""
                        :db-password ""
                        :db-current-schema ""
                        :db-name ""}}
   :project/dev  {:source-paths   ["dev/src"]
                  :resource-paths ["dev/resources"]
                  :dependencies   []}})

