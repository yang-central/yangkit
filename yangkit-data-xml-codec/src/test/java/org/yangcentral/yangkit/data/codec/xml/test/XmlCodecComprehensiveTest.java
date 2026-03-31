package org.yangcentral.yangkit.data.codec.xml.test;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.xml.YangDataDocumentXmlCodec;
import org.yangcentral.yangkit.data.impl.operation.YangDataDocumentOperatorImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive XML codec tests covering various YANG data types and structures.
 */
public class XmlCodecComprehensiveTest {
    
    private YangSchemaContext loadSchemaContext() throws Exception {
        URL yangUrl = this.getClass().getClassLoader().getResource("comprehensive/yang");
        if (yangUrl == null) {
            return null;
        }
        String yangDir = yangUrl.getFile();
        return YangYinParser.parse(yangDir);
    }
    
    private String getNamespace(YangSchemaContext schemaContext, String moduleName) {
        if (schemaContext.getModules() == null || schemaContext.getModules().isEmpty()) {
            return null;
        }
        
        for (org.yangcentral.yangkit.model.api.stmt.Module module : schemaContext.getModules()) {
            if (module != null && module.getMainModule() != null && 
                module.getMainModule().getNamespace() != null) {
                String ns = module.getMainModule().getNamespace().getUri().toString();
                if (ns != null && (moduleName == null || module.getModuleId().getModuleName().contains(moduleName))) {
                    return ns;
                }
            }
        }
        return null;
    }
    
    @Test
    public void testComplexNestingStructure() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = 
            "<nested-container xmlns=\"" + namespace + "\">" +
                "<name>test-parent</name>" +
                "<inner>" +
                    "<value>100</value>" +
                    "<item><id>1</id><description>First item</description></item>" +
                "</inner>" +
            "</nested-container>";
        
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testStringWithPattern() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = "<string-pattern xmlns=\"" + namespace + "\">123-4567</string-pattern>";
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testNumericTypes() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String uint8Xml = "<uint8-value xmlns=\"" + namespace + "\">255</uint8-value>";
        Document uint8Doc = DocumentHelper.parseText(uint8Xml);
        
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(uint8Doc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testEnumerationType() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = "<enum-value xmlns=\"" + namespace + "\">active</enum-value>";
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testLeafList() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = 
            "<string-list xmlns=\"" + namespace + "\">" +
                "<string-list>value1</string-list>" +
                "<string-list>value2</string-list>" +
                "<string-list>value3</string-list>" +
            "</string-list>";
        
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testChoiceCase() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = 
            "<notification-choice xmlns=\"" + namespace + "\">" +
                "<email-address>test@example.com</email-address>" +
                "<email-subject>Test Subject</email-subject>" +
            "</notification-choice>";
        
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testRpcInput() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = 
            "<calculate xmlns=\"" + namespace + "\">" +
                "<input>" +
                    "<operand1>10.50</operand1>" +
                    "<operand2>5.25</operand2>" +
                    "<operation>add</operation>" +
                "</input>" +
            "</calculate>";
        
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testNotification() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = 
            "<system-event xmlns=\"" + namespace + "\">" +
                "<event-type>startup</event-type>" +
                "<severity>informational</severity>" +
                "<timestamp>2024-03-28T10:00:00Z</timestamp>" +
            "</system-event>";
        
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testAugmentHandling() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = 
            "<base-container xmlns=\"" + namespace + "\">" +
                "<base-field>base-value</base-field>" +
                "<augmented-field-1>augmented-value</augmented-field-1>" +
            "</base-container>";
        
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testConfigFalseStateData() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = 
            "<state-data xmlns=\"" + namespace + "\">" +
                "<operational-status>up</operational-status>" +
                "<last-modified>2024-03-28</last-modified>" +
            "</state-data>";
        
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testGroupingUses() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = 
            "<shipping xmlns=\"" + namespace + "\">" +
                "<street>123 Main St</street>" +
                "<city>Springfield</city>" +
                "<postal-code>12345</postal-code>" +
            "</shipping>";
        
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testIdentityRef() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = "<identityref-value xmlns=\"" + namespace + "\">tcp</identityref-value>";
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }
    
    @Test
    public void testBitsType() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = "<bits-value xmlns=\"" + namespace + "\">admin user</bits-value>";
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());
    }

    @Test
    public void testValueAttributeForLeafList() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        // Test leaf-list with value attribute instead of text content
        String xmlStr = "<leaf-list-test xmlns=\"" + namespace + "\">" +
                        "<item value=\"first-value\"/>" +
                        "<item>second-value</item>" +  // mix both encodings
                        "</leaf-list-test>";
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());

        String serialized = codec.serialize(result).asXML();
        assertTrue(serialized.contains("<item>first-value</item>"));
        assertTrue(serialized.contains("<item>second-value</item>"));
    }

    @Test
    public void testInsertAttributeForOrderedList() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        // Test ordered list with insert attribute
        // Note: Requires schema with ordered-by user list
        String xmlStr = "<ordered-list xmlns=\"" + namespace + "\">" +
                        "<entry>" +
                        "<name>second-item</name>" +
                        "</entry>" +
                        "<entry nc:insert=\"first\" xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "<name>first-item</name>" +
                        "</entry>" +
                        "</ordered-list>";
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        ValidatorResult validation = validatorBuilder.build();
        assertTrue(validation.isOk());

        String serialized = codec.serialize(result).asXML();
        assertTrue(serialized.indexOf("<name>first-item</name>") < serialized.indexOf("<name>second-item</name>"));
    }

    @Test
    public void testOperationAttributeAppliedDuringMerge() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;

        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;

        String baseXml = "<ordered-list xmlns=\"" + namespace + "\">" +
                "<entry><name>second-item</name></entry>" +
                "</ordered-list>";
        String editXml = "<ordered-list xmlns=\"" + namespace + "\">" +
                "<entry nc:operation=\"create\" nc:insert=\"first\" xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<name>first-item</name>" +
                "</entry>" +
                "</ordered-list>";

        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder baseValidator = new ValidatorResultBuilder();
        ValidatorResultBuilder editValidator = new ValidatorResultBuilder();
        YangDataDocument baseDoc = codec.deserialize(DocumentHelper.parseText(baseXml).getRootElement(), baseValidator);
        YangDataDocument editDoc = codec.deserialize(DocumentHelper.parseText(editXml).getRootElement(), editValidator);

        assertNotNull(baseDoc);
        assertNotNull(editDoc);
        assertTrue(baseValidator.build().isOk());
        assertTrue(editValidator.build().isOk());

        YangDataDocumentOperatorImpl operator = new YangDataDocumentOperatorImpl(baseDoc);
        operator.merge(editDoc, false);

        String serialized = codec.serialize(baseDoc).asXML();
        assertTrue(serialized.indexOf("<name>first-item</name>") < serialized.indexOf("<name>second-item</name>"));
    }

    @Test
    public void testSelectAttributeForAnydata() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;
        
        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;
        
        String xmlStr = "<anydata-node nc:select=\"//leaf-list-test\" xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns=\"" + namespace + "\">" +
                        "<ordered-list>" +
                        "<entry><name>first-item</name></entry>" +
                        "</ordered-list>" +
                        "<leaf-list-test>" +
                        "<item>value1</item>" +
                        "</leaf-list-test>" +
                        "</anydata-node>";
        Document xmlDoc = DocumentHelper.parseText(xmlStr);
        
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);
        
        assertNotNull(result);
        ValidatorResult validation = validatorBuilder.build();
        assertTrue(validation.isOk());

        String serialized = codec.serialize(result).asXML();
        assertTrue(serialized.contains("<leaf-list-test>"));
        assertFalse(serialized.contains("<ordered-list>"));
    }

    @Test
    public void testSelectAttributeForAnyxml() throws Exception {
        YangSchemaContext schemaContext = loadSchemaContext();
        if (schemaContext == null) return;

        String namespace = getNamespace(schemaContext, "xml-test-types");
        if (namespace == null) return;

        String xmlStr = "<any-xml nc:select=\"//*[local-name()='beta']\" xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns=\"" + namespace + "\">" +
                "<alpha><value>a</value></alpha>" +
                "<beta><value>b</value></beta>" +
                "</any-xml>";
        Document xmlDoc = DocumentHelper.parseText(xmlStr);

        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(schemaContext);
        ValidatorResultBuilder validatorBuilder = new ValidatorResultBuilder();
        YangDataDocument result = codec.deserialize(xmlDoc.getRootElement(), validatorBuilder);

        assertNotNull(result);
        assertTrue(validatorBuilder.build().isOk());

        String serialized = codec.serialize(result).asXML();
        assertTrue(serialized.contains("<beta><value>b</value></beta>"));
        assertFalse(serialized.contains("<alpha><value>a</value></alpha>"));
    }
}
