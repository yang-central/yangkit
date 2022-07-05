package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.restriction.BuiltinType;
import org.yangcentral.yangkit.model.api.stmt.Default;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.Units;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.Iterator;
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

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      if (BuiltinType.isBuiltinType(this.getArgStr())) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_TYPEDEF_NAME.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.TYPE.getQName());
         if (matched.size() != 0) {
            this.type = (Type)matched.get(0);
         }

         matched = this.getSubStatement(YangBuiltinKeyword.UNITS.getQName());
         if (matched.size() != 0) {
            this.units = (Units)matched.get(0);
         }

         matched = this.getSubStatement(YangBuiltinKeyword.DEFAULT.getQName());
         if (matched.size() != 0) {
            this.aDefault = (Default)matched.get(0);
         }

         return validatorResultBuilder.build();
      }
   }

   public List<YangStatement> getReferencedBy() {
      return this.referencedBys;
   }

   public void addReference(YangStatement schemaNode) {
      this.referencedBys.add(schemaNode);
   }

   public void delReference(YangStatement schemaNode) {
      int pos = -1;

      for(int i = 0; i < this.referencedBys.size(); ++i) {
         if (this.referencedBys.get(i) == schemaNode) {
            pos = i;
            break;
         }
      }

      if (pos != -1) {
         this.referencedBys.remove(pos);
      }

   }

   public boolean isReferencedBy(YangStatement yangStatement) {
      Iterator var2 = this.referencedBys.iterator();

      YangStatement statement;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         statement = (YangStatement)var2.next();
      } while(statement != yangStatement);

      return true;
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
