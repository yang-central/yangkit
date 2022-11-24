package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
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
            if (this.description != null) {
//               YangStatement candidate = description.clone();
//               candidate.setContext(description.getContext());
//               List<YangStatement> statements = targetNode.getSubStatement(YangBuiltinKeyword.DESCRIPTION.getQName());
//               if(statements.isEmpty()){
//                  targetNode.addChild(candidate);
//               } else {
//                  targetNode.removeChild(statements.get(0));
//                  targetNode.addChild(candidate);
//               }
               targetNode.setDescription(description);
            }

            if (this.reference != null) {
//               YangStatement candidate = reference.clone();
//               candidate.setContext(reference.getContext());
//               List<YangStatement> statements = targetNode.getSubStatement(YangBuiltinKeyword.REFERENCE.getQName());
//               if(statements.isEmpty()){
//                  targetNode.addChild(candidate);
//               } else {
//                  targetNode.removeChild(statements.get(0));
//                  targetNode.addChild(candidate);
//               }
               targetNode.setReference(reference);
            }
            if (this.config != null) {
               if (this.targetNode instanceof ConfigSupport) {
//                  YangStatement candidate = config.clone();
//                  candidate.setContext(config.getContext());
//                  List<YangStatement> statements = targetNode.getSubStatement(YangBuiltinKeyword.CONFIG.getQName());
//                  if(statements.isEmpty()){
//                     targetNode.addChild(candidate);
//                  } else {
//                     targetNode.removeChild(statements.get(0));
//                     targetNode.addChild(candidate);
//                  }
                  ConfigSupport configSupport = (ConfigSupport) targetNode;
                  configSupport.setConfig(config);
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
//                     YangStatement candidate = ifFeature.clone();
//                     candidate.setContext(ifFeature.getContext());
//                     YangStatement orig = targetNode.getSubStatement(YangBuiltinKeyword.IFFEATURE.getQName(),ifFeature.getArgStr());
//                     if(orig == null){
//                        targetNode.addChild(candidate);
//                     } else {
//                        targetNode.removeChild(orig);
//                        targetNode.addChild(candidate);
//                     }
                     dataDefinition.addIfFeature(ifFeature);
                  }
               }
            }

            if (this.getMusts().size() > 0) {
               if (this.targetNode instanceof MustSupport) {
                  MustSupport mustSupport = (MustSupport)this.targetNode;
                  iterator = this.getMusts().iterator();

                  while(iterator.hasNext()) {
                     Must must = (Must)iterator.next();
//                     YangStatement candidate = must.clone();
//                     candidate.setContext(must.getContext());
//                     YangStatement orig = targetNode.getSubStatement(YangBuiltinKeyword.MUST.getQName(),must.getArgStr());
//                     if(orig == null){
//                        targetNode.addChild(candidate);
//                     } else {
//                        targetNode.removeChild(orig);
//                        targetNode.addChild(candidate);
//                     }
                     mustSupport.addMust(must);
                  }
               } else {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                          ErrorCode.NOT_SUPPORT_MUST.getFieldName()));
               }
            }

            if (this.defaults.size() > 0) {
               if ((this.targetNode instanceof Leaf) || (this.targetNode instanceof Choice)) {
                  if (this.defaults.size() > 1) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(1),
                             ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                  }
//                  YangStatement candidate = defaults.get(0).clone();
//                  candidate.setContext(defaults.get(0).getContext());
//                  List<YangStatement> statements = targetNode.getSubStatement(YangBuiltinKeyword.DEFAULT.getQName());
//                  if(!statements.isEmpty()){
//                     targetNode.removeChild(statements.get(0));
//                  }
//                  targetNode.addChild(candidate);
                  if(targetNode instanceof Leaf){
                     ((Leaf) targetNode).setDefault(defaults.get(0));
                  } else if(targetNode instanceof Choice){
                     ((Choice) targetNode).setDefault(defaults.get(0));
                  }
               } else if (this.targetNode instanceof LeafList) {
//                  List<YangStatement> statements = targetNode.getSubStatement(YangBuiltinKeyword.DEFAULT.getQName());
//                  for(YangStatement dflt:statements){
//                     targetNode.removeChild(dflt);
//                  }
                  LeafList leafList = (LeafList) targetNode;
                  leafList.setDefaults(defaults);
//                  for(Default newDflt:defaults){
////                     YangStatement candidate = newDflt.clone();
////                     candidate.setContext(newDflt.getContext());
//                     leafList.addDefault(newDflt);
//                  }
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
//                  YangStatement candidate = mandatory.clone();
//                  candidate.setContext(mandatory.getContext());
//                  List<YangStatement> statements = targetNode.getSubStatement(YangBuiltinKeyword.MANDATORY.getQName());
//                  if(statements.isEmpty()){
//                     targetNode.addChild(candidate);
//                  } else {
//                     targetNode.removeChild(statements.get(0));
//                     targetNode.addChild(candidate);
//                  }
                  MandatorySupport mandatorySupport = (MandatorySupport) targetNode;
                  mandatorySupport.setMandatory(mandatory);
               }
            }

            if (this.presence != null) {
               if (this.targetNode instanceof Container) {
//                  YangStatement candidate = presence.clone();
//                  candidate.setContext(presence.getContext());
//                  List<YangStatement> statements = targetNode.getSubStatement(YangBuiltinKeyword.PRESENCE.getQName());
//                  if(statements.isEmpty()){
//                     targetNode.addChild(candidate);
//                  } else {
//                     targetNode.removeChild(statements.get(0));
//                     targetNode.addChild(candidate);
//                  }
                  Container container = (Container) targetNode;
                  container.setPresence(presence);
               } else {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(presence,
                          ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               }
            }

            MultiInstancesDataNode multiInstancesDataNode;
            if (this.maxElements != null) {
               if (this.targetNode instanceof MultiInstancesDataNode) {
//                  YangStatement candidate = maxElements.clone();
//                  candidate.setContext(maxElements.getContext());
//                  List<YangStatement> statements = targetNode.getSubStatement(YangBuiltinKeyword.MAXELEMENTS.getQName());
//                  if(statements.isEmpty()){
//                     targetNode.addChild(candidate);
//                  } else {
//                     targetNode.removeChild(statements.get(0));
//                     targetNode.addChild(candidate);
//                  }
                  multiInstancesDataNode = (MultiInstancesDataNode) targetNode;
                  multiInstancesDataNode.setMaxElements(maxElements);
               } else {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(maxElements,
                          ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               }
            }

            if (this.minElements != null) {
               if (this.targetNode instanceof MultiInstancesDataNode) {
//                  YangStatement candidate = minElements.clone();
//                  candidate.setContext(minElements.getContext());
//                  List<YangStatement> statements = targetNode.getSubStatement(YangBuiltinKeyword.MINELEMENTS.getQName());
//                  if(statements.isEmpty()){
//                     targetNode.addChild(candidate);
//                  } else {
//                     targetNode.removeChild(statements.get(0));
//                     targetNode.addChild(candidate);
//                  }
                  multiInstancesDataNode = (MultiInstancesDataNode) targetNode;
                  multiInstancesDataNode.setMinElements(minElements);
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
                     if(!cardinality.isUnbounded()&& cardinality.getMaxElements() <=1){
                        isMultiInstance = false;
                     }
                  }
                  if(isMultiInstance){
//                     YangStatement orig = this.targetNode.getSubStatement(unknown.getYangKeyword(),unknown.getArgStr());
//                     if(orig != null){
//                        this.targetNode.removeChild(orig);
//
//                     }
                     targetNode.getUnknowns().add(unknown);
                  } else {
//                     List<YangStatement> origs = this.targetNode.getSubStatement(unknown.getYangKeyword());
//                     if(!origs.isEmpty()){
//                        this.targetNode.removeChild(origs.get(0));
//                     }
                     List<YangUnknown> unknowns = targetNode.getUnknowns(unknown.getYangKeyword());
                     if(!unknowns.isEmpty()){
                        targetNode.getUnknowns().remove(unknowns.get(0));
                     }
                     targetNode.getUnknowns().add(unknown);
                  }
//                  YangStatement candidate = unknown.clone();
//                  candidate.setContext(unknown.getContext());
//                  targetNode.addChild(candidate);

               }

            }
//            ValidatorResult validatorResult = targetNode.init();
//            if(!validatorResult.isOk()){
//               validatorResultBuilder.merge(validatorResult);
//            } else {
//               validatorResultBuilder.merge(targetNode.build());
//            }
         default:
            return validatorResultBuilder.build();
      }
   }

   @Override
   protected void clearSelf() {
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
      super.clearSelf();
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
