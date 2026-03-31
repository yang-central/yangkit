package org.yangcentral.yangkit.data.api.codec;

import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public interface YangDataDocumentCodec<T> {
   YangSchemaContext getSchemaContext();

   YangDataDocument deserialize(T document, ValidatorResultBuilder builder);

   default YangDataDocument deserialize(T document, ValidatorResultBuilder builder,
                                        AnydataValidationContextResolver resolver) {
      return deserialize(document, builder);
   }

   default YangDataDocument deserialize(T document, ValidatorResultBuilder builder,
                                        AnydataValidationOptions options) {
      return deserialize(document, builder, (AnydataValidationContextResolver) options);
   }

   T serialize(YangDataDocument yangDataDocument);
}
