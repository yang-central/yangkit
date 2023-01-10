package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.restriction.Binary;
import org.yangcentral.yangkit.model.api.restriction.Section;
import org.yangcentral.yangkit.model.api.restriction.YangString;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.type.Length;
import org.yangcentral.yangkit.model.impl.stmt.type.LengthImpl;
import org.yangcentral.yangkit.util.ModelUtil;

import java.math.BigInteger;

public class BinaryImpl extends RestrictionImpl<byte[]> implements Binary {
   private Length length;

   public BinaryImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public BinaryImpl(YangContext context) {
      super(context);
   }

   public Length getLength() {
      return this.length;
   }

   public ValidatorResult setLength(Length length) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if(length == null){
         this.length = null;
         return validatorResultBuilder.build();
      }
      length.setBound(this.getHighBound(), this.getLowBound());
      validatorResultBuilder.merge(length.build(BuildPhase.GRAMMAR));

      if (this.getDerived() != null && !length.isSubSet(((Binary)this.getDerived().getType().getRestriction()).getEffectiveLength())) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(length,ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity(),
                 ErrorTag.BAD_ELEMENT,ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getFieldName()));
         if (ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity() == Severity.ERROR) {
            return validatorResultBuilder.build();
         }
      }

      this.length = length;
      return validatorResultBuilder.build();
   }

   private BigInteger getHighBound() {
      return this.getDerived() != null ? ((Binary)this.getDerived().getType()).getMaxLength() : YangString.MAX_LENGTH;
   }

   private BigInteger getLowBound() {
      return this.getDerived() != null ? ((Binary)this.getDerived().getType()).getMinLength() : YangString.MIN_LENGTH;
   }

   public BigInteger getMaxLength() {
      return this.length != null ? (BigInteger)this.getLength().getMax() : this.getHighBound();
   }

   public BigInteger getMinLength() {
      return this.length != null ? (BigInteger)this.getLength().getMin() : this.getLowBound();
   }

   public boolean evaluated(byte[] value) {
      if (this.getLength() != null) {
         return this.length.evaluate(BigInteger.valueOf((long) value.length));
      } else if (this.getDerived() != null) {
         return this.getDerived().getType().getRestriction().evaluated(value);
      } else {
         Section section = new Section(this.getHighBound(), this.getLowBound());
         return section.evaluate(BigInteger.valueOf((long) value.length));
      }
   }

   public Length getEffectiveLength() {
      if (this.getLength() != null) {
         return this.getLength();
      } else {
         Typedef derived = this.getDerived();
         if (derived != null) {
            Binary derivedBinary = (Binary)derived.getType().getRestriction();
            return derivedBinary.getEffectiveLength();
         } else {
            Length newLength = new LengthImpl(this.getLowBound() + ".." + this.getHighBound());
            newLength.setContext(new YangContext(this.getContext()));
            newLength.setElementPosition(this.getContext().getSelf().getElementPosition());
            newLength.setParentStatement(this.getContext().getSelf());
            newLength.setBound(this.getHighBound(),this.getLowBound());
            newLength.init();
            newLength.build();
            return newLength;
         }
      }
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Binary)) {
         return false;
      } else {
         BinaryImpl another = (BinaryImpl)obj;
         Length thisLen = this.getEffectiveLength();
         Length anotherLen = another.getEffectiveLength();
         if (thisLen == null && anotherLen == null) {
            return true;
         } else {
            return thisLen != null && anotherLen != null ? thisLen.equals(anotherLen) : false;
         }
      }
   }
}
