name := "isthere"

version := "1.4"

autoScalaLibrary := false

libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.2.0",
    "javax.mail" % "mail" % "1.4.1"
)

packageArchetype.java_application
