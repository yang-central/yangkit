package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilderFactory;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.restriction.Union;
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
import org.yangcentral.yangkit.util.ModelUtil;
import org.yangcentral.yangkit.xpath.impl.YangLocationPathImpl;
import org.yangcentral.yangkit.xpath.impl.YangXPathContext;
import org.yangcentral.yangkit.xpath.impl.YangXPathValidator;
import org.yangcentral.yangkit.xpath.YangXPath;
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
         validatorResultBuilder.addRecord(ModelUtil.reportError(deflt,
                 ErrorCode.LEAFREF_SHOULD_NO_DEFAULT.getSeverity(),
                 ErrorTag.BAD_ELEMENT,
                 ErrorCode.LEAFREF_SHOULD_NO_DEFAULT.getFieldName()));
      }

      try {
         Object value = StringValueCodecFactory.getInstance().getStringValueCodec(this)
                 .deserialize(restriction, deflt.getArgStr());
         deflt.setValue(value);
      } catch (Exception e) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(deflt,
                 ErrorCode.INVALID_DEFAULTVALUE.getFieldName()));
      }

      return validatorResultBuilder.build();
   }

   @Override
   protected void clearSelf() {
      this.type = null;
      this.units = null;
      super.clearSelf();
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
      validatorResultBuilder.merge(this.validateLeafRefReferences(this.getType()));

      return validatorResultBuilder.build();
   }

   private ValidatorResult validateLeafRefReferences(Type type) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (type == null || type.getRestriction() == null) {
         return validatorResultBuilder.build();
      }

      Restriction<?> restriction = type.getRestriction();
      if (restriction instanceof LeafRef) {
         validatorResultBuilder.merge(this.validateLeafRefReference((LeafRef) restriction));
      } else if (restriction instanceof Union) {
         Union union = (Union) restriction;
         for (Type actualType : union.getActualTypes()) {
            validatorResultBuilder.merge(this.validateLeafRefReferences(actualType));
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult validateLeafRefReference(LeafRef leafRef) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Path effectivePath = leafRef.getEffectivePath();
      if (effectivePath == null) {
         return validatorResultBuilder.build();
      }

      YangXPath xpath = effectivePath.getXPathExpression();
      if (xpath == null) {
         return validatorResultBuilder.build();
      }

      YangLocationPathImpl path = (YangLocationPathImpl) xpath.getRootExpr();
      YangXPathContext yangXPathContext = new YangXPathContext(effectivePath.getContext(), this, this);
      xpath.setXPathContext(yangXPathContext);
      YangXPathValidator yangXPathValidator = new YangXPathValidator(xpath, yangXPathContext, new ValidatorResultBuilderFactory(), 2);
      ValidatorResult xpathResult = yangXPathValidator.visit(path, this);
      validatorResultBuilder.merge(xpathResult);
      if (!xpathResult.isOk()) {
         return validatorResultBuilder.build();
      }

      try {
         SchemaNode referencedNode = path.getTargetSchemaNode(yangXPathContext);
         if (!(referencedNode instanceof TypedDataNode)) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.WRONG_PATH.getFieldName()));
            return validatorResultBuilder.build();
         }

         leafRef.setReferencedNode((TypedDataNode) referencedNode);
      } catch (ModelException e) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(e.getElement(),
                 e.getSeverity(), ErrorTag.BAD_ELEMENT, e.getDescription()));
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList<>();
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
