package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
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
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.stmt.Description;
import org.yangcentral.yangkit.model.api.stmt.Deviate;
import org.yangcentral.yangkit.model.api.stmt.DeviateType;
import org.yangcentral.yangkit.model.api.stmt.Deviation;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.Reference;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.impl.schema.SchemaPathImpl;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DeviationImpl extends YangBuiltInStatementImpl implements Deviation {
   private Description description;
   private Reference reference;
   private SchemaNode target;
   private SchemaPath targetPath;
   private List<Deviate> deviates = new ArrayList();

   public DeviationImpl(String argStr) {
      super(argStr);
   }

   public SchemaNode getTarget() {
      return this.target;
   }

   public void setTarget(SchemaNode target) {
      this.target = target;
   }

   public SchemaPath getTargetPath() {
      return this.targetPath;
   }

   public void setTargetPath(SchemaPath schemaPath) {
      this.targetPath = schemaPath;
   }

   public List<Deviate> getDeviates() {
      return Collections.unmodifiableList(this.deviates);
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

   public QName getYangKeyword() {
      return YangBuiltinKeyword.DEVIATION.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      if (!ModelUtil.isAbsoluteSchemaNodeIdentifier(this.getArgStr())) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_ARG.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.DESCRIPTION.getQName());
         if (matched.size() != 0) {
            this.description = (Description)matched.get(0);
         }

         matched = this.getSubStatement(YangBuiltinKeyword.REFERENCE.getQName());
         if (matched.size() != 0) {
            this.reference = (Reference)matched.get(0);
         }

         matched = this.getSubStatement(YangBuiltinKeyword.DEVIATE.getQName());
         if (matched.size() != 0) {
            Iterator var3;
            YangStatement subStatement;
            if (matched.size() > 1) {
               var3 = matched.iterator();

               while(var3.hasNext()) {
                  subStatement = (YangStatement)var3.next();
                  Deviate deviate = (Deviate)subStatement;
                  if (DeviateType.forValue(deviate.getArgStr()) == DeviateType.NOT_SUPPORTED) {
                     ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setBadElement(deviate);
                     validatorRecordBuilder.setErrorPath(deviate.getElementPosition());
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DEVIATE_NOT_SUPPORT_ONLY_ONE.getFieldName()));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     return validatorResultBuilder.build();
                  }
               }
            }

            var3 = matched.iterator();

            while(var3.hasNext()) {
               subStatement = (YangStatement)var3.next();
               this.deviates.add((Deviate)subStatement);
            }
         }

         return validatorResultBuilder.build();
      }
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      ValidatorRecordBuilder validatorRecordBuilder;
      switch (phase) {
         case GRAMMAR:
            try {
               SchemaPath targetPath = SchemaPathImpl.from(this.getContext().getCurModule(), (SchemaNodeContainer)null, this.getArgStr());
               if (targetPath instanceof SchemaPath.Descendant) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setBadElement(this);
                  validatorRecordBuilder.setErrorPath(this.getElementPosition());
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SCHEMAPATH.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  return validatorResultBuilder.build();
               }

               this.setTargetPath(targetPath);
               break;
            } catch (ModelException var6) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(var6.getSeverity());
               validatorRecordBuilder.setBadElement(var6.getElement());
               validatorRecordBuilder.setErrorPath(var6.getElement().getElementPosition());
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(var6.getDescription()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               return validatorResultBuilder.build();
            }
         case SCHEMA_MODIFIER:
            SchemaNode targetNode = this.targetPath.getSchemaNode(this.getContext().getSchemaContext());
            if (targetNode == null) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.getElementPosition());
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_TARGET.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               return validatorResultBuilder.build();
            }

            this.setTarget(targetNode);
            Iterator var4 = this.deviates.iterator();

            while(var4.hasNext()) {
               Deviate deviate = (Deviate)var4.next();
               deviate.setTarget(targetNode);
            }
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.description != null) {
         statements.add(this.description);
      }

      if (this.reference != null) {
         statements.add(this.reference);
      }

      statements.addAll(this.deviates);
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
