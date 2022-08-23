package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Anydata;
import org.yangcentral.yangkit.model.api.stmt.Mandatory;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.List;

public class AnyDataImpl extends DataNodeImpl implements Anydata {
   private Mandatory mandatory;

   public AnyDataImpl(String argStr) {
      super(argStr);
   }

   public Mandatory getMandatory() {
      return this.mandatory;
   }

   public void setMandatory(Mandatory mandatory) {
      this.mandatory = mandatory;
   }

   public boolean isMandatory() {
      return this.mandatory == null ? false : this.mandatory.getValue();
   }

   public boolean hasDefault() {
      return false;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.ANYDATA.getQName();
   }

   @Override
   protected void clearSelf() {
      mandatory = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.MANDATORY.getQName());

      if (matched.size() > 0) {
         this.mandatory = (Mandatory)matched.get(0);
      }

      ValidatorResult validatorResult = validatorResultBuilder.build();
      return validatorResult;
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
         statements.add(newMandatory);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
