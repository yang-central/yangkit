package org.yangcentral.yangkit.data.impl.model;

import org.dom4j.Document;
import org.yangcentral.yangkit.data.api.model.AnyxmlData;
import org.yangcentral.yangkit.data.api.model.DataIdentifier;
import org.yangcentral.yangkit.model.api.stmt.Anyxml;

public class AnyXmlDataImpl extends YangDataImpl<Anyxml> implements AnyxmlData {
    private Document value;
    public AnyXmlDataImpl(Anyxml schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(getQName());
    }

    @Override
    public Document getValue() {
        return this.value;
    }

    @Override
    public void setValue(Document value) {
        this.value = value;
    }
    
}
