package main.groovy.commands

import groovy.io.FileType
import org.xml.sax.SAXException

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator

class FileValidation {

    private static def xmlPath = "C:/validation/export/"

    private static def xsdPath = "C:/validation/xmlSchemas/"

    private static def log = new File("C:/validation/logs/log_folderValidation.log")

    static void main(String[] args) {
        doRun(xmlPath, xsdPath)
    }

    static def doRun(String xmlLocation, String xsdLocation) {
        List<File> files = getFilesFromPath(new File(xmlLocation))

        Map<File, Boolean> validationResults = new HashMap<>()
        validateFiles(files, new File(xsdLocation), validationResults)

        validationResults.each { key, value -> println("${key.name}: ${value}") }
    }

    static def validateFiles(List<File> files, File xsdFolder, Map<File, Boolean> validationMap) {
        files.forEach(file -> {
            URL schemaFile
            xsdFolder.eachFile { xsdFile ->
                {
                    if (xsdFile.name.take(xsdFile.name.lastIndexOf('.')) == file.name.take(file.name.lastIndexOf('.'))) {
                        schemaFile = xsdFile.toURI().toURL()
                    }
                }
            }
            if (schemaFile) {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                Schema schema = factory.newSchema(schemaFile)
                Validator validator = schema.newValidator()
                try {
                    validator.validate(new StreamSource(file))
                    validationMap.put(file, true)
                } catch (SAXException ignored) {
                    log.append("Invalid file found $file\n")
                    validationMap.put(file, false)
                }
            } else {
                log.append("XML schema missing for $file\n")
                validationMap.put(file, false)
            }
        })
    }

    /**
     * Gets all files out of a directory, edits them, and saves them into an other directory
     *
     * @param directory directory that will be searched
     * @param outputDir where the edited files will be copied to
     * @return list of all files from this directory
     */
    static ArrayList<File> copyEditedFilesFromPath(File directory, File outputDir) {
        List<File> files = new ArrayList<>()
        BufferedReader reader
        BufferedWriter writer

        directory.eachFile(FileType.FILES) { file ->
            {
                reader = new BufferedReader(new FileReader(file))
                writer = new BufferedWriter(new FileWriter(outputDir.toString().replaceAll("\\\\", "/") + "/" + file.name))
                String lineToReplace = "<?xml version=\"1.0\"?>"

                files.add(file)
                if (!outputDir.exists()) outputDir.mkdirs()

                String currentLine
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine == lineToReplace) currentLine += "\n<link xsi:schemaLocation=\"https://www.w3schools.com note.xsd\"/>"
                    writer.write(currentLine + System.getProperty("line.separator"))
                }
                writer.close()
                reader.close()
            }
        }
        return files
    }

    /**
     * Gets all files out of a directory
     *
     * @param directory directory that will be searched
     * @return list of all files from this directory
     */
    static ArrayList<File> getFilesFromPath(File directory) {
        List<File> files = new ArrayList<>()
        directory.eachFileRecurse(FileType.FILES) { file -> files.add(file) }
        return files
    }
}
