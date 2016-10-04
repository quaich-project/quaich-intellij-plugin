onLoad in Global := ( (s: State) => {  "updateIdea" :: s}) compose (onLoad in Global).value

val projectVersion        = "0.1-SNAPSHOT"
val projectOrg            = "codes.bytes"

lazy val commonSettings = Seq(
  organization := projectOrg,
  version := projectVersion,
  scalaVersion := "2.11.8",
  retrieveManaged := true,
  libraryDependencies ++= Seq(
  ),
  scalacOptions := Seq(
    "-encoding",
    "UTF-8",
    "-target:jvm-1.8",
    "-deprecation",
    "-language:_"
  ),
  fork in (Test, run) := true
)

lazy val intellijPlugin = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "quaich-intellij-plugin",
    libraryDependencies ++= Seq(
      projectOrg %% "quaich-http" % projectVersion
    ),
    ideaInternalPlugins := Seq(),
    ideaExternalPlugins := Seq(
      IdeaPlugin.Zip("scala-plugin", url("https://plugins.jetbrains.com/files/1347/28632/scala-intellij-bin-2016.3.2.zip"))
    ),
    assemblyExcludedJars in assembly <<= ideaFullJars,
    ideaBuild := "162.2032.8" // Intellij 2016.2.4
  ).
  enablePlugins(SbtIdeaPlugin)

lazy val packagePlugin = TaskKey[File]("package-plugin", "Create plugin's zip file ready to load into IDEA")

packagePlugin in intellijPlugin <<= (assembly in intellijPlugin,
  target in intellijPlugin,
  ivyPaths) map { (ideaJar, target, paths) =>
  val pluginName = "quaich-intellij-plugin"
  val ivyLocal = paths.ivyHome.getOrElse(file(System.getProperty("user.home")) / ".ivy2") / "local"
  val sources = Seq(
    ideaJar -> s"$pluginName/lib/${ideaJar.getName}"
  )
  val out = target / s"$pluginName-plugin.zip"
  IO.zip(sources, out)
  out
}
