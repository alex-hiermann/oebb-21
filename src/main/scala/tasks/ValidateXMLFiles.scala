package main.scala.tasks

import com.typesafe.config.Config
import org.apache.commons.io.FilenameUtils
import org.xml.sax.SAXException

import java.io.File
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory, Validator}

protected sealed abstract class ValidateXMLFiles(xmlFileLocation: String, xsdFileLocation: String) extends Task {

  private val XML_DIRECTORY = ConfigStringVariable(xmlFileLocation)
  private val XSD_DIRECTORY = ConfigStringVariable(xsdFileLocation)

  override protected def doRun(): Unit = {
    val xsdFiles = getAllXSDNames(new File(XML_DIRECTORY))
    var files: List[File] = List()

    if (xsdFiles.nonEmpty) {
      files = getXMLFilesFromPath(new File(XML_DIRECTORY), xsdFiles)
      if (files.nonEmpty) {
        val validationMap = validateFiles(files, xsdFiles)
        validationMap foreach (entry => println(entry._1 + " is " + entry._2))
      } else println("ERROR: Nothing to validate: List of xml-files was empty!")
    } else println("ERROR: Stopped Validation: No xsd-file found!")
  }

  def getAllXSDNames(directory: File): List[String] = {
    if (directory.exists && directory.isDirectory) {
      directory.listFiles.filter(_.isFile).map(_.getAbsolutePath).toList
    } else {
      println("ERROR: Given xsd directory either doesn't exist or is no directory!")
      List[String]()
    }
  }

  def getXMLFilesFromPath(directory: File, fileNames: List[String]): List[File] = {
    var xsdNames: List[String] = List()
    fileNames.foreach(xsdFile => {
      xsdNames = xsdNames :+ FilenameUtils.removeExtension(new File(xsdFile).getName)
    })

    if (directory.exists && directory.isDirectory) {
      directory.listFiles.filter(file => {
        file.isFile && xsdNames.contains(FilenameUtils.removeExtension(file.getName))
      }).toList
    } else {
      println("ERROR: Given xml directory either doesn't exist or is no directory!")
      List[File]()
    }
  }

  /**
   * Validates all files and saves the boolean values into validationMap
   * if a file is invalid, a message with all needed information will be written into the log
   *
   * @param files    a list of strings with the xml file-paths that are going to be checked for validation
   * @param xsdFiles an array of strings with the paths for the xsd validation files
   */
  def validateFiles(files: List[File], xsdFiles: List[String]): Map[String, Boolean] = {
    var validationMap: Map[String, Boolean] = Map()
    files.foreach(xmlFile => {
      var xsdFile = ""
      xsdFiles.foreach(file => {
        val xsdFileNameWithOutExt = FilenameUtils.removeExtension(new File(file).getName)
        val xmlFileNameWithOutExt = FilenameUtils.removeExtension(xmlFile.getName)
        if (xmlFileNameWithOutExt.equals(xsdFileNameWithOutExt)) xsdFile = file
      })

      if (xsdFile.trim.nonEmpty) {
        try {
          val schemaFactory: SchemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
          val schema: Schema = schemaFactory.newSchema(new File(xsdFile))
          val validator: Validator = schema.newValidator()
          validator.validate(new StreamSource(xmlFile))
          validationMap += (xmlFile.getAbsolutePath -> true)
        } catch {
          case _: SAXException =>
            validationMap.+(xmlFile.getAbsolutePath -> false)
            println(s"WARNING: Invalid file found: ${xmlFile.getAbsolutePath}")
          case _: Exception =>
            validationMap.+(xmlFile.getAbsolutePath -> false)
            println(s"WARNING: A exception has been thrown while validating file: ${xmlFile.getAbsolutePath}")
        }
      } else {
        validationMap.+(xmlFile.getAbsolutePath -> false)
        println(s"ERROR: XML schema not found for: ${xmlFile.getAbsolutePath}")
      }
    })
    validationMap
  }

  override def init(manager: TaskManager, config: Config): Option[InitError] = {
    initAllConfigVariables(config, XML_DIRECTORY, XSD_DIRECTORY)
  }

  override val isKillable: Boolean = false

}

/**
 * Used to validate the export
 */
final class ValidateXMLExport() extends ValidateXMLFiles("", "")

/**
 * For testing usages only
 *
 * @param xmlFileLocation path to a directory with xml files
 * @param xsdFileLocation path to the directory with the xsd files
 */
final class ValidateTestXMLExport(xmlFileLocation: String, xsdFileLocation: String) extends ValidateXMLFiles("", "")