name := """dapeng-tools-web"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.apache.zookeeper" % "zookeeper" % "3.4.7",
  "com.isuwang" % "dapeng-tools" % "1.1-SNAPSHOT" from "http://nexus.oa.isuwang.com/content/groups/public/com/isuwang/dapeng-tools/1.1-SNAPSHOT/dapeng-tools-1.1-20160713.025130-4.jar",
  "com.isuwang" % "dapeng-core" % "1.1-SNAPSHOT" from "http://nexus.oa.isuwang.com/content/groups/public/com/isuwang/dapeng-tools/1.1-SNAPSHOT/dapeng-tools-1.1-20160713.025130-4.jar"

)
resolvers ++= Seq(
  "scalaz-bintray" at "httpp://dl.bintray.com/scalaz/releases"
)



