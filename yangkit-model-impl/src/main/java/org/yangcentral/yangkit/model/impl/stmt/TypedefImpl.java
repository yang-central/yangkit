package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.restriction.BuiltinType;
import org.yangcentral.yangkit.model.api.stmt.Default;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.Units;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.List;

public class TypedefImpl extends EntityImpl implements Typedef {
   private Type type;
   private Units units;
   private Default aDefault;
   private List<YangStatement> referencedBys = new ArrayList();

   public TypedefImpl(String argStr) {
      super(argStr);
   }

   public Type getType() {
      return this.type;
   }

   public Units getUnits() {
      return this.units;
   }

   public Default getDefault() {
      return this.aDefault;
   }

   public Default getEffectiveDefault() {
      if (this.aDefault != null) {
         return this.aDefault;
      } else {
         return this.type.isDerivedType() ? this.type.getDerived().getEffectiveDefault() : null;
      }
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.TYPEDEF.getQName();
   }

   @Override
   protected void clearSelf() {
      this.type = null;
      this.units = null;
      this.aDefault = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      if (BuiltinType.isBuiltinType(this.getArgStr())) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_TYPEDEF_NAME.getFieldName()));
         return validatorResultBuilder.build();
      }

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.TYPE.getQName());
      if (matched.size() != 0) {
         this.type = (Type) matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.UNITS.getQName());
      if (matched.size() != 0) {
         this.units = (Units) matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.DEFAULT.getQName());
      if (matched.size() != 0) {
         this.aDefault = (Default) matched.get(0);
      }

      return validatorResultBuilder.build();
   }


   public List<YangStatement> getReferencedBy() {
      return this.referencedBys;
   }


   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.type != null) {
         statements.add(this.type);
      }

      if (this.units != null) {
         statements.add(this.units);
      }

      if (this.aDefault != null) {
         statements.add(this.aDefault);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
