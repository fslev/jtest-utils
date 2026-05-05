package io.jtest.utils.common;

import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Convenience wrappers around {@link javax.xml.parsers DOM parsing} and
 * {@link javax.xml.transform serialization}, plus a recursive-walk helper for processing
 * every element and attribute in a document.
 *
 * <p>The DOM parser configured here suppresses warning, error, and fatal callbacks so a
 * malformed input surfaces only as a thrown exception from {@link #toNode(String)} —
 * handy in tests where parsing is expected to fail and you don't want SAX chatter on
 * stderr.
 */
public class XmlUtils {

    private XmlUtils() {

    }

    /**
     * Returns {@code true} if {@code xml} parses as a well-formed XML document,
     * {@code false} if parsing throws {@link SAXException}, {@link IOException}, or
     * {@link ParserConfigurationException}.
     *
     * @param xml the candidate XML string
     * @return {@code true} iff {@code xml} is well-formed
     */
    public static boolean isValid(String xml) {
        try {
            toNode(xml);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            return false;
        }
        return true;
    }

    /**
     * Parses {@code xml} and returns the document's root element as a DOM {@link Node}.
     *
     * @param xml the XML document
     * @return the document element
     * @throws ParserConfigurationException if a {@link DocumentBuilder} cannot be created
     * @throws IOException                  on I/O errors reading the in-memory bytes
     * @throws SAXException                 if the document is not well-formed
     */
    public static Node toNode(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) {
            }

            @Override
            public void error(SAXParseException e) {
            }

            @Override
            public void fatalError(SAXParseException e) {
            }
        });
        return builder.parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
    }

    /**
     * Serializes the DOM {@link Node} to a string with indentation enabled and the XML
     * declaration omitted. Blank lines produced by the transformer are stripped from the
     * output for cleaner diff output in tests.
     *
     * @param xml the node to serialize
     * @return the serialized XML
     * @throws TransformerException if the JAXP transformer cannot be created or fails
     */
    public static String toString(Node xml) throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.transform(new DOMSource(xml), new StreamResult(writer));
        return writer.toString().replaceAll("(?m)^[ \t]*\r?\n", "");
    }

    /**
     * Walks the XML tree rooted at {@code xml}, applying {@code processFunction} to each
     * element name, attribute value, and text-node value. Returns a map keyed by an
     * XPath-like position string (e.g. {@code /root/item[2]/{attr:id}}) so duplicate
     * paths are disambiguated.
     *
     * <p>{@code null} results from {@code processFunction} are skipped — the function
     * acts as a filter as well as a mapper.
     *
     * @param xml             the root node to walk
     * @param processFunction applied to each element name, attribute value, and text value;
     *                        return {@code null} to skip a node
     * @param <R>             type returned by {@code processFunction}
     * @return path → processed value, with {@code null} results omitted
     */
    public static <R> Map<String, R> walkXmlAndProcessNodes(Node xml, Function<String, R> processFunction) {
        Map<String, R> resultsMap = new HashMap<>();
        Xml.walkAndProcessNode(xml, processFunction, "", resultsMap);
        return resultsMap;
    }
}
