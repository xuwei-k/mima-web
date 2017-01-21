package mima

final case class Library(groupId: String, artifactId: String, version: String) {
  val name = s"$artifactId-$version.jar"
  val mavenCentralURL: String = {
    val g = groupId.replace('.', '/')
    s"https://repo1.maven.org/maven2/$g/$artifactId/$version/$name"
  }
}
