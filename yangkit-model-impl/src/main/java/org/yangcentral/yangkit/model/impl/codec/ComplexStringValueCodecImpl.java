package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.model.api.codec.ComplexStringValueCodec;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

public abstract class ComplexStringValueCodecImpl<T> extends StringValueCodecImpl<T> implements ComplexStringValueCodec<T> {
   private TypedDataNode schemaNode;

   public ComplexStringValueCodecImpl(TypedDataNode schemaNode) {
      this.schemaNode = schemaNode;
   }

   public TypedDataNode getSchemaNode() {
      return this.schemaNode;
   }
}
