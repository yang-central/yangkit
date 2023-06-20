package org.yangcentral.yangkit.data.impl.builder;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.impl.model.YangDataDocumentImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public class YangDataDocumentBuilder implements org.yangcentral.yangkit.data.api.builder.YangDataDocumentBuilder {
    @Override
    public YangDataDocument getYangDataDocument(QName qName, YangSchemaContext schemaContext) {
        return new YangDataDocumentImpl(qName,schemaContext);
    }
}
