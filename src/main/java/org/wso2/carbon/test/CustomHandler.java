package org.wso2.carbon.test;

import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomHandler extends AbstractHandler {

    public boolean handleRequest(MessageContext messageContext) {
        try {
            Map<String, Object> transportHeaders = (Map<String, Object>) ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext().getProperty("TRANSPORT_HEADERS");
//            transportHeaders.put("Content-Type", "text/plain");
            for (Map.Entry entry : transportHeaders.entrySet()) {
                System.out.println("TRANS:" + entry.getKey() + ", " + entry.getValue());
            }

            if("application/json".equalsIgnoreCase(String.valueOf(transportHeaders.get("Content-Type")))) {

                ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("messageType", "text/plain");
                ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("ContentType", "text/plain");

                RelayUtils.buildMessage(((Axis2MessageContext) messageContext).getAxis2MessageContext());

                SOAPEnvelope envelope = messageContext.getEnvelope();
                SOAPBody body = envelope.getBody();

                Iterator<?> children = body.getChildren();
                String jsonText = null;
                while (children.hasNext()) {
                    Object next = children.next();
                    if (next instanceof OMElement) {
                        OMElement element = (OMElement) next;
                        if ("text".equals(element.getLocalName())) {
                            jsonText = element.getText();
                            break;
                        }
                    }
                }

                if (jsonText == null) {
                    System.out.println("No <text> element found in SOAP body");
                }

                // Remove "@" from keys like "@key" → "key"
                Pattern pattern = Pattern.compile("\"@([^\"]+)\"\\s*:");
                Matcher matcher = pattern.matcher(jsonText);
                String cleanedJson = matcher.replaceAll("\"$1\":");

                // Replace the payload with the cleaned JSON
                JsonUtil.newJsonPayload(
                        ((Axis2MessageContext) messageContext).getAxis2MessageContext(),
                        cleanedJson, true, true);

                ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("messageType", "application/json");
                ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("ContentType", "application/json");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }


//    private void modifyJsonStructure(OMElement jsonObjectElement) {
//        try {
//            OMFactory factory = OMAbstractFactory.getOMFactory();
//
//            // Example 1: Add new elements
//            OMElement authTokenElement = factory.createOMElement("authToken", null);
//            authTokenElement.setText("custom-auth-token-12345");
//            jsonObjectElement.addChild(authTokenElement);
//
//            OMElement timestampElement = factory.createOMElement("timestamp", null);
//            timestampElement.setText(String.valueOf(System.currentTimeMillis()));
//            jsonObjectElement.addChild(timestampElement);
//
//            // Example 2: Modify existing elements
//            OMElement versionElement = jsonObjectElement.getFirstChildWithName(
//                    new javax.xml.namespace.QName("version"));
//            if (versionElement != null) {
//                System.out.println("Original version: " + versionElement.getText());
//                versionElement.setText("v2.0-modified");
//            }
//
//            // Example 3: Add conditional logic based on existing values
//            OMElement testElement = jsonObjectElement.getFirstChildWithName(
//                    new javax.xml.namespace.QName("test"));
//            if (testElement != null) {
//                String testValue = testElement.getText();
//                System.out.println("Original test value: " + testValue);
//
//                // Add a processed flag based on test value
//                OMElement processedElement = factory.createOMElement("processed", null);
//                processedElement.setText(testValue.equals("bgbg") ? "true" : "false");
//                jsonObjectElement.addChild(processedElement);
//
//                // Modify the test value
//                testElement.setText(testValue + "-processed");
//            }
//
//            // Example 4: Add nested object
//            OMElement metadataElement = factory.createOMElement("metadata", null);
//
//            OMElement handlerElement = factory.createOMElement("handler", null);
//            handlerElement.setText("CustomAPIAuthenticationHandler");
//            metadataElement.addChild(handlerElement);
//
//            OMElement processingTimeElement = factory.createOMElement("processingTime", null);
//            processingTimeElement.setText(String.valueOf(System.currentTimeMillis()));
//            metadataElement.addChild(processingTimeElement);
//
//            jsonObjectElement.addChild(metadataElement);
//
//        } catch (Exception e) {
//            System.err.println("Error modifying JSON structure: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

}
