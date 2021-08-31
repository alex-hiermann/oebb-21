package main.groovy.commands

import groovy.io.FileType
import groovy.util.logging.Slf4j
import picocli.CommandLine
import picocli.groovy.PicocliBaseScript2

import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.text.SimpleDateFormat

@CommandLine.Command(
        description = "Folder Monitoring"
)

@Slf4j
class FolderMonitoring extends PicocliBaseScript2 {

    @CommandLine.Parameters(paramLabel = "DIRECTORY", description = "Collects monitoring-info from the given directory")
    File directory

    @CommandLine.Option(names = ["-f", "--file"], description = "Saves monitoring-info into the given log file expecting absolute path")
    File logFile

    @CommandLine.Option(names = ["-r", "--recursive"], description = "Search for files recursively")
    Boolean recursive

    /**
     * Executes a script and saves the output in the log
     */
    @Override
    Object call() {
        def logFilePath = "C:/monitoring/logs/"
        if (!recursive) recursive = false
        File log = validateLogFile(logFilePath)
        while (true) {
            appendNewLinesToLog(findAllFiles(directory, recursive), log)
            Thread.sleep(59 * 1000)
        }
    }

    File validateLogFile(String path) {
        if (!directory || !directory.exists()) {
            println("ERROR: Directory $directory was not found!")
            System.exit(1)
        }

        File log
        if (logFile) {
            log = logFile
        } else {
            def logName = path + "/log_" + directory.name + ".log"
            log = new File(logName)
        }

        def savingDirectory = new File(path)
        if (!savingDirectory.exists()) savingDirectory.mkdirs()
        if (!log.exists()) log.createNewFile()
        log
    }

    /**
     * Find all files in a directory recursively
     *
     * @param folder folder
     * @param searchRecursively true if you want to search recursively with subfolders
     * @return all important files in a list
     */
    static ArrayList<String> findAllFiles(File folder, boolean searchRecursively) {
        List<String> files = new ArrayList<>()
        if (searchRecursively) {
            folder.eachFileRecurse(FileType.FILES) {
                files.add(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").format(new Date((Files.getAttribute(it.toPath(), "creationTime") as FileTime).toMillis())) + "; " + (it as String))
            }
        } else {
            folder.eachFile(FileType.FILES) {
                files.add(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").format(new Date((Files.getAttribute(it.toPath(), "creationTime") as FileTime).toMillis())) + "; " + (it as String))
            }
        }
        return files
    }

    /**
     * Append new lines to the log
     *
     * @param lines lines to compare with the log
     * @param log the log
     * @return all new lines
     */
    static void appendNewLinesToLog(ArrayList<String> lines, File log) {
        def logLines = log.getText().split("\r\n").toList()
        def commons = lines.intersect(logLines)

        lines.removeAll(commons)
        for (String line : lines) {
            line.replaceAll("(\\r\\n+)|(\\n+)|(\\r+)", "")
            if (line.isEmpty() || line.isBlank()) continue
            log.append(line + "\r\n")
        }
    }
}
