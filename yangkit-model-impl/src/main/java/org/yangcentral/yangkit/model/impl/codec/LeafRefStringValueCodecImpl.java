package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.LeafRefStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

public class LeafRefStringValueCodecImpl extends ComplexStringValueCodecImpl<Object> implements LeafRefStringValueCodec {
   public LeafRefStringValueCodecImpl(TypedDataNode schemaNode) {
      super(schemaNode);
   }

   public Object deserialize(Restriction<Object> restriction, String input) throws YangCodecException {
      TypedDataNode typedDataNode = ((LeafRef)restriction).getReferencedNode();
      if (typedDataNode == null) {
         throw new YangCodecException(ErrorCode.REFERENCE_NODE_NOT_FOUND.getFieldName());
      } else {
         StringValueCodec stringValueCodec = StringValueCodecFactory.getInstance().getStringValueCodec(typedDataNode);
         return stringValueCodec.deserialize(typedDataNode.getType().getRestriction(), input);
      }
   }

   public String serialize(Restriction<Object> restriction, Object output) throws YangCodecException {
      TypedDataNode typedDataNode = ((LeafRef)restriction).getReferencedNode();
      if (typedDataNode == null) {
         throw new YangCodecException(ErrorCode.REFERENCE_NODE_NOT_FOUND.getFieldName());
      } else {
         StringValueCodec stringValueCodec = StringValueCodecFactory.getInstance().getStringValueCodec(typedDataNode);
         return (String)stringValueCodec.serialize(typedDataNode.getType().getRestriction(), output);
      }
   }
}
