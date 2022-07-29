package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.restriction.Section;
import org.yangcentral.yangkit.model.api.restriction.YangString;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.type.Length;
import org.yangcentral.yangkit.model.api.stmt.type.Pattern;
import org.yangcentral.yangkit.model.impl.stmt.type.LengthImpl;
import org.yangcentral.yangkit.util.ModelUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YangStringImpl extends RestrictionImpl<String> implements YangString {
   private List<Pattern> patterns = new ArrayList();
   private Length length;

   public YangStringImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public YangStringImpl(YangContext context) {
      super(context);
   }

   public boolean evaluated(String value) {
      if (this.patterns.size() > 0) {
         Iterator patternIterator = this.patterns.iterator();

         while(patternIterator.hasNext()) {
            Pattern pattern = (Pattern)patternIterator.next();
            if (pattern.getModifier() != null) {
               if (!pattern.getPattern().matcher(value).matches()) {
                  return true;
               }
            } else if (pattern.getPattern().matcher(value).matches()) {
               return true;
            }
         }

         return false;
      } else if (this.getLength() != null) {
         return this.length.evaluate(BigInteger.valueOf((long) value.length()));
      } else if (this.getDerived() != null) {
         return this.getDerived().getType().getRestriction().evaluated(value);
      } else {
         Section section = new Section(this.getHighBound(), this.getLowBound());
         return section.evaluate(BigInteger.valueOf((long) value.length()));
      }
   }

   public Length getLength() {
      return this.length;
   }

   public Length getEffectiveLength() {
      if (this.length != null) {
         return this.length;
      } else if (this.getDerived() != null) {
         return ((YangString)this.getDerived().getType().getRestriction()).getEffectiveLength();
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

   public ValidatorResult setLength(Length length) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if(length == null){
         this.length = null;
         return validatorResultBuilder.build();
      }
      if (!length.isBuilt()) {
         length.setBound(this.getHighBound(), this.getLowBound());
         validatorResultBuilder.merge(length.build(BuildPhase.GRAMMAR));
      }

      if (this.getDerived() != null) {
         Length derivedLength = ((YangString)this.getDerived().getType().getRestriction()).getEffectiveLength();
         if (derivedLength != null && !length.isSubSet(derivedLength)) {
            length.isSubSet(derivedLength);
            validatorResultBuilder.addRecord(ModelUtil.reportError(length,ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity(),
                    ErrorTag.BAD_ELEMENT,ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getFieldName()));
            if (ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity() == Severity.ERROR) {
               return validatorResultBuilder.build();
            }
         }
      }

      this.length = length;
      return validatorResultBuilder.build();
   }

   public boolean addPattern(Pattern pattern) {
      Iterator patternIterator = this.patterns.iterator();

      Pattern o;
      do {
         if (!patternIterator.hasNext()) {
            return this.patterns.add(pattern);
         }

         o = (Pattern)patternIterator.next();
      } while(!o.equals(pattern));

      return false;
   }

   public List<Pattern> getPatterns() {
      return this.patterns;
   }

   private BigInteger getHighBound() {
      return this.getDerived() != null ? ((YangString)this.getDerived().getType().getRestriction()).getMaxLength() : YangString.MAX_LENGTH;
   }

   private BigInteger getLowBound() {
      return this.getDerived() != null ? ((YangString)this.getDerived().getType().getRestriction()).getMinLength() : YangString.MIN_LENGTH;
   }

   public BigInteger getMaxLength() {
      return this.length != null ? (BigInteger)this.getLength().getMax() : this.getHighBound();
   }

   public BigInteger getMinLength() {
      return this.length != null ? (BigInteger)this.getLength().getMin() : this.getLowBound();
   }

   public List<Pattern> getEffectivePatterns() {
      List<Pattern> effectivePatterns = new ArrayList();
      if (this.patterns.size() > 0) {
         effectivePatterns.addAll(this.patterns);
      }

      if (this.getDerived() != null) {
         YangStringImpl derived = (YangStringImpl)this.getDerived().getType().getRestriction();
         effectivePatterns.addAll(derived.getEffectivePatterns());
      }

      return effectivePatterns;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof YangString)) {
         return false;
      } else {
         YangStringImpl another = (YangStringImpl)obj;
         if (this.getEffectiveLength() != null || another.getEffectiveLength() != null) {
            if (this.getEffectiveLength() == null || another.getEffectiveLength() == null) {
               return false;
            }

            if (!this.getEffectiveLength().equals(another.getEffectiveLength())) {
               return false;
            }
         }

         List<Pattern> thisPatterns = this.getEffectivePatterns();
         List<Pattern> anotherPatterns = another.getEffectivePatterns();
         if (thisPatterns.size() != anotherPatterns.size()) {
            return false;
         } else {
            Iterator var5 = thisPatterns.iterator();

            Pattern theSame;
            do {
               if (!var5.hasNext()) {
                  return true;
               }

               Pattern thisPattern = (Pattern)var5.next();
               theSame = null;
               Iterator var8 = anotherPatterns.iterator();

               while(var8.hasNext()) {
                  Pattern anotherPattern = (Pattern)var8.next();
                  if (thisPattern.equals(anotherPattern)) {
                     theSame = anotherPattern;
                     break;
                  }
               }
            } while(theSame != null);

            return false;
         }
      }
   }
}
