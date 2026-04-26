package io.github.emaarco.bpmn.adapter.outbound.engine

import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

internal object SecureBpmnParser {

    private val saxFactory = SAXParserFactory.newInstance().also { factory ->
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    }

    fun readModelFromStream(inputStream: InputStream): BpmnModelInstance {
        val bytes = inputStream.readBytes()
        rejectDoctypeDeclaration(bytes)
        return Bpmn.readModelFromStream(bytes.inputStream())
    }

    private fun rejectDoctypeDeclaration(bytes: ByteArray) {
        try {
            saxFactory.newSAXParser().parse(bytes.inputStream(), object : DefaultHandler() {
                override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
                    // Abort as soon as we reach the first element — no DOCTYPE was encountered
                    throw EarlyAbortException()
                }
            })
        } catch (e: EarlyAbortException) {
            return // clean exit — no DOCTYPE found
        } catch (e: SAXParseException) {
            // disallow-doctype-decl throws SAXParseException when DOCTYPE is encountered
            throw SecurityException("DOCTYPE declarations are not allowed in BPMN files", e)
        }
    }

    private class EarlyAbortException : SAXException()
}
