plugins {
    id("java")
}

group = "org.deez.nuts"
version = "1.21.6"

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.16")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.14")
    compileOnly("net.luckperms:api:5.5")
}
java{
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
tasks.test {
}