package io.github.emaarco.bpmn.adapter.outbound.engine

import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

/**
 * Camunda's Bpmn.readModelFromStream does not disable external entity resolution, making it
 * vulnerable to XXE if attacker-controlled BPMN files reach the parser. This wrapper rejects
 * any file containing a DOCTYPE declaration before handing off to Camunda.
 */
internal object SecureBpmnParser {

    private val saxFactory = SAXParserFactory.newInstance().also { factory ->
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    }

    fun readModelFromBytes(bytes: ByteArray): BpmnModelInstance {
        val stream = bytes.inputStream()
        rejectDoctypeDeclaration(stream)
        stream.reset()
        return Bpmn.readModelFromStream(stream)
    }

    private fun rejectDoctypeDeclaration(stream: InputStream) {
        try {
            saxFactory.newSAXParser().parse(stream, object : DefaultHandler() {
                override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
                    // Abort as soon as we reach the first element — no DOCTYPE was encountered
                    throw EarlyAbortException()
                }
            })
        } catch (_: EarlyAbortException) {
            return // clean exit — no DOCTYPE found
        } catch (e: SAXParseException) {
            // disallow-doctype-decl throws SAXParseException when DOCTYPE is encountered
            throw SecurityException("DOCTYPE declarations are not allowed in BPMN files", e)
        }
    }

    private class EarlyAbortException : SAXException()
}
