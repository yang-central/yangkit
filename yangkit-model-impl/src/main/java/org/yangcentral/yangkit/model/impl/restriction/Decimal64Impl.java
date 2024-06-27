package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.restriction.Decimal64;
import org.yangcentral.yangkit.model.api.restriction.Section;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.type.FractionDigits;
import org.yangcentral.yangkit.model.api.stmt.type.Range;
import org.yangcentral.yangkit.model.impl.stmt.type.RangeImpl;
import org.yangcentral.yangkit.util.ModelUtil;

import java.math.BigDecimal;
import java.util.Iterator;

public class Decimal64Impl extends RestrictionImpl<BigDecimal> implements Decimal64 {
   private FractionDigits fractionDigits;
   private Range range;

   public Decimal64Impl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public Decimal64Impl(YangContext context) {
      super(context);
   }

   public FractionDigits getFractionDigits() {
      return this.fractionDigits;
   }

   public void setFractionDigits(FractionDigits fractionDigits) {
      this.fractionDigits = fractionDigits;
   }

   public void setRange(Range range) {
      this.range = range;
   }

   public Range getRange() {
      return this.range;
   }

   private BigDecimal getHighBound() {
      return this.getDerived() != null ? ((Decimal64)this.getDerived().getType().getRestriction()).getRangeMax() : new BigDecimal(9.223372036854776E18 * Math.pow(10.0, (double)(0 - this.fractionDigits.getValue())));
   }

   public BigDecimal getRangeMax() {
      return this.getRange() != null ? (BigDecimal)this.getRange().getMax() : this.getHighBound();
   }

   private BigDecimal getLowBound() {
      return this.getDerived() != null ? ((Decimal64)this.getDerived().getType().getRestriction()).getRangeMin() : new BigDecimal(-9.223372036854776E18 * Math.pow(10.0, (double)(0 - this.fractionDigits.getValue())));
   }

   public BigDecimal getRangeMin() {
      return this.getRange() != null ? (BigDecimal)this.getRange().getMin() : this.getLowBound();
   }

   public ValidatorResult validate() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (null == this.range) {
         return validatorResultBuilder.build();
      } else {
         this.range.setBound(this.getHighBound(), this.getLowBound());
         validatorResultBuilder.merge(this.range.build(BuildPhase.GRAMMAR));

         if (this.getDerived() != null && !this.range.isSubSet(((Decimal64)this.getDerived().getType().getRestriction()).getEffectiveRange())) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(range,ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity(),
                    ErrorTag.BAD_ELEMENT,ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getFieldName()));
         }
         return validatorResultBuilder.build();
      }
   }

   public boolean evaluate(BigDecimal value) {
      if (this.getRange() != null) {
         Iterator<Section> iterator = this.getRange().getSections().iterator();

         Section section;
         do {
            if (!iterator.hasNext()) {
               return false;
            }

            section = iterator.next();
         } while(value.compareTo((BigDecimal)section.getMin()) < 0 || value.compareTo((BigDecimal)section.getMax()) > 0);

         return true;
      } else {
         return value.compareTo(this.getRangeMin()) >= 0 && value.compareTo(this.getRangeMax()) <= 0;
      }
   }

   public Range getEffectiveRange() {
      if (this.range != null) {
         return this.range;
      } else if (this.getDerived() != null) {
         Range derivedRange = ((Decimal64)this.getDerived().getType().getRestriction()).getEffectiveRange();
         return derivedRange;
      } else {
//         Range newRange = new RangeImpl(this.getLowBound() + ".." + this.getHighBound());
//         newRange.setContext(new YangContext(this.getContext()));
//         newRange.setElementPosition(this.getContext().getSelf().getElementPosition());
//         newRange.setParentStatement(this.getContext().getSelf());
//         newRange.setBound(this.getHighBound(),this.getLowBound());
//         newRange.init();
//         newRange.build();
//         return newRange;
         return null;
      }
   }

   public FractionDigits getEffectiveFractionDigits() {
      if (this.fractionDigits != null) {
         return this.fractionDigits;
      } else if (this.getDerived() != null) {
         FractionDigits derivedFractionDigits = ((Decimal64)this.getDerived().getType().getRestriction()).getEffectiveFractionDigits();
         return derivedFractionDigits;
      } else {
         return null;
      }
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Decimal64)) {
         return false;
      } else {
         Decimal64Impl another = (Decimal64Impl)obj;
         Range thisRange = this.getEffectiveRange();
         Range anotherRange = another.getEffectiveRange();
         if (thisRange == null) {
            if (anotherRange != null) {
               return false;
            }
         } else {
            if (anotherRange == null) {
               return false;
            }

            if (!thisRange.equals(anotherRange)) {
               return false;
            }
         }

         FractionDigits thisFractionDigits = this.getEffectiveFractionDigits();
         FractionDigits anotherFractionDigits = another.getEffectiveFractionDigits();
         if (thisFractionDigits == null) {
            if (anotherFractionDigits != null) {
               return false;
            }
         } else {
            if (anotherFractionDigits == null) {
               return false;
            }

            if (!thisFractionDigits.equals(anotherFractionDigits)) {
               return false;
            }
         }

         return true;
      }
   }
}
