package test.groovy.commands

import main.groovy.commands.FolderMonitoring
import org.junit.jupiter.api.Test

import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertLinesMatch
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.fail

class FolderMonitoringTest {

    FolderMonitoring folderMonitoring = new FolderMonitoring()

    @Test
    void testFindFiles() {
        List<String> output = FolderMonitoring.findAllFiles(new File("src/test/resources/searchTests"), false)

        List<Boolean> dates = new ArrayList<>()
        List<String> files = new ArrayList<>()
        output.forEach(entry -> {
            dates.add(Pattern.matches("\\d+([-:.]\\d+)+ \\d+([-:.]\\d+)+", entry.split("; ").first()))
            files.add(entry.split("; ").last())
        })

        if (dates.contains(false)) fail("ERROR: Wrong date output")

        assertLinesMatch(new String[]{"src\\test\\resources\\searchTests\\file1.txt",
                "src\\test\\resources\\searchTests\\file2.txt",
                "src\\test\\resources\\searchTests\\file3.txt"
        }.toList(), files)
    }

    @Test
    void testFindFilesRecursively() {
        List<String> output = FolderMonitoring.findAllFiles(new File("src/test/resources/searchTests"), true)

        List<Boolean> dates = new ArrayList<>()
        List<String> files = new ArrayList<>()
        output.forEach(entry -> {
            dates.add(Pattern.matches("\\d+([-:.]\\d+)+ \\d+([-:.]\\d+)+", entry.split("; ").first()))
            files.add(entry.split("; ").last())
        })

        if (dates.contains(false)) fail("ERROR: Wrong date output")

        assertLinesMatch(new String[]{"src\\test\\resources\\searchTests\\file1.txt",
                "src\\test\\resources\\searchTests\\file2.txt",
                "src\\test\\resources\\searchTests\\file3.txt",
                "src\\test\\resources\\searchTests\\recursiveSearchTest\\recursiveTest.txt"
        }.toList(), files)
    }

    @Test
    void testFindAllFiles() {
        assertThrows(FileNotFoundException.class, () -> {
            FolderMonitoring.findAllFiles(new File("C:/test"), false)
        })
    }

    @Test
    void testFindAllFiles2() {
        List<String> output = FolderMonitoring.findAllFiles(new File("src/test/resources/searchTests"), false)
        assertEquals(3, output.size())
    }

    @Test
    void testFindAllFiles3() {
        List<String> output = FolderMonitoring.findAllFiles(new File("src/test/resources/searchTests"), true)
        assertEquals(4, output.size())
    }

    @Test
    void testRecursive() {
        Object result = folderMonitoring.getRecursive()
        assertEquals(null, result)

        folderMonitoring.setRecursive(false)
        assertEquals(false, folderMonitoring.getRecursive())
    }

    @Test
    void testDirectory() {
        Object result = folderMonitoring.getDirectory()
        assertEquals(null, result)

        folderMonitoring.setDirectory(new File("C:/test"))
        assertEquals(new File("C:/test"), folderMonitoring.getDirectory())
    }

    @Test
    void testCall() {
        List<Object> args = Arrays.asList(new File("src/test/resources/searchTests"), true)
        folderMonitoring.invokeMethod("findAllFiles", args)
    }
}