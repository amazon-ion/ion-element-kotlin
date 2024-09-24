
plugins {
    id("java-library")

    kotlin("jvm") version "1.6.20"
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.6.20"
    id("jacoco")
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.9.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"

    // TODO: Configure and use this
    id("com.diffplug.spotless") version "6.11.0"

    // There are newer versions available, but they are not guaranteed to support Java 8.
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

group = "com.amazon.ion"
version = "1.4.0-SNAPSHOT"
description = "An immutable in-memory representation of Amazon Ion for Kotlin"

val isCI: Boolean = System.getenv("CI") == "true"
val githubRepositoryUrl = "https://github.com/amazon-ion/ion-element-kotlin/"
val isReleaseVersion: Boolean = !version.toString().endsWith("SNAPSHOT")

dependencies {
    api("com.amazon.ion:ion-java:[1.4.0,)")
    compileOnly("com.amazon.ion:ion-java:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")

    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    explicitApi()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

lateinit var sourcesJar: AbstractArchiveTask
lateinit var javadocJar: AbstractArchiveTask

tasks {

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            apiVersion = "1.3"
            languageVersion = "1.4" // Can be read by 1.3 compiler, so consumers on kotlin 1.3 are still supported.
            freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn" // Using RequiresOptIn requires opt-in itself, at least in kotlin-1.4
            )
        }
    }

    ktlint {
        version.set("0.45.2")
        outputToConsole.set(true)
    }

    sourcesJar = create<Jar>("sourcesJar") sourcesJar@{
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    javadocJar = create<Jar>("javadocJar") javadocJar@{
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    withType<Sign> {
        setOnlyIf { isReleaseVersion && gradle.taskGraph.hasTask(":publish") }
    }

    test {
        useJUnitPlatform()
        // report is always generated after tests run
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        doLast {
            logger.quiet("Coverage report written to file://${reports.html.outputLocation.get()}/index.html")
        }
    }
}

publishing {
    publications.create<MavenPublication>("IonElement") {
        artifactId = "ion-element"
        artifact(tasks.jar)
        artifact(sourcesJar)
        artifact(javadocJar)

        pom {
            name.set("Ion Element Kotlin")
            description.set(project.description)
            url.set(githubRepositoryUrl)
            scm {
                connection.set("scm:git:git@github.com:amazon-ion/ion-element-kotlin.git")
                developerConnection.set("scm:git:git@github.com:amazon-ion/ion-element-kotlin.git")
                url.set("git@github.com:amazon-ion/ion-element-kotlin.git")
            }
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    name.set("Amazon Ion Team")
                    email.set("ion-team@amazon.com")
                    organization.set("Amazon Ion")
                    organizationUrl.set("https://github.com/amazon-ion")
                }
            }
        }
    }
}

signing {
    // Allow publishing to maven local even if we don't have the signing keys
    // This works because when not required, the signing task will be skipped
    // if signing.keyId, signing.password, signing.secretKeyRingFile, etc are
    // not present in gradle.properties.
    isRequired = isReleaseVersion

    if (isCI) {
        val signingKeyId: String? by project
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    }

    sign(publishing.publications["IonElement"])
}

nexusPublishing {
    // Documentation for this plugin, see https://github.com/gradle-nexus/publish-plugin/blob/v1.3.0/README.md
    this.repositories {
        sonatype {
            nexusUrl.set(uri("https://aws.oss.sonatype.org/service/local/"))
            // For CI environments, the username and password should be stored in
            // ORG_GRADLE_PROJECT_sonatypeUsername and ORG_GRADLE_PROJECT_sonatypePassword respectively.
            if (!isCI) {
                username.set(properties["ossrhUsername"].toString())
                password.set(properties["ossrhPassword"].toString())
            }
        }
    }
}
