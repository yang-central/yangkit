package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilderFactory;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.stmt.Default;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.Units;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.type.Path;
import org.yangcentral.yangkit.model.impl.codec.StringValueCodecFactory;
import org.yangcentral.yangkit.xpath.impl.YangLocationPathImpl;
import org.yangcentral.yangkit.xpath.impl.YangXPathContext;
import org.yangcentral.yangkit.xpath.impl.YangXPathValidator;
import java.util.ArrayList;
import java.util.List;

public abstract class TypedDataNodeImpl extends DataNodeImpl implements TypedDataNode {
   private Type type;
   private Units units;

   public TypedDataNodeImpl(String argStr) {
      super(argStr);
   }

   public Type getType() {
      return this.type;
   }

   public ValidatorResult setType(Type type) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      this.type = type;
      return validatorResultBuilder.build();
   }

   public Units getUnits() {
      return this.units;
   }

   public void setUnits(Units units) {
      this.units = units;
   }

   protected ValidatorResult validateDefault(Default deflt) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Restriction restriction = this.getType().getRestriction();
      if (restriction instanceof LeafRef) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.WARNING);
         validatorRecordBuilder.setErrorPath(deflt.getElementPosition());
         validatorRecordBuilder.setBadElement(deflt);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.LEAFREF_SHOULD_NO_DEFAULT.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      try {
         Object value = StringValueCodecFactory.getInstance().getStringValueCodec((TypedDataNode)this).deserialize(restriction, deflt.getArgStr());
         deflt.setValue(value);
      } catch (Exception var6) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(deflt.getElementPosition());
         validatorRecordBuilder.setBadElement(deflt);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_DEFAULTVALUE.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.TYPE.getQName());
      if (matched.size() != 0) {
         this.type = (Type)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.UNITS.getQName());
      if (matched.size() != 0) {
         this.units = (Units)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.validateSelf());
      if (this.getType().getRestriction() instanceof LeafRef) {
         LeafRef leafRef = (LeafRef)this.getType().getRestriction();
         Path effectivePath = leafRef.getEffectivePath();
         YangLocationPathImpl path = (YangLocationPathImpl)effectivePath.getXPathExpression().getRootExpr();
         YangXPathContext yangXPathContext = new YangXPathContext(effectivePath.getContext(), this, this);
         effectivePath.getXPathExpression().setXPathContext(yangXPathContext);
         YangXPathValidator yangXPathValidator = new YangXPathValidator(effectivePath.getXPathExpression(), yangXPathContext, new ValidatorResultBuilderFactory(), 2);
         ValidatorResult xpathResult = (ValidatorResult)yangXPathValidator.visit(path, this);
         validatorResultBuilder.merge(xpathResult);
         if (!xpathResult.isOk()) {
            return validatorResultBuilder.build();
         }

         SchemaNode referencedNode = null;

         try {
            referencedNode = path.getTargetSchemaNode(yangXPathContext);
            if (null == referencedNode || !(referencedNode instanceof TypedDataNode)) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.getElementPosition());
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.WRONG_PATH.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               return validatorResultBuilder.build();
            }

            leafRef.setReferencedNode((TypedDataNode)referencedNode);
         } catch (ModelException var11) {
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setSeverity(var11.getSeverity());
            validatorRecordBuilder.setErrorPath(var11.getElement().getElementPosition());
            validatorRecordBuilder.setBadElement(var11.getElement());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(var11.getDescription()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         }
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.type != null) {
         statements.add(this.type);
      }

      if (this.units != null) {
         statements.add(this.units);
      } else if (this.type.isDerivedType()) {
         Typedef typedef = this.type.getDerived();
         if (typedef.getUnits() != null) {
            statements.add(typedef.getUnits());
         }
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
