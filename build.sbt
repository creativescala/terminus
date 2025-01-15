/*
 * Copyright 2015-2020 Creative Scala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import scala.sys.process.*
import laika.config.LinkConfig
import laika.config.ApiLinks
import laika.theme.Theme
import laika.helium.config.TextLink

ThisBuild / tlBaseVersion := "0.3" // your current series x.y

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / organization := "org.creativescala"
ThisBuild / organizationName := "Creative Scala"
ThisBuild / startYear := Some(2024)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("noelwelsh", "Noel Welsh")
)

ThisBuild / sonatypeCredentialHost := xerial.sbt.Sonatype.sonatypeLegacy

lazy val scala3 = "3.3.4"

ThisBuild / crossScalaVersions := List(scala3)
ThisBuild / scalaVersion := scala3
ThisBuild / useSuperShell := false
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / tlSitePublishBranch := Some("main")

// Run this (build) to do everything involved in building the project
commands += Command.command("build") { state =>
  "clean" ::
    "compile" ::
    "test" ::
    "scalafixAll" ::
    "scalafmtAll" ::
    "scalafmtSbt" ::
    "headerCreateAll" ::
    "githubWorkflowGenerate" ::
    "dependencyUpdates" ::
    "reload plugins; dependencyUpdates; reload return" ::
    "docs / tlSite" ::
    state
}

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    Dependencies.munit.value,
    Dependencies.munitScalaCheck.value
  ),
  startYear := Some(2024),
  licenses := List(
    "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
  )
)

lazy val root = tlCrossRootProject.aggregate(core, unidocs)

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("core"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.catsCore.value
    ),
    name := "terminus-core"
  )
  .jvmSettings(libraryDependencies += Dependencies.jline.value)
  .jsSettings(libraryDependencies += Dependencies.scalajsDom.value)

lazy val docs =
  project
    .in(file("docs"))
    .settings(
      tlSiteApiUrl := Some(
        sbt.url(
          "https://javadoc.io/doc/org.creativescala/terminus-docs_3/latest/"
        )
      ),
      laikaConfig := laikaConfig.value.withConfigValue(
        LinkConfig.empty
          .addApiLinks(
            ApiLinks(baseUri =
              "https://javadoc.io/doc/org.creativescala/terminus-docs_3/latest/"
            )
          )
      ),
      mdocIn := file("docs/src/pages"),
      Laika / sourceDirectories ++= Seq(
        file("docs/src/css"),
        file("docs/src/js"),
        (examples.js / Compile / fastOptJS / artifactPath).value
          .getParentFile() / s"${(examples.js / moduleName).value}-fastopt"
      ),
      laikaTheme := CreativeScalaTheme.empty
        .withHome(
          TextLink.internal(laika.ast.Path.Root / "README.md", "Terminus")
        )
        .withCommunity(
          TextLink.external("https://discord.gg/rRhcFbJxVG", "Community")
        )
        .withApi(
          TextLink.external(
            "https://javadoc.io/doc/org.creativescala/terminus-docs_3/latest",
            "API"
          )
        )
        .withSource(
          TextLink.external(
            "https://github.com/creativescala/terminus",
            "Source"
          )
        )
        .addJs(laika.ast.Path.Root / "xterm.js")
        .addJs(laika.ast.Path.Root / "main.js")
        .addCss(laika.ast.Path.Root / "xterm.css")
        .build,
      laikaExtensions ++= Seq(
        laika.format.Markdown.GitHubFlavor,
        laika.config.SyntaxHighlighting
      ),
      tlSite := Def
        .sequential(
          (examples.js / Compile / fastLinkJS),
          mdoc.toTask(""),
          laikaSite
        )
        .value
    )
    .enablePlugins(TypelevelSitePlugin)
    .dependsOn(core.jvm)

lazy val unidocs = project
  .in(file("unidocs"))
  .enablePlugins(TypelevelUnidocPlugin) // also enables the ScalaUnidocPlugin
  .settings(
    name := "terminus-docs",
    ScalaUnidoc / unidoc / unidocProjectFilter :=
      inAnyProject -- inProjects(
        docs,
        core.js,
        examples.js
      )
  )

lazy val examples = crossProject(JSPlatform, JVMPlatform)
  .in(file("examples"))
  .settings(
    commonSettings,
    moduleName := "terminus-examples"
  )
  .jvmConfigure(
    _.settings(mimaPreviousArtifacts := Set.empty)
      .dependsOn(core.jvm)
  )
  .jsConfigure(
    _.settings(mimaPreviousArtifacts := Set.empty)
      .dependsOn(core.js)
  )
  .dependsOn(core)
