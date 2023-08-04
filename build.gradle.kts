plugins {
    id("java")
}

group = "gg.generations"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:3.3.2"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.joml", "joml", "1.10.5")

    implementation("com.google.flatbuffers", "flatbuffers-java", "23.3.3")
}
