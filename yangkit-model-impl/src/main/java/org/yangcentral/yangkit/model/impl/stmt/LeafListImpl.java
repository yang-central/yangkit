package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Default;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.MaxElements;
import org.yangcentral.yangkit.model.api.stmt.MinElements;
import org.yangcentral.yangkit.model.api.stmt.OrderedBy;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LeafListImpl extends TypedDataNodeImpl implements LeafList {
   private List<Default> defaults = new ArrayList<>();
   private MinElements minElements;
   private MaxElements maxElements;
   private OrderedBy orderedBy;

   public LeafListImpl(String argStr) {
      super(argStr);
   }

   public List<Default> getDefaults() {
      return this.defaults;
   }

   public List<Default> getEffectiveDefaults() {
      if (!this.getDefaults().isEmpty()) {
         return this.defaults;
      } else {
         List<Default> effectiveDefaults = new ArrayList<>();
         if (this.getType().isDerivedType()) {
            Default effectiveDefault = this.getType().getDerived().getEffectiveDefault();
            if (effectiveDefault != null) {
               effectiveDefaults.add(effectiveDefault);
            }
         }

         return effectiveDefaults;
      }
   }

   public void setDefaults(List<Default> defaults) {
      this.defaults = defaults;
   }

   public Default getDefault(String value) {
      Iterator<Default> defaultIterator = this.defaults.iterator();

      Default defl;
      do {
         if (!defaultIterator.hasNext()) {
            return null;
         }

         defl = defaultIterator.next();
      } while(!defl.getArgStr().equals(value));

      return defl;
   }

   public ValidatorResult addDefault(Default aDefault) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Default orig = (Default) ModelUtil.checkConflict(aDefault, this.defaults);
      if (orig != null) {
         validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(orig, aDefault));
         aDefault.setErrorStatement(true);
         return validatorResultBuilder.build();
      } else {
         this.defaults.add(aDefault);
         return validatorResultBuilder.build();
      }
   }

   public ValidatorResult updateDefault(Default aDefault) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      int idx = -1;

      for(int i = 0; i < this.defaults.size(); ++i) {
         Default def = (Default)this.defaults.get(i);
         if (def != null && def.getArgStr().equals(aDefault.getArgStr())) {
            idx = i;
            break;
         }
      }

      if (idx != -1) {
         this.defaults.set(idx, aDefault);
      }

      return validatorResultBuilder.build();
   }

   public void removeDefault(String value) {
      int idx = -1;

      for(int i = 0; i < this.defaults.size(); ++i) {
         Default def = (Default)this.defaults.get(i);
         if (def != null && def.getArgStr().equals(value)) {
            idx = i;
            break;
         }
      }

      if (idx != -1) {
         this.defaults.remove(idx);
      }

   }

   public MinElements getMinElements() {
      return this.minElements;
   }

   public void setMinElements(MinElements minElements) {
      this.minElements = minElements;
   }

   public MaxElements getMaxElements() {
      return this.maxElements;
   }

   public void setMaxElements(MaxElements maxElements) {
      this.maxElements = maxElements;
   }

   public OrderedBy getOrderedBy() {
      return this.orderedBy;
   }

   public boolean isMandatory() {
      if (null == this.minElements) {
         return false;
      } else {
         return this.minElements.getValue() > 0;
      }
   }

   public boolean hasDefault() {
      return this.getDefaults().size() > 0;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.LEAFLIST.getQName();
   }

   @Override
   protected void clearSelf() {
      this.minElements = null;
      this.maxElements = null;
      this.orderedBy = null;
      this.defaults.clear();
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.MINELEMENTS.getQName());
      if (null != matched && matched.size() > 0) {
         this.minElements = (MinElements)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.MAXELEMENTS.getQName());
      if (null != matched && matched.size() > 0) {
         this.maxElements = (MaxElements)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.ORDEREDBY.getQName());
      if (null != matched && matched.size() > 0) {
         this.orderedBy = (OrderedBy)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.DEFAULT.getQName());
      if (matched.size() > 0) {
         if (this.minElements != null) {
            YangContext minContext = this.minElements.getContext();
            if (minContext == null) {
               minContext = new YangContext(this.getContext());
               this.minElements.setContext(minContext);
            }
            validatorResultBuilder.merge(this.minElements.init());
            if (this.minElements.getValue() > 0) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.MANDATORY_HASDEFAULT.getFieldName()));
            }
         }

         if (this.maxElements != null) {
            YangContext maxContext = this.maxElements.getContext();
            if (maxContext == null) {
               maxContext = new YangContext(this.getContext());
               this.maxElements.setContext(maxContext);
            }
            validatorResultBuilder.merge(this.maxElements.init());
            if (matched.size() > this.maxElements.getValue()) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.DEFAULT_EXCEED.getFieldName()));
            }
         }

         for (YangStatement subStatement : matched) {
            this.defaults.add((Default) subStatement);
         }
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.validateSelf());
      List<Default> effectiveDefaults = this.getEffectiveDefaults();

      for (Default deflt : effectiveDefaults) {
         validatorResultBuilder.merge(this.validateDefault(deflt));
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList<>();
      if (this.defaults.size() > 0) {
         statements.addAll(this.defaults);
      } else if (this.getType().isDerivedType()) {
         Default typedefDefault = this.getType().getDerived().getEffectiveDefault();
         if (typedefDefault != null) {
            statements.add(typedefDefault);
         }
      }

      if (this.minElements != null) {
         statements.add(this.minElements);
      } else {
         MinElements newMinElements = new MinElementsImpl("0");
         newMinElements.setContext(new YangContext(this.getContext()));
         newMinElements.setElementPosition(this.getElementPosition());
         newMinElements.setParentStatement(this);
         newMinElements.init();
         newMinElements.build();
         statements.add(newMinElements);
      }

      if (this.maxElements != null) {
         statements.add(this.maxElements);
      } else {
         MaxElements newMaxElements = new MaxElementsImpl("unbounded");
         newMaxElements.setContext(new YangContext(this.getContext()));
         newMaxElements.setElementPosition(this.getElementPosition());
         newMaxElements.setParentStatement(this);
         newMaxElements.init();
         newMaxElements.build();
         statements.add(newMaxElements);
      }

      if (this.orderedBy != null) {
         statements.add(this.orderedBy);
      } else {
         OrderedBy newOrderedBy = new OrderedByImpl("system");
         newOrderedBy.setContext(new YangContext(this.getContext()));
         newOrderedBy.setElementPosition(this.getElementPosition());
         newOrderedBy.setParentStatement(this);
         newOrderedBy.init();
         newOrderedBy.build();
         statements.add(newOrderedBy);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
