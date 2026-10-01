package mima

final case class Library(groupId: String, artifactId: String, version: String) {
  lazy val toCoursier: coursier.core.Dependency = coursier.core.Dependency(
    coursier.core.Module(
      coursier.core.Organization(groupId),
      coursier.core.ModuleName(artifactId),
      Map.empty
    ),
    coursier.VersionConstraint(version)
  )

  val name = s"$artifactId-$version.jar"
  val mavenCentralURL: String = {
    val g = groupId.replace('.', '/')
    s"${Library.MavenCentral}$g/$artifactId/$version/$name"
  }
}
object Library {
  final val MavenCentral = "https://repo1.maven.org/maven2/"
}
