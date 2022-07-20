package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.restriction.*;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.type.Range;
import org.yangcentral.yangkit.model.impl.stmt.type.RangeImpl;
import org.yangcentral.yangkit.util.ModelUtil;

public abstract class YangIntegerImpl<T extends Comparable> extends RestrictionImpl<T> implements YangInteger<T> {
   private Range range;

   public YangIntegerImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public YangIntegerImpl(YangContext context) {
      super(context);
   }

   public boolean evaluated(T value) {
      if (this.getRange() != null) {
         return this.getRange().evaluate(value);
      } else if (this.getDerived() == null) {
         Section section = new Section(this.getHighBound(), this.getLowBound());
         return section.evaluate(value);
      } else {
         return this.getDerived().getType().getRestriction().evaluated(value);
      }
   }

   public Range getRange() {
      return this.range;
   }

   public Range getEffectiveRange() {
      if (this.range != null) {
         return this.range;
      } else if (this.getDerived() != null) {
         return ((YangInteger)this.getDerived().getType().getRestriction()).getEffectiveRange();
      } else {
         Range newRange = new RangeImpl(this.getLowBound() + ".." + this.getHighBound());
         newRange.setContext(new YangContext(this.getContext()));
         newRange.setElementPosition(this.getContext().getSelf().getElementPosition());
         newRange.setParentStatement(this.getContext().getSelf());
         newRange.init();
         newRange.build();
         return newRange;
      }
   }

   public ValidatorResult setRange(Range range) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!range.isBuilt()) {
         range.setBound(this.getHighBound(), this.getLowBound());
         validatorResultBuilder.merge(range.build(BuildPhase.GRAMMAR));
      }

      if (this.getDerived() != null) {
         Range derivedRange = ((YangInteger)this.getDerived().getType().getRestriction()).getRange();
         if (derivedRange != null && !range.isSubSet(derivedRange)) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(range,ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity(),
                    ErrorTag.BAD_ELEMENT,ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getFieldName()));
            if (ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity() == Severity.DEBUG) {
               return validatorResultBuilder.build();
            }
         }
      }

      this.range = range;
      return validatorResultBuilder.build();
   }

   private T getHighBound() {
      if (this.getDerived() != null) {
         return ((YangInteger<T>)this.getDerived().getType().getRestriction()).getMax();
      } else if (this instanceof Int8) {
         return (T)Int8.MAX;
      } else if (this instanceof Int16) {
         return (T)Int16.MAX;
      } else if (this instanceof Int32) {
         return (T)Int32.MAX;
      } else if (this instanceof Int64) {
         return (T)Int64.MAX;
      } else if (this instanceof UInt8) {
         return (T)UInt8.MAX;
      } else if (this instanceof UInt16) {
         return (T)UInt16.MAX;
      } else {
         return (T)(this instanceof UInt32 ? UInt32.MAX : UInt64.MAX);
      }
   }

   public T getMax() {
      return this.getRange() != null ? (T)this.getRange().getMax() : this.getHighBound();
   }

   private T getLowBound() {
      if (this.getDerived() != null) {
         return ((YangInteger<T>)this.getDerived().getType().getRestriction()).getMin();
      } else if (this instanceof Int8) {
         return (T)Int8.MIN;
      } else if (this instanceof Int16) {
         return (T)Int16.MIN;
      } else if (this instanceof Int32) {
         return (T)Int32.MIN;
      } else if (this instanceof Int64) {
         return (T)Int64.MIN;
      } else if (this instanceof UInt8) {
         return (T)UInt8.MIN;
      } else if (this instanceof UInt16) {
         return (T)UInt16.MIN;
      } else {
         return (T)(this instanceof UInt32 ? UInt32.MIN : UInt64.MIN);
      }
   }

   public T getMin() {
      return this.getRange() != null ? (T)this.getRange().getMin() : this.getLowBound();
   }

   public static YangIntegerImpl getInstance(BuiltinType builtinType, YangContext context, Typedef derived) {
      switch (builtinType) {
         case INT8:
            return new Int8Impl(context, derived);
         case INT16:
            return new Int16Impl(context, derived);
         case INT32:
            return new Int32Impl(context, derived);
         case INT64:
            return new Int64Impl(context, derived);
         case UINT8:
            return new UInt8Impl(context, derived);
         case UINT16:
            return new Uint16Impl(context, derived);
         case UINT32:
            return new UInt32Impl(context, derived);
         case UINT64:
            return new UInt64Impl(context, derived);
         default:
            throw new IllegalArgumentException("unrecognized type.");
      }
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof YangInteger)) {
         return false;
      } else {
         YangIntegerImpl another = (YangIntegerImpl)obj;
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

         return true;
      }
   }
}
