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

    implementation("de.javagl:jgltf-model:2.0.3")
    implementation("de.javagl:jgltf-obj:2.0.3")
    implementation("de.javagl:jgltf-model-builder:2.0.3")

    implementation("net.imagej:ij:1.54f")

    implementation("com.google.flatbuffers", "flatbuffers-java", "23.3.3")
}
