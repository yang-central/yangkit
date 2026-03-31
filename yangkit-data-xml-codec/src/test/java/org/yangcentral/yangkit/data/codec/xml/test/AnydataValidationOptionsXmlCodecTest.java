package org.yangcentral.yangkit.data.codec.xml.test;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.AnydataValidationOptions;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.xml.YangDataDocumentXmlCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Anydata;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AnydataValidationOptionsXmlCodecTest {
    private static final String OUTER_NS = "urn:xml:test:types";
    private static final String PAYLOAD_NS = "urn:test:payload-anydata";
    private static final QName WRAPPER_QNAME = new QName(OUTER_NS, "anydata-wrapper");
    private static final QName PAYLOAD_HOLDER_QNAME = new QName(OUTER_NS, "payload-holder");
    private static final QName PAYLOAD_ROOT_QNAME = new QName(PAYLOAD_NS, "payload-root");

    private Object defaultValue(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == double.class) {
            return 0D;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T buildSchemaNode(Class<T> schemaType, QName identifier,
                                  YangSchemaContext schemaContext,
                                  Map<QName, SchemaNode> children) {
        YangContext context = new YangContext(schemaContext, null);
        return (T) Proxy.newProxyInstance(
                schemaType.getClassLoader(),
                new Class[]{schemaType},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getIdentifier":
                        case "getQName":
                            return identifier;
                        case "getArgStr":
                            return identifier.getLocalName();
                        case "getContext":
                            return context;
                        case "getTreeNodeChild":
                        case "getSchemaNodeChild":
                            return children.get(args[0]);
                        case "getTreeNodeChildren":
                        case "getSchemaNodeChildren":
                            return new ArrayList<>(children.values());
                        case "getUnknowns":
                        case "getSubElements":
                        case "getEffectiveSubStatements":
                            return Collections.emptyList();
                        case "getSubStatement":
                            return Collections.emptyList();
                        case "toString":
                            return identifier.getQualifiedName();
                        case "getSelf":
                            return proxy;
                        case "equals":
                            return proxy == args[0];
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        default:
                            return defaultValue(method.getReturnType());
                    }
                });
    }

    private YangSchemaContext buildSchemaContext(Map<QName, SchemaNode> rootChildren) {
        return (YangSchemaContext) Proxy.newProxyInstance(
                YangSchemaContext.class.getClassLoader(),
                new Class[]{YangSchemaContext.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getTreeNodeChild":
                        case "getSchemaNodeChild":
                            return rootChildren.get(args[0]);
                        case "getTreeNodeChildren":
                        case "getSchemaNodeChildren":
                            return new ArrayList<>(rootChildren.values());
                        case "getModules":
                        case "getImportOnlyModules":
                            return Collections.emptyList();
                        case "getModule":
                        case "getLatestModule":
                            if (method.getReturnType() == List.class) {
                                return Collections.emptyList();
                            }
                            return Optional.empty();
                        case "getSchemaContext":
                        case "getSelf":
                            return proxy;
                        case "toString":
                            return "test-schema-context";
                        default:
                            return defaultValue(method.getReturnType());
                    }
                });
    }

    private YangSchemaContext buildOuterSchemaContext() {
        Map<QName, SchemaNode> rootChildren = new LinkedHashMap<>();
        YangSchemaContext schemaContext = buildSchemaContext(rootChildren);
        Map<QName, SchemaNode> wrapperChildren = new LinkedHashMap<>();
        Anydata payloadHolder = buildSchemaNode(Anydata.class, PAYLOAD_HOLDER_QNAME, schemaContext,
                Collections.emptyMap());
        wrapperChildren.put(PAYLOAD_HOLDER_QNAME, payloadHolder);
        Container wrapper = buildSchemaNode(Container.class, WRAPPER_QNAME, schemaContext, wrapperChildren);
        rootChildren.put(WRAPPER_QNAME, wrapper);
        return schemaContext;
    }

    private YangSchemaContext buildPayloadSchemaContext() {
        Map<QName, SchemaNode> rootChildren = new LinkedHashMap<>();
        YangSchemaContext schemaContext = buildSchemaContext(rootChildren);
        Container payloadRoot = buildSchemaNode(Container.class, PAYLOAD_ROOT_QNAME, schemaContext,
                Collections.emptyMap());
        rootChildren.put(PAYLOAD_ROOT_QNAME, payloadRoot);
        return schemaContext;
    }

    private Document buildAnydataDocument() throws Exception {
        String xml = "<anydata-wrapper xmlns=\"" + OUTER_NS + "\">"
                + "<payload-holder>"
                + "<payload-root xmlns=\"" + PAYLOAD_NS + "\"><value>abc</value></payload-root>"
                + "</payload-holder>"
                + "</anydata-wrapper>";
        return DocumentHelper.parseText(xml);
    }

    @Test
    public void deserializeWithoutOptionsKeepsUnknownAnydataPayloadEmpty() throws Exception {
        YangSchemaContext outerSchemaContext = buildOuterSchemaContext();
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();

        YangDataDocument document = codec.deserialize(buildAnydataDocument(), validator);
        assertNotNull(document);
        assertEquals(1, document.getDataChildren().size());
        AnyDataData anyDataData = (AnyDataData) document.getDataChildren().get(0);
        assertNotNull(anyDataData.getValue());
        assertEquals(0, anyDataData.getValue().getDataChildren().size());
    }

    @Test
    public void deserializeWithSchemaMappedOptionsParsesAnydataPayload() throws Exception {
        YangSchemaContext outerSchemaContext = buildOuterSchemaContext();
        YangSchemaContext payloadSchemaContext = buildPayloadSchemaContext();
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        AnydataValidationOptions options = new AnydataValidationOptions()
                .registerSchemaContext(new QName(OUTER_NS, "payload-holder"), payloadSchemaContext);

        YangDataDocument document = codec.deserialize(buildAnydataDocument(), validator, options);
        assertNotNull(document);
        assertEquals(1, document.getDataChildren().size());
        AnyDataData anyDataData = (AnyDataData) document.getDataChildren().get(0);
        assertNotNull(anyDataData.getValue());
        assertEquals(1, anyDataData.getValue().getDataChildren().size());
        assertEquals("payload-root", anyDataData.getValue().getDataChildren().get(0).getQName().getLocalName());
    }

    @Test
    public void deserializeWithRuleBasedOptionsParsesAnydataPayload() throws Exception {
        YangSchemaContext outerSchemaContext = buildOuterSchemaContext();
        YangSchemaContext payloadSchemaContext = buildPayloadSchemaContext();
        YangDataDocumentXmlCodec codec = new YangDataDocumentXmlCodec(outerSchemaContext);
        ValidatorResultBuilder validator = new ValidatorResultBuilder();
        AnydataValidationOptions options = new AnydataValidationOptions()
                .addRule(request -> request != null
                                && request.getSchemaNodeIdentifier() != null
                                && "payload-holder".equals(request.getSchemaNodeIdentifier().getLocalName()),
                        payloadSchemaContext);

        YangDataDocument document = codec.deserialize(buildAnydataDocument(), validator, options);
        assertNotNull(document);
        AnyDataData anyDataData = (AnyDataData) document.getDataChildren().get(0);
        assertEquals(1, anyDataData.getValue().getDataChildren().size());
    }
}





