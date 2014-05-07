name := "Kafka Common"

version := "0.0.1-SNAPSHOT"

organization := "com.github.warmerq"  

scalaVersion := "2.9.2"

libraryDependencies ++= {
  val finagleVersion = "6.5.2"
  Seq(
    "com.typesafe" % "config" % "1.0.0",
    "com.twitter" %% "finagle-core" % finagleVersion,
    "com.twitter" %% "finagle-http" % finagleVersion,
    "com.twitter" %% "twitter-server" % "1.0.3",
    "com.twitter" %% "util-core" % "6.3.8",
    "com.github.sgroschupf" % "zkclient" % "0.1",
    "org.specs2" %% "specs2" % "1.11" % "test",
    "org.specs2" %% "specs2-scalaz-core" % "6.0.1" % "test"
  )
}

// publishTo := Some(Resolver.file("file", new File("/Users/mengwu/.m2/repository")))

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishTo <<= (version) { version: String =>
  val repo = "http://nexus.yidian-inc.com/content/repositories/" // some repository
  if (version.trim.endsWith("SNAPSHOT"))
    Some("Snapshots" at repo + "snapshots/")
  else
    Some("releases" at repo + "releases/")
}
