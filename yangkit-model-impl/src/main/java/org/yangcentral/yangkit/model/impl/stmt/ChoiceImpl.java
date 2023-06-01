package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ChoiceImpl extends SchemaDataNodeImpl implements Choice {
   private Default aDefault;
   private Case defaultCase;
   private List<Case> cases = new ArrayList<>();
   private Mandatory mandatory;
   private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
   private List<DataDefinition> dataDefinitions = new ArrayList<>();
   private QName identifier;

   public ChoiceImpl(String argStr) {
      super(argStr);
   }

   public Default getDefault() {
      return this.aDefault;
   }

   public boolean setDefault(Default aDefault) {
      if (null == aDefault) {
         this.aDefault = null;
         return true;
      }

      Case defaultCase = this.getCase(aDefault.getArgStr());
      if (defaultCase == null) {
         return false;
      } else {
         this.setDefaultCase(defaultCase);
         this.aDefault = aDefault;
         return true;
      }
   }

   public Case getDefaultCase() {
      return this.defaultCase;
   }

   public boolean setDefaultCase(Case defaultCase) {
      if (this.getCase(defaultCase.getArgStr()) == null) {
         return false;
      } else {
         this.defaultCase = defaultCase;
         return true;
      }
   }

   public List<Case> getCases() {
      return Collections.unmodifiableList(this.cases);
   }

   public boolean addCase(Case aCase) {
      if (aCase == null) {
         return false;
      } else {
         Iterator caseIterator = this.cases.iterator();

         Case ca;
         do {
            if (!caseIterator.hasNext()) {
               aCase.setParent(this);
               return this.cases.add(aCase);
            }

            ca = (Case)caseIterator.next();
         } while(!ca.getIdentifier().equals(aCase.getIdentifier()));

         return false;
      }
   }

   public boolean removeCase(Case ca){
      return cases.remove(ca);
   }

   public Case removeCase(QName identifier){
      for(Case c:cases){
         if(c.getIdentifier().equals(identifier)){
            cases.remove(c);
            return c;
         }
      }
      return null;
   }
   private Case getCase(String name) {
      Iterator caseIterator = this.cases.iterator();

      Case c;
      do {
         if (!caseIterator.hasNext()) {
            return null;
         }

         c = (Case)caseIterator.next();
      } while(!c.getArgStr().equals(name));

      return c;
   }

   public Mandatory getMandatory() {
      return this.mandatory;
   }

   public void setMandatory(Mandatory mandatory) {
      this.mandatory = mandatory;
   }

   public boolean isMandatory() {
      return null == this.mandatory ? false : Boolean.getBoolean(this.mandatory.getArgStr());
   }

   public boolean hasDefault() {
      return this.aDefault != null;
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

   @Override
   public List<SchemaNode> getTreeNodeChildren() {
      return schemaNodeContainer.getTreeNodeChildren();
   }

   @Override
   public SchemaNode getTreeNodeChild(QName identifier) {
      return schemaNodeContainer.getTreeNodeChild(identifier);
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

   @Override
   public boolean checkChild(YangStatement subStatement) {
      boolean result =  super.checkChild(subStatement);
      if(!result){
         return false;
      }
      YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(subStatement.getYangKeyword());
      switch (builtinKeyword){
         case CASE:
         case ANYDATA:
         case ANYXML:
         case CHOICE:
         case CONTAINER:
         case LEAF:
         case LEAFLIST:
         case LIST:{
            if(getDataDefChild(subStatement.getArgStr()) != null){
               return false;
            }
            return true;
         }
         default:{
            return true;
         }
      }
   }

   @Override
   protected void clearSelf() {
      this.dataDefinitions.clear();
      this.cases.clear();
      this.mandatory = null;
      this.aDefault = null;
      this.schemaNodeContainer.removeSchemaNodeChildren();
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangElement> subElements = this.getSubElements();

      for (YangElement subElement : subElements) {
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement) subElement;
            YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(builtinStatement.getYangKeyword());
            switch (builtinKeyword) {
               case CASE:
               case ANYDATA:
               case ANYXML:
               case CHOICE:
               case CONTAINER:
               case LEAF:
               case LEAFLIST:
               case LIST:
                  validatorResultBuilder.merge(this.addDataDefChild((DataDefinition) builtinStatement));
            }
         }
      }

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.MANDATORY.getQName());
      if (matched.size() > 0) {
         this.mandatory = (Mandatory)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.DEFAULT.getQName());
      if (matched.size() > 0) {
         if (this.mandatory != null && this.mandatory.getValue()) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(matched.get(0),Severity.WARNING,ErrorTag.BAD_ELEMENT,
                    ErrorCode.MANDATORY_HASDEFAULT.getFieldName()));
            return validatorResultBuilder.build();
         }

         this.aDefault = (Default)matched.get(0);
         Case defaultCase = this.getCase(this.aDefault.getArgStr());
         if (null == defaultCase) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this.aDefault,
                    ErrorCode.MISSING_CASE.toString(new String[]{"name=" + this.aDefault.getArgStr()})));
            return validatorResultBuilder.build();
         }

         this.setDefaultCase(defaultCase);
      }

      return validatorResultBuilder.build();
   }

   public void setContext(YangContext context) {
      super.setContext(context);
      this.schemaNodeContainer.setYangContext(context);
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.validateSelf());
      Case defCase = this.getDefaultCase();
      if (null == defCase) {
         return validatorResultBuilder.build();
      } else {
         if (!defCase.hasDefault()) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(defCase,Severity.WARNING,ErrorTag.BAD_ELEMENT,
                    ErrorCode.DEFAULT_CASE_NO_DEFAULT.getFieldName()));
         }

         if (defCase.isMandatory()) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(defCase,Severity.WARNING,ErrorTag.BAD_ELEMENT,
                    ErrorCode.DEFAULT_CASE_IS_MANDATORY.getFieldName() ));
         }

         return validatorResultBuilder.build();
      }
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder;
      validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.buildSelf(phase));
      label44:
      switch (phase) {
         case GRAMMAR:
            Case defaultCase = this.getDefaultCase();
            if (defaultCase != null) {
               if (!defaultCase.evaluateFeatures()) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(defaultCase,
                          ErrorCode.MISSING_CASE.toString(new String[]{"name=" + defaultCase.getArgStr()})));
               } else if (this.getIfFeatures().size() > 0) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(defaultCase,
                          ErrorCode.DEFAULT_CASE_IS_OPTIONAL.getFieldName()));
               }
            }
            for (Case c: this.cases) {
               if (c.evaluateFeatures() && c.isShortCase()) {
                  validatorResultBuilder.merge(c.build(phase));
               }
            }
            break;
         case SCHEMA_BUILD:
            Iterator<Case> iterator = this.cases.iterator();

            while(true) {
               if (!iterator.hasNext()) {
                  break label44;
               }

               Case c = iterator.next();
               if (c.evaluateFeatures()) {
                  this.addSchemaNodeChild(c);
                  if (c.isShortCase()) {
                     validatorResultBuilder.merge(c.build(phase));
                  }
               }
            }
         case SCHEMA_TREE:
            for (Case c: this.cases) {
               if (c.evaluateFeatures() && c.isShortCase()) {
                  validatorResultBuilder.merge(c.build(phase));
               }
            }
      }

      ValidatorResult validatorResult = validatorResultBuilder.build();
      return validatorResult;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.CHOICE.getQName();
   }

   public QName getIdentifier() {
      if (this.identifier != null) {
         return this.identifier;
      } else {
         this.identifier = new QName(this.getContext().getNamespace(), this.getArgStr());
         return this.identifier;
      }
   }

   public List<DataDefinition> getDataDefChildren() {
      return Collections.unmodifiableList(this.dataDefinitions);
   }

   public DataDefinition getDataDefChild(String name) {
      Iterator<DataDefinition> iterator = this.dataDefinitions.iterator();

      DataDefinition dataDefinition;
      do {
         if (!iterator.hasNext()) {
            return null;
         }

         dataDefinition = iterator.next();
      } while(!dataDefinition.getArgStr().equals(name));

      return dataDefinition;
   }

   public ValidatorResult addDataDefChild(DataDefinition dataDefinition) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Case old;
      old = this.getCase(dataDefinition.getArgStr());
      if (null != old) {
         validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(old, dataDefinition));
         dataDefinition.setErrorStatement(true);
         return validatorResultBuilder.build();
      }
      if (dataDefinition instanceof Case) {
         Case newCase = (Case)dataDefinition;
         this.dataDefinitions.add(newCase);
         this.cases.add(newCase);
         newCase.setParent(this);
      } else {
         Case newCase = new CaseImpl(dataDefinition.getArgStr());
         YangContext childContext = new YangContext(this.getContext());
         newCase.setContext(childContext);
         newCase.setElementPosition(this.getElementPosition());
         newCase.setShortCase(true);
         newCase.setParent(this);
         newCase.addChild(dataDefinition);
         this.dataDefinitions.add(newCase);
         this.cases.add(newCase);
         validatorResultBuilder.merge(newCase.init());
      }

      return validatorResultBuilder.build();
   }
   @Override
   public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) {
      return schemaNodeContainer.getEffectiveSchemaNodeChildren(ignoreNamespace);
   }
   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList<>();
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

      if (this.aDefault != null) {
         statements.add(this.aDefault);
      }

      statements.addAll(getEffectiveSchemaNodeChildren());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
