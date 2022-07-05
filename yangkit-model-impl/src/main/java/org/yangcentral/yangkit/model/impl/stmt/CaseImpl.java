package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.stmt.Case;
import org.yangcentral.yangkit.model.api.stmt.Choice;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CaseImpl extends DataDefinitionImpl implements Case {
   private boolean shortCase = false;
   private Choice parent;
   private SchemaPath.Absolute schemaPath;
   private DataDefContainerImpl dataDefContainer = new DataDefContainerImpl();
   private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
   private QName identifier;

   public void setContext(YangContext context) {
      super.setContext(context);
      this.dataDefContainer.setYangContext(context);
      this.schemaNodeContainer.setYangContext(context);
   }

   public CaseImpl(String argStr) {
      super(argStr);
   }

   public boolean isShortCase() {
      return this.shortCase;
   }

   public void setShortCase(boolean bool) {
      this.shortCase = bool;
   }

   public Choice getParent() {
      return this.parent;
   }

   public void setParent(Choice choice) {
      this.parent = choice;
   }

   public List<DataDefinition> getDataDefChildren() {
      return this.dataDefContainer.getDataDefChildren();
   }

   public DataDefinition getDataDefChild(String name) {
      return this.dataDefContainer.getDataDefChild(name);
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangElement> subElements = this.getSubElements();
      Iterator var3 = subElements.iterator();

      while(var3.hasNext()) {
         YangElement subElement = (YangElement)var3.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(builtinStatement.getYangKeyword());
            switch (builtinKeyword) {
               case CONTAINER:
               case LIST:
               case LEAF:
               case LEAFLIST:
               case ANYDATA:
               case ANYXML:
               case CHOICE:
               case USES:
                  DataDefinition newDataDefinition = (DataDefinition)builtinStatement;
                  validatorResultBuilder.merge(this.dataDefContainer.addDataDefChild(newDataDefinition));
            }
         }
      }

      return validatorResultBuilder.build();
   }

   public ValidatorResult addDataDefChild(DataDefinition dataDefinition) {
      return this.dataDefContainer.addDataDefChild(dataDefinition);
   }

   public boolean isConfig() {
      if (this.getSchemaTreeType() == SchemaTreeType.DATATREE) {
         SchemaNodeContainer schemaParent = this.getParentSchemaNode();
         return schemaParent instanceof SchemaNode ? ((SchemaNode)schemaParent).isConfig() : true;
      } else {
         return false;
      }
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.CASE.getQName();
   }

   public List<SchemaNode> getSchemaNodeChildren() {
      return this.schemaNodeContainer.getSchemaNodeChildren();
   }

   public ValidatorResult addSchemaNodeChild(SchemaNode schemaNode) {
      return this.schemaNodeContainer.addSchemaNodeChild(schemaNode);
   }

   public ValidatorResult addSchemaNodeChildren(List<SchemaNode> schemaNodes) {
      return this.schemaNodeContainer.addSchemaNodeChildren(schemaNodes);
   }

   public SchemaNode getSchemaNodeChild(QName identifier) {
      return this.schemaNodeContainer.getSchemaNodeChild(identifier);
   }

   public DataNode getDataNodeChild(QName identifier) {
      return this.schemaNodeContainer.getDataNodeChild(identifier);
   }

   public List<DataNode> getDataNodeChildren() {
      return this.schemaNodeContainer.getDataNodeChildren();
   }

   public void removeSchemaNodeChild(QName identifier) {
      this.schemaNodeContainer.removeSchemaNodeChild(identifier);
   }

   public void removeSchemaNodeChild(SchemaNode schemaNode) {
      this.schemaNodeContainer.removeSchemaNodeChild(schemaNode);
   }

   public SchemaNode getMandatoryDescendant() {
      return this.schemaNodeContainer.getMandatoryDescendant();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.buildSelf(phase));
      switch (phase) {
         case SCHEMA_BUILD:
            Iterator var3 = this.getDataDefChildren().iterator();

            while(var3.hasNext()) {
               DataDefinition dataDefinition = (DataDefinition)var3.next();
               if (dataDefinition.evaluateFeatures()) {
                  validatorResultBuilder.merge(this.addSchemaNodeChild(dataDefinition));
               }
            }
         default:
            return validatorResultBuilder.build();
      }
   }

   public QName getIdentifier() {
      if (this.identifier != null) {
         return this.identifier;
      } else {
         if (this.isShortCase()) {
            DataDefinition child = this.getDataDefChild(this.getArgStr());
            this.identifier = new QName(child.getContext().getNamespace(), this.getArgStr());
         } else {
            this.identifier = new QName(this.getContext().getNamespace(), this.getArgStr());
         }

         return this.identifier;
      }
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(this.dataDefContainer.getDataDefChildren());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
