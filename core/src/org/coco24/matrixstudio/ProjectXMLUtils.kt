package org.coco24.matrixstudio

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import org.dom4j.DocumentFactory
import org.dom4j.Element
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.SAXWriter
import org.dom4j.io.XMLWriter
import java.lang.Exception
import com.badlogic.gdx.utils.Array
import java.io.InputStreamReader

object ProjectXMLUtils
{
    val optimized = false //OPTIMIZED
    fun addColorizedCellElement(parent: Element, colorizedLEDCell: LEDCell): Element
    {
        val element = parent.addElement("Cell")
        element.addAttribute("type", "colorized")
        element.addText(colorizedLEDCell.color.toString())
        return element
    }
    fun addQueuedCellElement(parent: Element, project: Project, queuedLEDCell: LEDCell.QueuedLEDCell): Element
    {
        val element = parent.addElement("Cell")
        element.addAttribute("type", "queued")
        element.addText("${project.getQueues().indexOf(queuedLEDCell.ledsQueue)}@${queuedLEDCell.ledsQueueOffset}")
        return element
    }
    class XMLWritingException: Exception()
    class XMLReadingException: Exception()
    fun writeXML(project: Project, fileHandle: FileHandle)
    {
        val documentFactory = DocumentFactory.getInstance()
        val document = documentFactory.createDocument()
        val rootElement = document.addElement("Project")
        rootElement.addAttribute("name", project.getName())
        val queuesElement = rootElement.addElement("Queues")
        project.getQueues().forEach {
            val queueElement = queuesElement.addElement("Queue")
            queueElement.addAttribute("name", it.name)
            queueElement.addAttribute("type", "colorized")//SIZE_OPTIMIZED
            it.forEach { cell ->
                val cellElement = addColorizedCellElement(queueElement, cell)
                cellElement.remove(cellElement.attribute("type")) //SIZE_OPTIMIZED
            }
        }
        val seqsElement = rootElement.addElement("Sequences")
        project.getSequences().forEach {
            val seqElement = seqsElement.addElement("Sequence")
            seqElement.addAttribute("name", it.name)
            seqElement.addAttribute("width", it.width.toString())
            seqElement.addAttribute("height", it.height.toString())
            it.forEach {page ->
                var lastCell: LEDCell? = null//SIZE_OPTIMIZED
                var same = true//SIZE_OPTIMIZED
                val pageElement = seqElement.addElement("Page")
                page.leds.forEach {row ->
                    row.forEach {cell ->
                        if(cell is LEDCell.ColorizedLEDCell)
                        {
                            addColorizedCellElement(pageElement, cell)
                        }
                        else if(cell is LEDCell.QueuedLEDCell)
                        {
                            addQueuedCellElement(pageElement, project, cell)
                        }
                        if(cell.javaClass != lastCell?.javaClass?:cell.javaClass) same = false//SIZE_OPTIMIZED
                        lastCell = cell//SIZE_OPTIMIZED
                    }
                }
                if(same) //SIZE_OPTIMIZED
                {
                    pageElement.elements().forEach { element ->
                        element.remove(element.attribute("type"))
                    }
                    pageElement.addAttribute("type", if(lastCell is LEDCell.ColorizedLEDCell) "colorized" else if(lastCell is LEDCell.QueuedLEDCell) "queued" else throw XMLWritingException())

                }
            }
        }
//        val outputFormat = OutputFormat.createPrettyPrint();
//        outputFormat.encoding = "UTF-8";
//        outputFormat.setIndent(true); //设置是否缩进
//        outputFormat.indent = "  "; //以四个空格方式实现缩进
//        outputFormat.isNewlines = true; //设置是否换行
        val xmlWriter = XMLWriter(fileHandle.writer(false, "UTF-8"))//, outputFormat)
        xmlWriter.write(document)
        xmlWriter.close()
    }
    fun loadFromXML(project: Project, fileHandle: FileHandle)
    {
        val queues = Array<NamedQueue<LEDCell>>()
        val sequences = Array<LEDSequence>()

        val documentFactory = DocumentFactory.getInstance()
        val saxReader = SAXReader(documentFactory)
        val document = saxReader.read(fileHandle.reader("UTF-8"))
        val projectElement = document.rootElement

        val queuesElement = projectElement.element("Queues")
        queuesElement.elements().forEach {queueElement ->
            val namedQueue = NamedQueue<LEDCell>(queueElement.attributeValue("name"))
            queueElement.elements().forEach { cellElement ->
                namedQueue.addLast(LEDCell.ColorizedLEDCell(Color.valueOf(cellElement.text)))
            }
            queues.add(namedQueue)
        }
        val sequencesElement = projectElement.element("Sequences")
        sequencesElement.elements().forEach {sequenceElement ->
            val sequence = LEDSequence(sequenceElement.attributeValue("name"), sequenceElement.attributeValue("width").toInt(), sequenceElement.attributeValue("height").toInt())
            sequenceElement.elements().forEach { pageElement ->
                val type: String? = pageElement.attributeValue("type")
                val page = LEDPage(sequence.width, sequence.height)
                page.leds.clear()
                var row = Array<LEDCell>()
                pageElement.elements().forEach {cellElement ->
                    when(type?:cellElement.attributeValue("type")?:throw XMLReadingException())
                    {
                        "colorized" -> row.add(LEDCell.ColorizedLEDCell(Color.valueOf(cellElement.text)))
                        "queued" ->
                        {
                            val texts = cellElement.text.split("@")
                            row.add(LEDCell.QueuedLEDCell(queues[texts[0].toInt()], texts[1].toInt()))
                        }
                        else -> throw XMLReadingException()
                    }
                    if(row.size == page.getWidth())
                    {
                        page.leds.add(row)
                        row = Array()
                    }
                }
                sequence.add(page)
            }
            sequences.add(sequence)
        }

        project.setName(projectElement.attributeValue("name"))
        project.setQueues(queues)
        project.setSequences(sequences)
    }
}