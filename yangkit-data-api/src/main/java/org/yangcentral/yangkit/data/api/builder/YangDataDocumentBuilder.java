package org.yangcentral.yangkit.data.api.builder;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public interface YangDataDocumentBuilder {
    YangDataDocument getYangDataDocument(QName qName, YangSchemaContext schemaContext);
}
