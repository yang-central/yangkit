package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
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

   @Override
   protected void clearSelf() {
      this.description = null;
      this.reference = null;
      this.deviates.clear();
      this.targetPath = null;
      this.target = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      if (!ModelUtil.isAbsoluteSchemaNodeIdentifier(this.getArgStr())) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_ARG.getFieldName()));
         return validatorResultBuilder.build();
      }

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
         Iterator iterator;
         YangStatement subStatement;
         if (matched.size() > 1) {
            iterator = matched.iterator();

            while(iterator.hasNext()) {
               subStatement = (YangStatement)iterator.next();
               Deviate deviate = (Deviate)subStatement;
               if (DeviateType.forValue(deviate.getArgStr()) == DeviateType.NOT_SUPPORTED) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(deviate,
                          ErrorCode.DEVIATE_NOT_SUPPORT_ONLY_ONE.getFieldName()));
                  return validatorResultBuilder.build();
               }
            }
         }

         iterator = matched.iterator();

         while(iterator.hasNext()) {
            subStatement = (YangStatement)iterator.next();
            this.deviates.add((Deviate)subStatement);
         }
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      ValidatorRecordBuilder validatorRecordBuilder;
      switch (phase) {
         case GRAMMAR:{

            try {
               SchemaPath targetPath = SchemaPathImpl.from(this.getContext().getCurModule(), null, this,this.getArgStr());
               if (targetPath instanceof SchemaPath.Descendant) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                          ErrorCode.INVALID_SCHEMAPATH.getFieldName()));
                  return validatorResultBuilder.build();
               }

               this.setTargetPath(targetPath);
               break;
            } catch (ModelException e) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       e.getSeverity(),ErrorTag.BAD_ELEMENT,e.getDescription()));
               break;
            }
         }

         case SCHEMA_MODIFIER:{

            SchemaNode targetNode = this.targetPath.getSchemaNode(this.getContext().getSchemaContext());
            if (targetNode == null) {
               this.targetPath.getSchemaNode(this.getContext().getSchemaContext());
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.MISSING_TARGET.getFieldName()));
               return validatorResultBuilder.build();
            }

            this.setTarget(targetNode);
            Iterator deviateIterator = this.deviates.iterator();

            while(deviateIterator.hasNext()) {
               Deviate deviate = (Deviate)deviateIterator.next();
               deviate.setTarget(targetNode);
            }
            break;
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
