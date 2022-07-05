package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.stmt.Action;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Grouping;
import org.yangcentral.yangkit.model.api.stmt.IfFeature;
import org.yangcentral.yangkit.model.api.stmt.Input;
import org.yangcentral.yangkit.model.api.stmt.Output;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActionImpl extends SchemaNodeImpl implements Action {
   private GroupingDefContainerImpl groupingDefContainer = new GroupingDefContainerImpl();
   private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
   private TypedefContainerImpl typedefContainer = new TypedefContainerImpl();
   private Input input;
   private Output output;
   private IfFeatureSupportImpl ifFeatureSupport = new IfFeatureSupportImpl();
   private QName identifier;

   public ActionImpl(String argStr) {
      super(argStr);
   }

   public void setContext(YangContext context) {
      super.setContext(context);
      this.groupingDefContainer.setYangContext(context);
      this.schemaNodeContainer.setYangContext(context);
      this.typedefContainer.setYangContext(context);
   }

   public List<Grouping> getGroupings() {
      return this.groupingDefContainer.getGroupings();
   }

   public Grouping getGrouping(String name) {
      return this.groupingDefContainer.getGrouping(name);
   }

   public Input getInput() {
      return this.input;
   }

   public Output getOutput() {
      return this.output;
   }

   public void setOutput(Output output) {
      this.output = output;
   }

   public void setInput(Input input) {
      this.input = input;
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
      return null;
   }

   public List<Typedef> getTypedefs() {
      return this.typedefContainer.getTypedefs();
   }

   public Typedef getTypedef(int index) {
      return this.typedefContainer.getTypedef(index);
   }

   public Typedef getTypedef(String defName) {
      return this.typedefContainer.getTypedef(defName);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.ACTION.getQName();
   }

   public List<IfFeature> getIfFeatures() {
      return this.ifFeatureSupport.getIfFeatures();
   }

   public ValidatorResult addIfFeature(IfFeature ifFeature) {
      return this.ifFeatureSupport.addIfFeature(ifFeature);
   }

   public void setIfFeatures(List<IfFeature> ifFeatures) {
      this.ifFeatureSupport.setIfFeatures(ifFeatures);
   }

   public boolean evaluateFeatures() {
      return this.ifFeatureSupport.evaluateFeatures();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.GROUPING.getQName());
      Iterator var3;
      YangStatement statement;
      if (matched.size() > 0) {
         var3 = matched.iterator();

         while(var3.hasNext()) {
            statement = (YangStatement)var3.next();
            Grouping grouping = (Grouping)statement;
            validatorResultBuilder.merge(this.groupingDefContainer.addGrouping(grouping));
         }
      }

      matched = this.getSubStatement(YangBuiltinKeyword.TYPEDEF.getQName());
      if (matched.size() > 0) {
         var3 = matched.iterator();

         while(var3.hasNext()) {
            statement = (YangStatement)var3.next();
            Typedef typedef = (Typedef)statement;
            validatorResultBuilder.merge(this.typedefContainer.addTypedef(typedef));
         }
      }

      matched = this.getSubStatement(YangBuiltinKeyword.IFFEATURE.getQName());
      if (matched.size() > 0) {
         var3 = matched.iterator();

         while(var3.hasNext()) {
            statement = (YangStatement)var3.next();
            IfFeature ifFeature = (IfFeature)statement;
            validatorResultBuilder.merge(this.ifFeatureSupport.addIfFeature(ifFeature));
         }
      }

      matched = this.getSubStatement(YangBuiltinKeyword.INPUT.getQName());
      if (matched.size() > 0) {
         this.input = (Input)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.OUTPUT.getQName());
      if (matched.size() > 0) {
         this.output = (Output)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   private boolean isAncestorNodeNoKey() {
      for(SchemaNodeContainer parent = this.getParentSchemaNode(); parent != null; parent = ((SchemaNode)parent).getParentSchemaNode()) {
         if (!(parent instanceof SchemaNode)) {
            return false;
         }

         if (parent instanceof YangList) {
            YangList list = (YangList)parent;
            if (list.getKey() == null) {
               return true;
            }
         }
      }

      return false;
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.validateSelf());
      if (this.isAncestorNodeNoKey()) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.ACTION_IN_LIST_NO_KEY.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      switch (phase) {
         case SCHEMA_BUILD:
            this.setSchemaTreeType(SchemaTreeType.RPCTREE);
            if (this.input != null) {
               this.schemaNodeContainer.addSchemaNodeChild(this.input);
            } else {
               Input input = new InputImpl((String)null);
               input.setContext(new YangContext(this.getContext()));
               input.setElementPosition(this.getElementPosition());
               input.setParentStatement(this);
               input.init();
               input.build();
               this.schemaNodeContainer.addSchemaNodeChild(input);
            }

            if (this.output != null) {
               this.schemaNodeContainer.addSchemaNodeChild(this.output);
            } else {
               Output output = new OutputImpl((String)null);
               output.setContext(new YangContext(this.getContext()));
               output.setElementPosition(this.getElementPosition());
               output.setParentStatement(this);
               output.init();
               output.build();
               this.schemaNodeContainer.addSchemaNodeChild(output);
            }
         default:
            return validatorResultBuilder.build();
      }
   }

   public boolean isConfig() {
      return false;
   }

   public QName getIdentifier() {
      if (this.identifier != null) {
         return this.identifier;
      } else {
         this.identifier = new QName(this.getContext().getNamespace(), this.getArgStr());
         return this.identifier;
      }
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      statements.addAll(this.groupingDefContainer.getGroupings());
      statements.addAll(this.typedefContainer.getTypedefs());
      if (this.input != null) {
         statements.add(this.input);
      }

      if (this.output != null) {
         statements.add(this.output);
      }

      statements.addAll(this.ifFeatureSupport.getIfFeatures());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
