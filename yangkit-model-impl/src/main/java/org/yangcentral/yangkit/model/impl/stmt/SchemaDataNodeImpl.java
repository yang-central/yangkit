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
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.stmt.Config;
import org.yangcentral.yangkit.model.api.stmt.SchemaDataNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.List;

public abstract class SchemaDataNodeImpl extends DataDefinitionImpl implements SchemaDataNode {
   private Config config;

   public SchemaDataNodeImpl(String argStr) {
      super(argStr);
   }

   public Config getConfig() {
      return this.config;
   }

   public void setConfig(Config config) {
      this.config = config;
   }

   public boolean isConfig() {
      if (this.getSchemaTreeType() != SchemaTreeType.DATATREE) {
         return false;
      } else if (this.config != null) {
         return this.config.isConfig();
      } else {
         SchemaNodeContainer parent = this.getParentSchemaNode();
         return parent instanceof SchemaNode ? ((SchemaNode)parent).isConfig() : true;
      }
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.CONFIG.getQName());
      if (matched.size() != 0) {
         this.config = (Config)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.validateSelf());
      if (this.config != null && this.config.isConfig() && this.getSchemaTreeType() == SchemaTreeType.DATATREE) {
         SchemaNodeContainer parent = this.getParentSchemaNode();
         if (parent instanceof SchemaNode) {
            SchemaNode parSchemaNode = this.getRealSchemaNode((SchemaNode)parent);
            if (parSchemaNode != null && !parSchemaNode.isConfig()) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.config.getElementPosition());
               validatorRecordBuilder.setBadElement(this.config);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.CONFIG_CONFILICT.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         }
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.config != null) {
         statements.add(this.config);
      } else {
         ConfigImpl newConfig;
         if (this.isConfig()) {
            newConfig = new ConfigImpl("true");
         } else {
            newConfig = new ConfigImpl("false");
         }

         newConfig.setContext(this.getContext());
         newConfig.setElementPosition(this.getElementPosition());
         newConfig.setParentStatement(this);
         newConfig.init();
         statements.add(newConfig);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
