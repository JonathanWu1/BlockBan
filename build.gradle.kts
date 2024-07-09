plugins {
    id("java")
}

group = "org.deez.nuts"
version = "1.21-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.10")
    compileOnly("net.luckperms:api:5.4")
}
java{
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
tasks.test {
}