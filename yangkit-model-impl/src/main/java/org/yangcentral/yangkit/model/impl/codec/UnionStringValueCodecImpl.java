package org.yangcentral.yangkit.model.impl.codec;

import org.apache.commons.lang3.ObjectUtils;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.codec.*;
import org.yangcentral.yangkit.model.api.restriction.*;
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
               if(typeRes instanceof Binary){
                  if(!(output instanceof byte[])){
                     continue;
                  }
                  s = ((BinaryStringValueCodec)codec).serialize(typeRes, (byte[])output);
               } else if (typeRes instanceof Bits){
                  if(!(output instanceof List)){
                     continue;
                  }
                  s = ((BitsStringValueCodec)codec).serialize(typeRes, (List)output);
               } else if (typeRes instanceof Decimal64) {
                  if(!(output instanceof BigDecimal)){
                     continue;
                  }
                  s = ((Decimal64StringValueCodec)codec).serialize(typeRes, (BigDecimal)output);
               } else if (typeRes instanceof Empty){
                  if(!(output instanceof ObjectUtils.Null)){
                     continue;
                  }
                  s = ((EmptyStringValueCodec)codec).serialize(typeRes,(ObjectUtils.Null) output);
               } else if (typeRes instanceof Enumeration){
                  if(!(output instanceof String)){
                     continue;
                  }
                  s = ((EnumerationStringValueCodec)codec).serialize(typeRes, (String)output);
               } else if (typeRes instanceof IdentityRef){
                  if(!(output instanceof QName)){
                     continue;
                  }
                  s = ((IdentityRefStringValueCodec)codec).serialize(typeRes, (QName)output);
               } else if (typeRes instanceof InstanceIdentifier){
                  if(!(output instanceof YangAbsoluteLocationPath)){
                     continue;
                  }
                  s = ((InstanceIdentifierStringValueCodec)codec).serialize(typeRes, (YangAbsoluteLocationPath)output);
               } else if (typeRes instanceof Int16){
                  if(!(output instanceof Short)){
                     continue;
                  }
                  s = ((Int16StringValueCodec)codec).serialize(typeRes, (Short)output);
               } else if (typeRes instanceof Int32){
                  if(!(output instanceof Integer)){
                     continue;
                  }
                  s = ((Int32StringValueCodec)codec).serialize(typeRes, (Integer)output);
               } else if (typeRes instanceof Int64){
                  if(!(output instanceof Long)){
                     continue;
                  }
                  s = ((Int64StringValueCodec)codec).serialize(typeRes, (Long)output);
               }  else if (typeRes instanceof Int8){
                  if(!(output instanceof Byte)){
                     continue;
                  }
                  s = ((Int8StringValueCodec)codec).serialize(typeRes, (Byte)output);
               }  else if (typeRes instanceof UInt16){
                  if(!(output instanceof Integer)){
                     continue;
                  }
                  s = ((UInt16StringValueCodec)codec).serialize(typeRes, (Integer)output);
               }  else if (typeRes instanceof UInt32){
                  if(!(output instanceof Long)){
                     continue;
                  }
                  s = ((UInt32StringValueCodec)codec).serialize(typeRes, (Long)output);
               } else if (typeRes instanceof UInt64){
                  if(!(output instanceof BigInteger)){
                     continue;
                  }
                  s = ((UInt64StringValueCodec)codec).serialize(typeRes, (BigInteger)output);
               } else if (typeRes instanceof UInt8){
                  if(!(output instanceof Short)){
                     continue;
                  }
                  s = (String)((UInt8StringValueCodec)codec).serialize(typeRes, (Short)output);
               } else if (typeRes instanceof YangBoolean){
                  if(!(output instanceof Boolean)){
                     continue;
                  }
                  s = ((BooleanStringValueCodec)codec).serialize(typeRes, (Boolean)output);
               } else if (typeRes instanceof YangString){
                  if(!(output instanceof String)){
                     continue;
                  }
                  s = ((StringStringValueCodec)codec).serialize(typeRes, (String)output);
               } else if (typeRes instanceof LeafRef){
                  s = ((LeafRefStringValueCodec)codec).serialize(typeRes,output);
               } else if (typeRes instanceof Union){
                  s = ((UnionStringValueCodec)codec).serialize(typeRes,output);
               }
            } catch (YangCodecException e){
               continue;
            }

            if(s != null){
               break;
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
