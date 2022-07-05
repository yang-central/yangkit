package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.model.api.codec.StringValueCodec;
import org.yangcentral.yangkit.model.api.restriction.Binary;
import org.yangcentral.yangkit.model.api.restriction.Bits;
import org.yangcentral.yangkit.model.api.restriction.Decimal64;
import org.yangcentral.yangkit.model.api.restriction.Empty;
import org.yangcentral.yangkit.model.api.restriction.Enumeration;
import org.yangcentral.yangkit.model.api.restriction.IdentityRef;
import org.yangcentral.yangkit.model.api.restriction.InstanceIdentifier;
import org.yangcentral.yangkit.model.api.restriction.Int16;
import org.yangcentral.yangkit.model.api.restriction.Int32;
import org.yangcentral.yangkit.model.api.restriction.Int64;
import org.yangcentral.yangkit.model.api.restriction.Int8;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.restriction.UInt16;
import org.yangcentral.yangkit.model.api.restriction.UInt32;
import org.yangcentral.yangkit.model.api.restriction.UInt64;
import org.yangcentral.yangkit.model.api.restriction.UInt8;
import org.yangcentral.yangkit.model.api.restriction.Union;
import org.yangcentral.yangkit.model.api.restriction.YangBoolean;
import org.yangcentral.yangkit.model.api.restriction.YangString;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

public class StringValueCodecFactory {
   private static final StringValueCodecFactory ourInstance = new StringValueCodecFactory();

   public static StringValueCodecFactory getInstance() {
      return ourInstance;
   }

   private StringValueCodecFactory() {
   }

   public StringValueCodec<?> getStringValueCodec(TypedDataNode dataNode) {
      if (dataNode.getType() == null) {
         assert false;

         return null;
      } else {
         Restriction restriction = dataNode.getType().getRestriction();
         return this.getStringValueCodec(dataNode, restriction);
      }
   }

   public StringValueCodec<?> getStringValueCodec(TypedDataNode dataNode, Restriction restriction) {
      if (restriction instanceof LeafRef) {
         return new LeafRefStringValueCodecImpl(dataNode);
      } else if (restriction instanceof IdentityRef) {
         return new IdentityRefStringValueCodecImpl(dataNode);
      } else if (restriction instanceof InstanceIdentifier) {
         return new InstanceIdentifierStringValueCodecImpl(dataNode);
      } else {
         return (StringValueCodec)(restriction instanceof Union ? new UnionStringValueCodecImpl(dataNode) : this.getStringValueCodec(restriction));
      }
   }

   public StringValueCodec<?> getStringValueCodec(Restriction restriction) {
      if (restriction instanceof Binary) {
         return new BinaryStringValueCodecImpl();
      } else if (restriction instanceof Bits) {
         return new BitsStringValueCodecImpl();
      } else if (restriction instanceof YangBoolean) {
         return new BooleanStringValueCodecImpl();
      } else if (restriction instanceof Decimal64) {
         return new Decimal64StringValueCodecImpl();
      } else if (restriction instanceof Enumeration) {
         return new EnumerationStringValudeCodecImpl();
      } else if (restriction instanceof Int8) {
         return new Int8StringValueCodecImpl();
      } else if (restriction instanceof Int16) {
         return new Int16StringValueCodecImpl();
      } else if (restriction instanceof Int32) {
         return new Int32StringValueCodecImpl();
      } else if (restriction instanceof Int64) {
         return new Int64StringValueCodecImpl();
      } else if (restriction instanceof UInt8) {
         return new UInt8StringValueCodecImpl();
      } else if (restriction instanceof UInt16) {
         return new UInt16StringValueCodecImpl();
      } else if (restriction instanceof UInt32) {
         return new UInt32StringValueCodecImpl();
      } else if (restriction instanceof UInt64) {
         return new UInt64StringValueCodecImpl();
      } else if (restriction instanceof YangString) {
         return new StringStringValueCodecImpl();
      } else if (restriction instanceof Empty) {
         return new EmptyStringValueCodecImpl();
      } else {
         throw new IllegalArgumentException("unrecognized restriction type");
      }
   }
}
