import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.Service

plugins {
    id("org.hidetake.ssh") version "2.10.1"
}

val getPropsFromFile = rootProject.extra["getPropsFromFile"] as (propName: String, path: String) -> String

val raspberry = remotes.create("raspberry") {
    host = "192.168.1.63"
    user = "pi"
    password = getPropsFromFile("password", "gradle.local.properties")

}

tasks.register("deployServer") {
    doLast {// -> not executing on configure (syncing)
        ssh.runSessions {
            session(raspberry) {
                try {
                    execute("sudo rm -rf media-server")
                } catch (e: Exception) {
                    logger.error(e.message)
                }
                put(File(rootDir, "media-server"), File("/home/pi/")) {
                    println(it.name)
                    it.name != "node_module"
                }
                logger.warn(execute("pm2 restart media-server"))
            }
        }
    }
}



fun Service.runSessions(action: RunHandler.() -> Unit) =
    run(delegateClosureOf(action))

fun RunHandler.session(vararg remotes: Remote, action: SessionHandler.() -> Unit) =
    session(*remotes, delegateClosureOf(action))

fun SessionHandler.put(from: File, into: File, filter: (File) -> Boolean = { true }) {
    put(hashMapOf("from" to from, "into" to into))
}