package org.yangcentral.yangkit.data.impl.model;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.impl.util.NetconfEditUtil;
import org.yangcentral.yangkit.model.api.stmt.Anyxml;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NetconfSelectSupportTest {
    private Anyxml buildAnyxmlSchemaNode() {
        return (Anyxml) Proxy.newProxyInstance(
                Anyxml.class.getClassLoader(),
                new Class[]{Anyxml.class},
                (proxy, method, args) -> {
                    if ("getIdentifier".equals(method.getName())) {
                        return new QName("urn:test:select", "any-xml");
                    }
                    if ("toString".equals(method.getName())) {
                        return "any-xml";
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class) {
                        return false;
                    }
                    if (returnType == int.class) {
                        return 0;
                    }
                    return null;
                });
    }

    @Test
    public void anyxmlSelectIsAppliedInDataImpl() throws Exception {
        AnyXmlDataImpl anyxmlData = new AnyXmlDataImpl(buildAnyxmlSchemaNode());
        Document document = DocumentHelper.parseText(
                "<any-xml xmlns=\"urn:test:select\">"
                        + "<alpha><value>a</value></alpha>"
                        + "<beta><value>b</value></beta>"
                        + "</any-xml>");
        anyxmlData.setValue(document);
        anyxmlData.addAttribute(new Attribute(NetconfEditUtil.SELECT_QNAME, "//*[local-name()='beta']"));

        Document effective = anyxmlData.getEffectiveValue();
        assertNotNull(effective);
        assertEquals(1, effective.getRootElement().elements().size());
        assertEquals("beta", effective.getRootElement().elements().get(0).getName());
    }
}




