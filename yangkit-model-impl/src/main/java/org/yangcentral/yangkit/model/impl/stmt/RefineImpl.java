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
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.stmt.Case;
import org.yangcentral.yangkit.model.api.stmt.Choice;
import org.yangcentral.yangkit.model.api.stmt.Config;
import org.yangcentral.yangkit.model.api.stmt.ConfigSupport;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Default;
import org.yangcentral.yangkit.model.api.stmt.Description;
import org.yangcentral.yangkit.model.api.stmt.IfFeature;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.Mandatory;
import org.yangcentral.yangkit.model.api.stmt.MandatorySupport;
import org.yangcentral.yangkit.model.api.stmt.MaxElements;
import org.yangcentral.yangkit.model.api.stmt.MetaDef;
import org.yangcentral.yangkit.model.api.stmt.MinElements;
import org.yangcentral.yangkit.model.api.stmt.MultiInstancesDataNode;
import org.yangcentral.yangkit.model.api.stmt.Must;
import org.yangcentral.yangkit.model.api.stmt.MustSupport;
import org.yangcentral.yangkit.model.api.stmt.Presence;
import org.yangcentral.yangkit.model.api.stmt.Reference;
import org.yangcentral.yangkit.model.api.stmt.Refine;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
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

   public void setTarget(SchemaNode schemaNode) {
      this.targetNode = schemaNode;
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
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.getElementPosition());
                  validatorRecordBuilder.setBadElement(this);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.NOT_SUPPORT_CONFIG.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }

            Iterator var5;
            if (this.getIfFeatures().size() > 0) {
               if (!(this.targetNode instanceof DataNode) && !(this.targetNode instanceof Choice) && !(this.targetNode instanceof Case)) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.getElementPosition());
                  validatorRecordBuilder.setBadElement(this);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.NOT_SUPPORT_IFFEATURE.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  DataDefinition dataDefinition = (DataDefinition)this.targetNode;
                  var5 = this.getIfFeatures().iterator();

                  while(var5.hasNext()) {
                     IfFeature ifFeature = (IfFeature)var5.next();
                     validatorResultBuilder.merge(dataDefinition.addIfFeature(ifFeature));
                  }
               }
            }

            if (this.getMusts().size() > 0) {
               if (this.targetNode instanceof MustSupport) {
                  MustSupport mustSupport = (MustSupport)this.targetNode;
                  var5 = this.getMusts().iterator();

                  while(var5.hasNext()) {
                     Must must = (Must)var5.next();
                     validatorResultBuilder.merge(mustSupport.addMust(must));
                  }
               } else {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.getElementPosition());
                  validatorRecordBuilder.setBadElement(this);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.NOT_SUPPORT_MUST.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  }

                  leaf.setDefault((Default)this.defaults.get(0));
               } else if (this.targetNode instanceof Choice) {
                  Choice choice = (Choice)this.targetNode;
                  if (this.defaults.size() > 1) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(1)).getElementPosition());
                     validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(1));
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  }

                  boolean bool = choice.setDefault(this.defaults.get(0));
                  if (!bool) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setBadElement(this.defaults.get(0));
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath((this.defaults.get(0)).getElementPosition());
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_CASE.toString(new String[]{"name=" + ((Default)this.defaults.get(0)).getArgStr()})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  }
               } else if (this.targetNode instanceof LeafList) {
                  LeafList leafList = (LeafList)this.targetNode;
                  leafList.setDefaults(this.defaults);
               } else {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
                  validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }

            if (this.mandatory != null) {
               if (!(this.targetNode instanceof MandatorySupport)) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.mandatory.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.mandatory);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.presence.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.presence);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }

            MultiInstancesDataNode multiInstancesDataNode;
            if (this.maxElements != null) {
               if (this.targetNode instanceof MultiInstancesDataNode) {
                  multiInstancesDataNode = (MultiInstancesDataNode)this.targetNode;
                  multiInstancesDataNode.setMaxElements(this.maxElements);
               } else {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.maxElements.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.maxElements);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }

            if (this.minElements != null) {
               if (this.targetNode instanceof MultiInstancesDataNode) {
                  multiInstancesDataNode = (MultiInstancesDataNode)this.targetNode;
                  multiInstancesDataNode.setMinElements(this.minElements);
               } else {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.minElements.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.minElements);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }

            if (this.getUnknowns().size() > 0) {
               this.targetNode.getUnknowns().addAll(this.getUnknowns());
            }
         default:
            return validatorResultBuilder.build();
      }
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());
      if (!ModelUtil.isDescendantSchemaNodeIdentifier(this.getArgStr())) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_ARG.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      Iterator var6 = this.getSubElements().iterator();

      while(var6.hasNext()) {
         YangElement subElement = (YangElement)var6.next();
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
