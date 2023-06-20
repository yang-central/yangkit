package org.yangcentral.yangkit.data.api.codec;

import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public interface YangDataDocumentCodec<T> {
   YangSchemaContext getSchemaContext();

   YangDataDocument deserialize(T document, ValidatorResultBuilder builder);

   T serialize(YangDataDocument yangDataDocument);
}
