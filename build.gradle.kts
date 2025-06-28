plugins {
    java
    application
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("io.javalin:javalin:6.1.3")
    implementation("org.eclipse.jetty:jetty-server:11.0.20")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("org.slf4j:slf4j-simple:2.0.10") 

    testImplementation("io.javalin:javalin-testtools:6.1.3")
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0") 
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("app.App") // Classe principal para rodar a aplicação Javalin
}

// NOVO: Tarefa personalizada para rodar o ApiClient
// Isso permite que você execute o cliente com ".\gradlew runClient"
val runClient by tasks.creating(JavaExec::class) {
    classpath = sourceSets.main.get().runtimeClasspath // Usa o classpath da aplicação principal
    mainClass.set("app.client.ApiClient") // Define a classe principal para esta tarefa como ApiClient
    standardInput = System.`in` // Permite entrada do usuário se necessário (não usado no cliente atual)
    group = "application" // Agrupa a tarefa na categoria "application"
    description = "Runs the Java API Client." // Descrição da tarefa
}