package org.yangcentral.yangkit.model.impl.stmt.type;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.restriction.Section;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.type.SectionExpression;
import org.yangcentral.yangkit.model.impl.stmt.YangBuiltInStatementImpl;
import org.yangcentral.yangkit.util.ModelUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

abstract class SectionExpressionImpl extends YangBuiltInStatementImpl implements SectionExpression {
   private ErrorMessageStmt errorMessage;
   private ErrorAppTagStmt errorAppTag;
   private Description description;
   private Reference reference;
   private Comparable highBound;
   private Comparable lowBound;
   private List<Section> sections = new ArrayList();

   public SectionExpressionImpl(String argStr) {
      super(argStr);
   }

   public ErrorAppTagStmt getErrorAppTag() {
      return this.errorAppTag;
   }

   public ErrorMessageStmt getErrorMessage() {
      return this.errorMessage;
   }

   public Description getDescription() {
      return this.description;
   }

   public void setDescription(Description description) {
      this.description = description;
   }

   public Reference getReference() {
      return this.reference;
   }

   public void setReference(Reference reference) {
      this.reference = reference;
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      this.description = null;
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.DESCRIPTION.getQName());
      if (matched.size() != 0) {
         this.description = (Description)matched.get(0);
      }
      this.reference = null;
      matched = this.getSubStatement(YangBuiltinKeyword.REFERENCE.getQName());
      if (matched.size() != 0) {
         this.reference = (Reference)matched.get(0);
      }
      this.errorMessage = null;
      matched = this.getSubStatement(YangBuiltinKeyword.ERRORMESSAGE.getQName());
      if (matched.size() != 0) {
         this.errorMessage = (ErrorMessageStmt)matched.get(0);
      }
      this.errorAppTag = null;
      matched = this.getSubStatement(YangBuiltinKeyword.ERRORAPPTAG.getQName());
      if (matched.size() != 0) {
         this.errorAppTag = (ErrorAppTagStmt)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   private boolean checkSections() {
      Section lastSection = null;
      Iterator iterator = this.sections.iterator();

      while(iterator.hasNext()) {
         Section section = (Section)iterator.next();
         if (lastSection == null) {
            lastSection = section;
         } else if (section.getMin().compareTo(lastSection.getMax()) <= 0) {
            return false;
         }
      }

      return true;
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      switch (phase) {
         case GRAMMAR:
            try {
               List<Section> sections = this.parseRange(this.getArgStr());
               this.sections = sections;
               if (!this.checkSections()) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                          ErrorCode.SECTIONS_MUST_ASCEND_ORDER.getFieldName()));
                  return validatorResultBuilder.build();
               }
            } catch (RuntimeException e) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.INVALID_ARG.getFieldName()));
               return validatorResultBuilder.build();
            }
         default:
            return validatorResultBuilder.build();
      }
   }

   private Comparable parseValue(String val) {
      if (val.equals("max")) {
         return this.highBound;
      } else if (val.equals("min")) {
         return this.lowBound;
      } else if (this.highBound instanceof Byte) {
         return Byte.valueOf(val);
      } else if (this.highBound instanceof Short) {
         return Short.valueOf(val);
      } else if (this.highBound instanceof Integer) {
         return Integer.valueOf(val);
      } else if (this.highBound instanceof Long) {
         return Long.valueOf(val);
      } else if (this.highBound instanceof BigInteger) {
         return new BigInteger(val);
      } else if (this.highBound instanceof BigDecimal) {
         return new BigDecimal(val);
      } else {
         throw new IllegalArgumentException("un-support type.");
      }
   }

   public List<Section> getSections() {
      return Collections.unmodifiableList(this.sections);
   }

   public void setBound(Comparable highBound, Comparable lowBound) {
      this.highBound = highBound;
      this.lowBound = lowBound;
   }

   private Section parseSectionExpression(String expression) {
      if (null == expression) {
         return null;
      } else if (0 == expression.length()) {
         return null;
      } else if (!expression.contains("..")) {
         Comparable val = this.parseValue(expression.trim());
         if (val.compareTo(this.highBound) <= 0 && val.compareTo(this.lowBound) >= 0) {
            return new Section(val, val);
         } else {
            throw new IllegalArgumentException(ErrorCode.RANGE_OR_LENGTH_NOT_SUBSET_OF_DERIVED.getFieldName());
         }
      } else {
         String[] strs = expression.split("\\.\\.");
         if (2 != strs.length) {
            return null;
         } else {
            Comparable min = this.parseValue(strs[0].trim());
            Comparable max = this.parseValue(strs[1].trim());
            if (min.compareTo(this.lowBound) >= 0 && max.compareTo(this.highBound) <= 0) {
               return new Section(max, min);
            } else {
               throw new IllegalArgumentException(ErrorCode.RANGE_OR_LENGTH_NOT_SUBSET_OF_DERIVED.getFieldName());
            }
         }
      }
   }

   private List<Section> parseRange(String expression) {
      List<Section> sections = new ArrayList();
      if (null == expression) {
         return sections;
      } else if (0 == expression.length()) {
         return sections;
      } else if (!expression.contains("|")) {
         Section section = this.parseSectionExpression(expression);
         sections.add(section);
         return sections;
      } else {
         String[] strs = expression.split("\\|");
         int size = strs.length;

         for(int i = 0; i < size; ++i) {
            if (null != strs[i]) {
               Section section = this.parseSectionExpression(strs[i].trim());
               int lastIndex = sections.size() - 1;
               if (lastIndex >= 0 && ((Section)sections.get(lastIndex)).getMax().compareTo(section.getMin()) >= 0) {
                  throw new IllegalArgumentException("the range argument is not ascend order.");
               }

               sections.add(section);
            }
         }

         return sections;
      }
   }

   private boolean match(Section section, List<Section> derivedSections) {
      Iterator sectionIterator = derivedSections.iterator();

      Section derivedSection;
      do {
         if (!sectionIterator.hasNext()) {
            return false;
         }

         derivedSection = (Section)sectionIterator.next();
      } while(!section.isSubSection(derivedSection));

      return true;
   }

   private boolean match(List<Section> sections, List<Section> derivedSections) {
      Iterator sectionIterator = sections.iterator();

      Section section;
      do {
         if (!sectionIterator.hasNext()) {
            return true;
         }

         section = (Section)sectionIterator.next();
      } while(this.match(section, derivedSections));

      return false;
   }

   public boolean isSubSet(SectionExpression sectionExpression) {
      if (sectionExpression == null) {
         return false;
      } else {
         return this.match(this.getSections(), sectionExpression.getSections());
      }
   }

   public boolean evaluate(Comparable val) {
      Iterator sectionIterator = this.sections.iterator();

      Section section;
      do {
         if (!sectionIterator.hasNext()) {
            return false;
         }

         section = (Section)sectionIterator.next();
      } while(!section.evaluate(val));

      return true;
   }

   public Comparable<?> getMax() {
      int size = this.sections.size();
      return (this.sections.get(size - 1)).getMax();
   }

   public Comparable<?> getMin() {
      return ((Section)this.sections.get(0)).getMin();
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof SectionExpression)) {
         return false;
      } else {
         SectionExpression another = (SectionExpression)obj;
         List<Section> anotherSections = another.getSections();
         if (this.sections.size() != anotherSections.size()) {
            return false;
         } else {
            for(int i = 0; i < this.sections.size(); ++i) {
               Section thisSection = this.sections.get(i);
               Section anotherSection = anotherSections.get(i);
               if (!thisSection.equals(anotherSection)) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}
