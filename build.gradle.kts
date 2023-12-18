plugins {
    id("java")
}

group = "gg.generations"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://maven.generations.gg/snapshots")
    maven("https://maven.generations.gg/releases")
}

dependencies {
    implementation("gg.generations:RareCandy:2.4.25-SNAPSHOT"){isTransitive = false}

    implementation("org.tukaani", "xz", "1.9")
    implementation("org.joml", "joml", "1.10.5")

    implementation(platform("org.lwjgl:lwjgl-bom:3.3.2"))!!
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")
    implementation("com.github.thecodewarrior", "BinarySMD", "-SNAPSHOT")

    runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows")
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-windows")
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = "natives-windows")
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-windows")

    implementation("org.slf4j:slf4j-jdk14:2.0.7")

    // PokeUtils Libs
    implementation("com.github.weisj:darklaf-core:3.0.2")
    implementation("com.intellij:forms_rt:7.0.3")
    implementation("org.lwjgl", "lwjgl-nfd")
    runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = "natives-windows")
    implementation("org.lwjglx", "lwjgl3-awt", "0.1.8")

    implementation("com.thebombzen:jxlatte:1.1.2")

    implementation("org.apache.commons", "commons-compress", "1.24.0")

    implementation("de.javagl:jgltf-model:2.0.4-SNAPSHOT")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    implementation(fileTree("libs") {
        include("*.jar")
    })

    implementation("com.google.flatbuffers", "flatbuffers-java", "23.3.3")
    implementation ("com.google.code.gson:gson:2.10.1")
}
