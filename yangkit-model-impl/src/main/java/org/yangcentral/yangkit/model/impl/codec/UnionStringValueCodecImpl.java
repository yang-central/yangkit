package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.codec.BinaryStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.BitsStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.BooleanStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.Decimal64StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.EnumerationStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.IdentityRefStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.InstanceIdentifierStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.Int16StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.Int32StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.Int64StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.Int8StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.LeafRefStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.StringStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.UInt16StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.UInt32StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.UInt64StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.UInt8StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.UnionStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.restriction.Union;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

public class UnionStringValueCodecImpl extends ComplexStringValueCodecImpl<Object> implements UnionStringValueCodec {
   public UnionStringValueCodecImpl(TypedDataNode schemaNode) {
      super(schemaNode);
   }

   public Object deserialize(Restriction<Object> restriction, String input) throws YangCodecException {
      Union union = (Union)restriction;
      if (union.getDerived() != null) {
         Union derivedUnion = (Union)union.getDerived().getType().getRestriction();
         return this.deserialize(derivedUnion, (String)input);
      } else {
         List<Type> types = union.getTypes();
         Object o = null;
         Iterator iterator = types.iterator();

         while(iterator.hasNext()) {
            Type type = (Type)iterator.next();
            StringValueCodec<?> codec = StringValueCodecFactory.getInstance().getStringValueCodec(this.getSchemaNode(), type.getRestriction());
            Restriction typeRes = type.getRestriction();

            try {
               o = codec.deserialize(typeRes, input);
               break;
            } catch (Exception e) {
            }
         }

         if (o == null) {
            throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
         } else {
            return o;
         }
      }
   }

   public String serialize(Restriction<Object> restriction, Object output) throws YangCodecException {
      if (!restriction.evaluate(output)) {
         throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
      } else {
         List<Type> types = ((Union)restriction).getActualTypes();
         String s = null;
         Iterator iterator = types.iterator();

         while(iterator.hasNext()) {
            Type type = (Type)iterator.next();
            StringValueCodec<?> codec = StringValueCodecFactory.getInstance().getStringValueCodec(this.getSchemaNode(), type.getRestriction());
            Restriction typeRes = type.getRestriction();

            try {
               if (codec instanceof BinaryStringValueCodec) {
                  s = (String)((BinaryStringValueCodec)codec).serialize(typeRes, (byte[])output);
               } else if (codec instanceof BitsStringValueCodec) {
                  s = (String)((BitsStringValueCodec)codec).serialize(typeRes, (List)output);
               } else if (codec instanceof BooleanStringValueCodec) {
                  s = (String)((BooleanStringValueCodec)codec).serialize(typeRes, (Boolean)output);
               } else if (codec instanceof Decimal64StringValueCodec) {
                  s = (String)((Decimal64StringValueCodec)codec).serialize(typeRes, (BigDecimal)output);
               } else if (codec instanceof EnumerationStringValueCodec) {
                  s = (String)((EnumerationStringValueCodec)codec).serialize(typeRes, (String)output);
               } else if (codec instanceof IdentityRefStringValueCodec) {
                  s = (String)((IdentityRefStringValueCodec)codec).serialize(typeRes, (QName)output);
               } else if (codec instanceof InstanceIdentifierStringValueCodec) {
                  s = (String)((InstanceIdentifierStringValueCodec)codec).serialize(typeRes, (YangAbsoluteLocationPath)output);
               } else if (codec instanceof Int8StringValueCodec) {
                  s = (String)((Int8StringValueCodec)codec).serialize(typeRes, (Byte)output);
               } else if (codec instanceof Int16StringValueCodec) {
                  s = (String)((Int16StringValueCodec)codec).serialize(typeRes, (Short)output);
               } else if (codec instanceof Int32StringValueCodec) {
                  s = (String)((Int32StringValueCodec)codec).serialize(typeRes, (Integer)output);
               } else if (codec instanceof Int64StringValueCodec) {
                  s = (String)((Int64StringValueCodec)codec).serialize(typeRes, (Long)output);
               } else if (codec instanceof UInt8StringValueCodec) {
                  s = (String)((UInt8StringValueCodec)codec).serialize(typeRes, (Short)output);
               } else if (codec instanceof UInt16StringValueCodec) {
                  s = (String)((UInt16StringValueCodec)codec).serialize(typeRes, (Integer)output);
               } else if (codec instanceof UInt32StringValueCodec) {
                  s = (String)((UInt32StringValueCodec)codec).serialize(typeRes, (Long)output);
               } else if (codec instanceof UInt64StringValueCodec) {
                  s = (String)((UInt64StringValueCodec)codec).serialize(typeRes, (BigInteger)output);
               } else if (codec instanceof LeafRefStringValueCodec) {
                  s = (String)((LeafRefStringValueCodec)codec).serialize(typeRes, output);
               } else if (codec instanceof StringStringValueCodec) {
                  s = (String)((StringStringValueCodec)codec).serialize(typeRes, (String)output);
               } else if (codec instanceof UnionStringValueCodec) {
                  s = (String)((UnionStringValueCodec)codec).serialize(typeRes, output);
               }
            } catch (YangCodecException var10) {
            } catch (ClassCastException var10) {
            }
         }

         if (s == null) {
            throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
         } else {
            return s;
         }
      }
   }
}
