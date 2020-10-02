// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.util.Properties
import java.io.FileInputStream
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.Service

buildscript {

    val composeVersion by extra { "1.0.0-alpha03" }

    val kotlinVersion by extra { "1.4.0" }
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-alpha12")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.gms:google-services:4.3.3")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven ( url= "https://jitpack.io" )
    }
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}


val getProps by extra {
    fun(propName: String): String {
        return getProps(propName)
    }
}
plugins {
    id("org.hidetake.ssh") version "2.10.1"
}

val raspberry = remotes.create("raspberry") {
    host = "192.168.1.63"
    user = "pi"
    password = getProps("password", "gradle.local.properties")
}

val deployServer = tasks.create("deployServer") {
    doLast {
        ssh.runSessions {
            session(raspberry) {
                put(File("media-server"), File("/home/pi/")){it.name != "node_modules"}
                logger.warn(execute("pm2 restart media-server"))
            }
        }
    }
}

fun getProps(propName: String, path: String = "local.properties"): String {
    val propsFile = rootProject.file(path)
    return if (propsFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(propsFile))
        props[propName] as String
    } else {
        ""
    }
}

val generateBuildConfig by extra {
    fun(propFile: File?, buildType: com.android.build.gradle.internal.dsl.BuildType) {
        val file = propFile ?: rootProject.file("local.properties")
        if (file.exists()) {
            val props = Properties()
            props.load(FileInputStream(file))

            props.forEach { key, value ->

                if(key != "sdk.dir") {
                    val name = (key as String).replace(".", "_").toUpperCase()
                    val v = value as String
                    when  {
                        v.toIntOrNull() != null -> buildType.buildConfigField(
                            type = "int",
                            name = name,
                            value = value
                        )
                        v.toLongOrNull() != null -> buildType.buildConfigField(
                            type = "long",
                            name = name,
                            value = value.toString()
                        )
                        v.toFloatOrNull() != null -> buildType.buildConfigField(
                            type = "float",
                            name = name,
                            value = value.toString()
                        )
                        v.toDoubleOrNull() != null -> buildType.buildConfigField(
                            type = "double",
                            name = name,
                            value = value.toString()
                        )
                        else -> buildType.buildConfigField(
                            type = "String",
                            name = name,
                            value = value.toString()
                        )
                    }
                }
            }
        }
    }
}

fun Service.runSessions(action: RunHandler.() -> Unit) =
    run(delegateClosureOf(action))

fun RunHandler.session(vararg remotes: Remote, action: SessionHandler.() -> Unit) =
    session(*remotes, delegateClosureOf(action))

fun SessionHandler.put(from: Any, into: Any, filter: (f: File) -> Boolean = {true}) =
    put(hashMapOf("from" to from, "into" to into, "filter" to filter))