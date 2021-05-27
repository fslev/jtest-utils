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

public class XmlUtils {
    /**
     * @param xml
     * @return check is valid XML
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
     * @param xml
     * @return Document representation of String XML
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

    public static String toString(Node xml) throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.transform(new DOMSource(xml), new StreamResult(writer));
        return writer.toString().replaceAll("(?m)^[ \t]*\r?\n", "");
    }

    /**
     * Walks the XML, applies a function on each XML node element and attribute
     * and returns resulted values mapped to their corresponding XML paths
     *
     * @param xml
     * @param processFunction Applied for each XML node element and attribute
     * @param <R>             Process function return type
     * @return A map between resulted node values and their corresponding XML paths
     */
    public static <R> Map<String, R> walkXmlAndProcessNodes(Node xml, Function<String, R> processFunction) {
        Map<String, R> resultsMap = new HashMap<>();
        Xml.walkAndProcessNode(xml, processFunction, "", resultsMap);
        return resultsMap;
    }
}