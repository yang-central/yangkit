package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.util.ModelUtil;
import org.yangcentral.yangkit.xpath.impl.XPathUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RefineImpl extends YangBuiltInStatementImpl implements Refine {
   private IfFeatureSupportImpl ifFeatureSupport = new IfFeatureSupportImpl();
   private SchemaPath targetPath;
   private SchemaNode targetNode;
   private Description description;
   private Reference reference;
   private MustSupportImpl mustSupport = new MustSupportImpl();
   private Config config;
   private List<Default> defaults = new ArrayList();
   private Mandatory mandatory;
   private Presence presence;
   private MaxElements maxElements;
   private MinElements minElements;

   public RefineImpl(String argStr) {
      super(argStr);
   }

   public void setContext(YangContext context) {
      super.setContext(context);
      this.ifFeatureSupport.setYangContext(context);
   }

   public SchemaNode getTarget() {
      return this.targetNode;
   }

   public void setTarget(SchemaNode target) {
      this.targetNode = target;
   }

   public SchemaPath getTargetPath() {
      return this.targetPath;
   }

   public void setTargetPath(SchemaPath schemaPath) {
      this.targetPath = schemaPath;
   }

   public List<IfFeature> getIfFeatures() {
      return this.ifFeatureSupport.getIfFeatures();
   }

   public ValidatorResult addIfFeature(IfFeature ifFeature) {
      return this.ifFeatureSupport.addIfFeature(ifFeature);
   }

   @Override
   public IfFeature getIfFeature(String exp) {
      return ifFeatureSupport.getIfFeature(exp);
   }

   @Override
   public IfFeature removeIfFeature(String exp) {
      return ifFeatureSupport.removeIfFeature(exp);
   }

   public void setIfFeatures(List<IfFeature> ifFeatures) {
      this.ifFeatureSupport.setIfFeatures(ifFeatures);
   }

   public boolean evaluateFeatures() {
      return this.ifFeatureSupport.evaluateFeatures();
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

   public Must getMust(int index) {
      return this.mustSupport.getMust(index);
   }

   public Must getMust(String condition) {
      return this.mustSupport.getMust(condition);
   }

   public List<Must> getMusts() {
      return this.mustSupport.getMusts();
   }

   public void setMusts(List<Must> musts) {
      this.mustSupport.setMusts(musts);
   }

   public ValidatorResult addMust(Must must) {
      return this.mustSupport.addMust(must);
   }

   public void removeMust(String condition) {
      this.mustSupport.removeMust(condition);
   }

   public ValidatorResult updateMust(Must must) {
      return this.mustSupport.updateMust(must);
   }

   public ValidatorResult validateMusts() {
      return this.mustSupport.validateMusts();
   }

   public Config getConfig() {
      return this.config;
   }

   public List<Default> getDefaults() {
      return Collections.unmodifiableList(this.defaults);
   }

   public Mandatory getMandatory() {
      return this.mandatory;
   }

   public Presence getPresence() {
      return this.presence;
   }

   public MinElements getMinElements() {
      return this.minElements;
   }

   public MaxElements getMaxElements() {
      return this.maxElements;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.REFINE.getQName();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      switch (phase) {
         case SCHEMA_BUILD:
            this.mustSupport.setContextNode(XPathUtil.getXPathContextNode(this.targetNode));
            MetaDef metaDef = this.targetNode;
            if (this.description != null) {
               metaDef.setDescription(this.description);
            }

            if (this.reference != null) {
               metaDef.setReference(this.reference);
            }

            ValidatorRecordBuilder validatorRecordBuilder;
            if (this.config != null) {
               if (this.targetNode instanceof ConfigSupport) {
                  ConfigSupport configSupport = (ConfigSupport)this.targetNode;
                  configSupport.setConfig(this.config);
               } else {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                          ErrorCode.NOT_SUPPORT_CONFIG.getFieldName()));
               }
            }

            Iterator iterator;
            if (this.getIfFeatures().size() > 0) {
               if (!(this.targetNode instanceof DataNode) && !(this.targetNode instanceof Choice) && !(this.targetNode instanceof Case)) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                          ErrorCode.NOT_SUPPORT_IFFEATURE.getFieldName()));
               } else {
                  DataDefinition dataDefinition = (DataDefinition)this.targetNode;
                  iterator = this.getIfFeatures().iterator();

                  while(iterator.hasNext()) {
                     IfFeature ifFeature = (IfFeature)iterator.next();
                     if(dataDefinition.getIfFeature(ifFeature.getArgStr()) != null){
                        dataDefinition.removeIfFeature(ifFeature.getArgStr());
                     }
                     validatorResultBuilder.merge(dataDefinition.addIfFeature(ifFeature));
                  }
               }
            }

            if (this.getMusts().size() > 0) {
               if (this.targetNode instanceof MustSupport) {
                  MustSupport mustSupport = (MustSupport)this.targetNode;
                  iterator = this.getMusts().iterator();

                  while(iterator.hasNext()) {
                     Must must = (Must)iterator.next();
                     mustSupport.removeMust(must.getArgStr());
                     validatorResultBuilder.merge(mustSupport.addMust(must));
                  }
               } else {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                          ErrorCode.NOT_SUPPORT_MUST.getFieldName()));
               }
            }

            if (this.defaults.size() > 0) {
               if (this.targetNode instanceof Leaf) {
                  Leaf leaf = (Leaf)this.targetNode;
                  if (this.defaults.size() > 1) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(1)).getElementPosition());
                     validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(1));
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                     validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(1),
                             ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                  }

                  leaf.setDefault((Default)this.defaults.get(0));
               } else if (this.targetNode instanceof Choice) {
                  Choice choice = (Choice)this.targetNode;
                  if (this.defaults.size() > 1) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(1),
                             ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                  }

                  boolean bool = choice.setDefault(this.defaults.get(0));
                  if (!bool) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                             ErrorCode.MISSING_CASE.toString(new String[]{"name="
                                     + ((Default)this.defaults.get(0)).getArgStr()})));
                  }
               } else if (this.targetNode instanceof LeafList) {
                  LeafList leafList = (LeafList)this.targetNode;
                  leafList.setDefaults(this.defaults);
               } else {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                          ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               }
            }

            if (this.mandatory != null) {
               if (!(this.targetNode instanceof MandatorySupport)) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(mandatory,
                          ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               } else {
                  MandatorySupport mandatorySupport = (MandatorySupport)this.targetNode;
                  mandatorySupport.setMandatory(this.mandatory);
               }
            }

            if (this.presence != null) {
               if (this.targetNode instanceof Container) {
                  Container container = (Container)this.targetNode;
                  container.setPresence(this.presence);
               } else {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(presence,
                          ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               }
            }

            MultiInstancesDataNode multiInstancesDataNode;
            if (this.maxElements != null) {
               if (this.targetNode instanceof MultiInstancesDataNode) {
                  multiInstancesDataNode = (MultiInstancesDataNode)this.targetNode;
                  multiInstancesDataNode.setMaxElements(this.maxElements);
               } else {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(maxElements,
                          ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               }
            }

            if (this.minElements != null) {
               if (this.targetNode instanceof MultiInstancesDataNode) {
                  multiInstancesDataNode = (MultiInstancesDataNode)this.targetNode;
                  multiInstancesDataNode.setMinElements(this.minElements);
               } else {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(minElements,
                          ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               }
            }

            if (this.getUnknowns().size() > 0) {
               YangSpecification yangSpecification = this.targetNode.getContext().getYangSpecification();
               YangStatementDef yangStatementDef = yangSpecification.getStatementDef(this.targetNode.getYangKeyword());
               for(YangUnknown unknown:this.getUnknowns()){
                  boolean isMultiInstance = true;
                  Cardinality cardinality = yangStatementDef.getSubStatementCardinality(unknown.getYangKeyword());
                  if(cardinality != null){
                     if(cardinality.getMaxElements() <=1){
                        isMultiInstance = false;
                     }
                  }
                  if(isMultiInstance){
                     YangUnknown orig = this.targetNode.getUnknown(unknown.getYangKeyword(),unknown.getArgStr());
                     if(orig != null){
                        this.targetNode.getUnknowns().remove(orig);

                     }
                  } else {
                     List<YangUnknown> origs = this.targetNode.getUnknowns(unknown.getYangKeyword());
                     if(!origs.isEmpty()){
                        this.targetNode.getUnknowns().remove(origs.get(0));
                     }
                  }
                  this.targetNode.getUnknowns().add(unknown);

               }

            }
         default:
            return validatorResultBuilder.build();
      }
   }

   @Override
   protected void clear() {
      this.config = null;
      this.ifFeatureSupport.removeIfFeatures();
      this.description = null;
      this.reference = null;
      this.mustSupport.removeMusts();
      this.defaults.clear();
      this.mandatory = null;
      this.reference = null;
      this.maxElements = null;
      this.minElements = null;
      super.clear();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      if (!ModelUtil.isDescendantSchemaNodeIdentifier(this.getArgStr())) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.INVALID_ARG.getFieldName() ));
      }

      Iterator elementIterator = this.getSubElements().iterator();

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(((YangBuiltinStatement)subElement).getYangKeyword());
            switch (builtinKeyword) {
               case CONFIG:
                  this.config = (Config)subElement;
                  break;
               case IFFEATURE:
                  validatorResultBuilder.merge(this.addIfFeature((IfFeature)subElement));
                  break;
               case DESCRIPTION:
                  this.description = (Description)subElement;
                  break;
               case REFERENCE:
                  this.reference = (Reference)subElement;
                  break;
               case MUST:
                  validatorResultBuilder.merge(this.addMust((Must)subElement));
                  break;
               case DEFAULT:
                  Default oldDefault = (Default)ModelUtil.checkConflict((Default)subElement, this.defaults);
                  if (oldDefault != null) {
                     validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(oldDefault, (Default)subElement));
                     ((Default)subElement).setErrorStatement(true);
                  } else {
                     this.defaults.add((Default)subElement);
                  }
                  break;
               case MANDATORY:
                  this.mandatory = (Mandatory)subElement;
                  break;
               case PRESENCE:
                  this.presence = (Presence)subElement;
                  break;
               case MAXELEMENTS:
                  this.maxElements = (MaxElements)subElement;
                  break;
               case MINELEMENTS:
                  this.minElements = (MinElements)subElement;
            }
         }
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.config != null) {
         statements.add(this.config);
      }

      statements.addAll(this.defaults);
      if (this.mandatory != null) {
         statements.add(this.mandatory);
      }

      if (this.maxElements != null) {
         statements.add(this.maxElements);
      }

      if (this.minElements != null) {
         statements.add(this.minElements);
      }

      if (this.description != null) {
         statements.add(this.description);
      }

      if (this.reference != null) {
         statements.add(this.reference);
      }

      if (this.presence != null) {
         statements.add(this.presence);
      }

      statements.addAll(this.mustSupport.getMusts());
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
