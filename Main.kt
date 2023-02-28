package svcs

import java.io.File

fun main(args: Array<String>) {
    File("vcs/commits").mkdirs()
    File("vcs/log.txt").createNewFile()
    when (if (args.isEmpty()) "--help" else args[0]) {
        "--help" -> printHelpPage()
        "config" -> config(args)
        "add" -> addFile(args)
        "log" -> log()
        "commit" -> commit(args)
        "checkout" -> checkout(args)
        else -> println("'${args[0]}' is not a SVCS command.")
    }
}

fun printHelpPage() {
    println("These are SVCS commands:")
    println("config     Get and set a username.")
    println("add        Add a file to the index.")
    println("log        Show commit logs.")
    println("commit     Save changes.")
    println("checkout   Restore a file.")
}

fun config(args: Array<String>) {
    val configFile = File("vcs/config.txt")
    if (args.size > 1) {
        val name = args[1]
        configFile.writeText(name)
        println("The username is $name.")
    } else {
        if (configFile.exists()) {
            val name = configFile.readText()
            println("The username is $name.")
        } else {
            println("Please, tell me who you are.")
        }
    }
}

fun addFile(args: Array<String>) {
    val indexFile = File("vcs/index.txt")
    if (args.size > 1) {
        val newFile = File(args[1])
        if (newFile.exists()) {
            if (indexFile.exists()) {
                indexFile.appendText("\n${args[1]}")
            } else {
                indexFile.writeText(args[1])
            }
            println("The file '$newFile' is tracked.")
        } else {
            println("Can't find '$newFile'.")
        }
    } else {
        if (indexFile.exists()) {
            println("Tracked files:")
            for (fileName in indexFile.readText().split(" ")) {
                println(fileName)
            }
        } else {
            println("Add a file to the index.")
        }
    }
}

fun log() {
    val logFile = File("vcs/log.txt")
    val commits = File("vcs/commits")
    if (commits.listFiles()!!.isEmpty()) {
        println("No commits yet.")
    } else {
        println(logFile.readText())
    }
}

fun commit(args: Array<String>) {
    val commits = File("vcs/commits")
    val commitsList = commits.listFiles()!!
    if (args.size > 1) {
        val indexFile = File("vcs/index.txt")
        val trackedFiles = indexFile.readText().split("\n")
        val hashValue = getHash(trackedFiles)
        val newCommit = File("$commits/$hashValue")
        if (hashValue.isEmpty() || commitsList.contains(newCommit)) {
            println("Nothing to commit.")
        } else {
            val logFile = File("vcs/log.txt")
            val configFile = File("vcs/config.txt")
            val logs = logFile.readText()
            val newLog = """
                commit $hashValue
                Author: ${configFile.readText()}
                ${args[1]}
                
            """.trimIndent()
            logFile.writeText(newLog + logs)
            println("Changes are committed.")
            newCommit.mkdir()
            for (fileName in trackedFiles) {
                val file = File("$newCommit/$fileName")
                file.createNewFile()
                File(fileName).copyTo(file, true)
            }
        }
    } else {
        println("Message was not passed.")
    }
}

fun getHash(files: List<String>): String {
    var hashValue = ""
    for (fileName in files) {
        val file = File(fileName)
        if (file.exists()) {
            hashValue += kotlin.math.abs(file.readText().hashCode()).toString(36)
        }
    }
    return hashValue
}

fun checkout(args: Array<String>) {
    if (args.size > 1) {
        val commit = File("vcs/commits/${args[1]}")
        if (commit.exists()) {
            commit.copyRecursively(File("."), true)
            println("Switched to commit ${args[1]}.")
        } else {
            println("Commit does not exist.")
        }
    } else {
        println("Commit id was not passed.")
    }
}