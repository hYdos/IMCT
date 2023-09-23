plugins {
    id("java")
}

group = "gg.generations"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:3.3.2"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.joml", "joml", "1.10.5")
    implementation("org.tukaani", "xz", "1.9")
    implementation("org.apache.commons", "commons-compress", "1.24.0")

    implementation("de.javagl:jgltf-model:2.0.4-SNAPSHOT")
    implementation("de.javagl:jgltf-model-builder:2.0.4-SNAPSHOT")

    implementation("net.imagej:ij:1.54f")

    implementation("com.google.flatbuffers", "flatbuffers-java", "23.3.3")
    implementation("org.openpnp:opencv:4.7.0-0");
    implementation ("com.google.code.gson:gson:2.10.1")
}
