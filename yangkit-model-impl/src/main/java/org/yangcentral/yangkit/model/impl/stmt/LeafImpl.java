package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Default;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.Mandatory;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.List;

public class LeafImpl extends TypedDataNodeImpl implements Leaf {
   private Mandatory mandatory;
   private Default aDefault;
   private boolean key;

   public LeafImpl(String argStr) {
      super(argStr);
   }

   public boolean isMandatory() {
      return this.mandatory != null ? this.mandatory.getValue() : false;
   }

   public boolean hasDefault() {
      return this.aDefault != null;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.LEAF.getQName();
   }

   public Default getDefault() {
      return this.aDefault;
   }

   public Default getEffectiveDefault() {
      if (this.aDefault != null) {
         return this.aDefault;
      } else {
         return this.getType().isDerivedType() ? this.getType().getDerived().getEffectiveDefault() : null;
      }
   }

   public void setDefault(Default aDefault) {
      this.aDefault = aDefault;
   }

   public boolean isKey() {
      return this.key;
   }

   public void setKey(boolean key) {
      this.key = key;
   }

   public Mandatory getMandatory() {
      return this.mandatory;
   }

   public void setMandatory(Mandatory mandatory) {
      this.mandatory = mandatory;
   }

   @Override
   protected void clear() {
      this.mandatory = null;
      this.aDefault = null;
      super.clear();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.MANDATORY.getQName());
      if (matched.size() != 0) {
         this.mandatory = (Mandatory)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.DEFAULT.getQName());
      if (matched.size() != 0) {
         this.aDefault = (Default)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.validateSelf());
      Default effectiveDefault = this.getEffectiveDefault();
      if (effectiveDefault != null) {
         validatorResultBuilder.merge(this.validateDefault(effectiveDefault));
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.mandatory != null) {
         statements.add(this.mandatory);
      } else {
         Mandatory newMandatory = new MandatoryImpl("false");
         newMandatory.setContext(new YangContext(this.getContext()));
         newMandatory.setElementPosition(this.getElementPosition());
         newMandatory.setParentStatement(this);
         newMandatory.init();
         newMandatory.build();
         statements.add(newMandatory);
      }

      if (this.aDefault != null) {
         statements.add(this.aDefault);
      } else if (this.getType().isDerivedType()) {
         Default typedefDefault = this.getType().getDerived().getEffectiveDefault();
         if (typedefDefault != null) {
            statements.add(typedefDefault);
         }
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
