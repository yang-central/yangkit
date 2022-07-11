package org.yangcentral.yangkit.data.api.codec;

import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

public interface YangDataCodec<S extends SchemaNode, D extends YangData<S>, T> {
   S getSchemaNode();

   YangSchemaContext getSchemaContext();

   D deserialize(T element, ValidatorResultBuilder validatorResultBuilder);

   T serialize(YangData<?> yangData);
}
