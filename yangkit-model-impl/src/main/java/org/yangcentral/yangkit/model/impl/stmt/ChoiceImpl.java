package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Case;
import org.yangcentral.yangkit.model.api.stmt.Choice;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Default;
import org.yangcentral.yangkit.model.api.stmt.Mandatory;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ChoiceImpl extends SchemaDataNodeImpl implements Choice {
   private Default aDefault;
   private Case defaultCase;
   private List<Case> cases = new ArrayList();
   private Mandatory mandatory;
   private SchemaNodeContainerImpl schemaNodeContainer = new SchemaNodeContainerImpl(this);
   private List<DataDefinition> dataDefinitions = new ArrayList();
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

   public void removeSchemaNodeChild(QName identifier) {
      this.schemaNodeContainer.removeSchemaNodeChild(identifier);
   }

   public void removeSchemaNodeChild(SchemaNode schemaNode) {
      this.schemaNodeContainer.removeSchemaNodeChild(schemaNode);
   }

   public SchemaNode getMandatoryDescendant() {
      return null;
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangElement> subElements = this.getSubElements();
      Iterator iterator = subElements.iterator();
      this.dataDefinitions.clear();
      while(iterator.hasNext()) {
         YangElement subElement = (YangElement)iterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
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
                  validatorResultBuilder.merge(this.addDataDefChild((DataDefinition)builtinStatement));
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
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setBadElement((YangStatement)matched.get(0));
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(((YangStatement)matched.get(0)).getElementPosition());
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MANDATORY_HASDEFAULT.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            return validatorResultBuilder.build();
         }

         this.aDefault = (Default)matched.get(0);
         Case defaultCase = this.getCase(this.aDefault.getArgStr());
         if (null == defaultCase) {
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setBadElement(this.aDefault);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(this.aDefault.getElementPosition());
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_CASE.toString(new String[]{"name=" + this.aDefault.getArgStr()})));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
         ValidatorRecordBuilder validatorRecordBuilder;
         if (!defCase.hasDefault()) {
            validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setBadElement(defCase);
            validatorRecordBuilder.setSeverity(Severity.WARNING);
            validatorRecordBuilder.setErrorPath(defCase.getElementPosition());
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DEFAULT_CASE_NO_DEFAULT.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         }

         if (defCase.isMandatory()) {
            validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setBadElement(defCase);
            validatorRecordBuilder.setSeverity(Severity.WARNING);
            validatorRecordBuilder.setErrorPath(defCase.getElementPosition());
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DEFAULT_CASE_IS_MANDATORY.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         }

         return validatorResultBuilder.build();
      }
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder;
      validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.buildSelf(phase));
      Iterator iterator;
      Case c;
      label44:
      switch (phase) {
         case GRAMMAR:
            Case defaultCase = this.getDefaultCase();
            if (defaultCase != null) {
               ValidatorRecordBuilder validatorRecordBuilder;
               if (!defaultCase.evaluateFeatures()) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setBadElement(defaultCase);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(defaultCase.getElementPosition());
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_CASE.toString(new String[]{"name=" + defaultCase.getArgStr()})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else if (this.getIfFeatures().size() > 0) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setBadElement(defaultCase);
                  validatorRecordBuilder.setSeverity(Severity.WARNING);
                  validatorRecordBuilder.setErrorPath(defaultCase.getElementPosition());
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DEFAULT_CASE_IS_OPTIONAL.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }
            break;
         case SCHEMA_BUILD:
            iterator = this.cases.iterator();

            while(true) {
               if (!iterator.hasNext()) {
                  break label44;
               }

               c = (Case)iterator.next();
               if (c.evaluateFeatures()) {
                  this.addSchemaNodeChild(c);
                  if (c.isShortCase()) {
                     validatorResultBuilder.merge(c.build(phase));
                  }
               }
            }
         case SCHEMA_TREE:
            iterator = this.cases.iterator();

            while(iterator.hasNext()) {
               c = (Case)iterator.next();
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
      Iterator iterator = this.dataDefinitions.iterator();

      DataDefinition dataDefinition;
      do {
         if (!iterator.hasNext()) {
            return null;
         }

         dataDefinition = (DataDefinition)iterator.next();
      } while(!dataDefinition.getArgStr().equals(name));

      return dataDefinition;
   }

   public ValidatorResult addDataDefChild(DataDefinition dataDefinition) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Case old;
      if (dataDefinition instanceof Case) {
         old = this.getCase(dataDefinition.getArgStr());
         if (null != old) {
            validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(old, dataDefinition));
            dataDefinition.setErrorStatement(true);
            return validatorResultBuilder.build();
         }

         Case newCase = (Case)dataDefinition;
         this.dataDefinitions.add(newCase);
         this.cases.add(newCase);
         newCase.setParent(this);
      } else {
         old = this.getCase(dataDefinition.getArgStr());
         if (old != null) {
            validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(old, dataDefinition));
            dataDefinition.setErrorStatement(true);
            return validatorResultBuilder.build();
         }

         Case newCase = new CaseImpl(dataDefinition.getArgStr());
         YangContext childContext = new YangContext(this.getContext());
         newCase.setContext(childContext);
         newCase.setElementPosition(this.getElementPosition());
         newCase.setShortCase(true);
         newCase.setParent(this);
         newCase.addChild(dataDefinition);
         newCase.init();
         this.dataDefinitions.add(newCase);
         this.cases.add(newCase);
         validatorResultBuilder.merge(newCase.init());
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
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

      statements.addAll(this.dataDefinitions);
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
