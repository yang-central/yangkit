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
import org.yangcentral.yangkit.model.api.stmt.Choice;
import org.yangcentral.yangkit.model.api.stmt.Config;
import org.yangcentral.yangkit.model.api.stmt.ConfigSupport;
import org.yangcentral.yangkit.model.api.stmt.Default;
import org.yangcentral.yangkit.model.api.stmt.Deviate;
import org.yangcentral.yangkit.model.api.stmt.DeviateType;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.Mandatory;
import org.yangcentral.yangkit.model.api.stmt.MandatorySupport;
import org.yangcentral.yangkit.model.api.stmt.MaxElements;
import org.yangcentral.yangkit.model.api.stmt.MinElements;
import org.yangcentral.yangkit.model.api.stmt.MultiInstancesDataNode;
import org.yangcentral.yangkit.model.api.stmt.Must;
import org.yangcentral.yangkit.model.api.stmt.MustSupport;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.api.stmt.Unique;
import org.yangcentral.yangkit.model.api.stmt.Units;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;
import org.yangcentral.yangkit.util.ModelUtil;
import org.yangcentral.yangkit.xpath.impl.XPathUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DeviateImpl extends YangBuiltInStatementImpl implements Deviate {
   private SchemaNode target;
   private DeviateType deviateType;
   private Config config;
   private List<Default> defaults = new ArrayList();
   private Mandatory mandatory;
   private MaxElements maxElements;
   private MinElements minElements;
   private MustSupportImpl mustSupport = new MustSupportImpl();
   private Type type;
   private List<Unique> uniques = new ArrayList();
   private Units units;

   public DeviateImpl(String argStr) {
      super(argStr);
   }

   public void setContext(YangContext context) {
      super.setContext(context);
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());

      try {
         DeviateType deviateType = DeviateType.forValue(this.getArgStr());
         this.deviateType = deviateType;
      } catch (IllegalArgumentException var6) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_ARG.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      }

      Iterator var7 = this.getSubElements().iterator();

      while(var7.hasNext()) {
         YangElement subElement = (YangElement)var7.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(((YangBuiltinStatement)subElement).getYangKeyword());
            switch (builtinKeyword) {
               case CONFIG:
                  this.config = (Config)subElement;
                  break;
               case MUST:
                  validatorResultBuilder.merge(this.addMust((Must)subElement));
                  break;
               case DEFAULT:
                  Default oldDefault = (Default) ModelUtil.checkConflict((Default)subElement, this.defaults);
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
               case MAXELEMENTS:
                  this.maxElements = (MaxElements)subElement;
                  break;
               case MINELEMENTS:
                  this.minElements = (MinElements)subElement;
                  break;
               case TYPE:
                  this.type = (Type)subElement;
                  break;
               case UNIQUE:
                  Unique oldUnique = (Unique)ModelUtil.checkConflict((Unique)subElement, this.uniques);
                  if (oldUnique != null) {
                     validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(oldUnique, (Unique)subElement));
                     ((Unique)subElement).setErrorStatement(true);
                  } else {
                     this.uniques.add((Unique)subElement);
                  }
                  break;
               case UNITS:
                  this.units = (Units)subElement;
            }
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult deviateConfig() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      ValidatorRecordBuilder validatorRecordBuilder;
      if (!(this.target instanceof ConfigSupport)) {
         validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.config.getElementPosition());
         validatorRecordBuilder.setBadElement(this.config);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.NOT_SUPPORT_CONFIG.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         switch (this.deviateType) {
            case ADD:
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.config.getElementPosition());
               validatorRecordBuilder.setBadElement(this.config);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=config"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               break;
            case DELETE:
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.config.getElementPosition());
               validatorRecordBuilder.setBadElement(this.config);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DEVIATE_NOT_ALLOWED.toString(new String[]{"property=config", "deviate=delete"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               break;
            case REPLACE:
               ConfigSupport configSupport = (ConfigSupport)this.target;
               configSupport.setConfig(this.config);
               SchemaNode schemaNode = (SchemaNode)configSupport;
               schemaNode.setDeviated(true);
         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateMust() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof MustSupport)) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getMust(0).getElementPosition());
         validatorRecordBuilder.setBadElement(this.getMust(0));
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         MustSupport mustSupport = (MustSupport)this.target;
         Iterator var3 = this.getMusts().iterator();

         while(var3.hasNext()) {
            Must must = (Must)var3.next();
            ValidatorRecordBuilder validatorRecordBuilder;
            SchemaNode schemaNode;
            switch (this.deviateType) {
               case ADD:
                  if (mustSupport.getMust(must.getArgStr()) != null) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(must.getElementPosition());
                     validatorRecordBuilder.setBadElement(must);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=must"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     validatorResultBuilder.merge(mustSupport.addMust(must));
                     schemaNode = (SchemaNode)mustSupport;
                     schemaNode.setDeviated(true);
                  }
                  break;
               case DELETE:
                  if (mustSupport.getMust(must.getArgStr()) == null) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(must.getElementPosition());
                     validatorRecordBuilder.setBadElement(must);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=must"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     mustSupport.removeMust(must.getArgStr());
                     schemaNode = (SchemaNode)mustSupport;
                     schemaNode.setDeviated(true);
                  }
                  break;
               case REPLACE:
                  if (mustSupport.getMust(must.getArgStr()) == null) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(must.getElementPosition());
                     validatorRecordBuilder.setBadElement(must);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=must"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     mustSupport.updateMust(must);
                     schemaNode = (SchemaNode)mustSupport;
                     schemaNode.setDeviated(true);
                  }
            }
         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateLeafDefault() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Leaf leaf = (Leaf)this.target;
      ValidatorRecordBuilder validatorRecordBuilder;
      if (this.defaults.size() > 1) {
         validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(1)).getElementPosition());
         validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(1));
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      switch (this.deviateType) {
         case ADD:
            if (leaf.getDefault() != null) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
               validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=default"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               leaf.setDefault((Default)this.defaults.get(0));
               leaf.setDeviated(true);
            }
            break;
         case DELETE:
            if (leaf.getDefault() == null) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
               validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else if (!leaf.getDefault().getArgStr().equals(((Default)this.defaults.get(0)).getArgStr())) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
               validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=default"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               leaf.setDefault((Default)null);
               leaf.setDeviated(true);
            }
            break;
         case REPLACE:
            if (leaf.getDefault() == null) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
               validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               leaf.setDefault((Default)this.defaults.get(0));
               leaf.setDeviated(true);
            }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult deviateChoiceDefault() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Choice choice = (Choice)this.target;
      ValidatorRecordBuilder validatorRecordBuilder;
      if (this.defaults.size() > 1) {
         validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(1)).getElementPosition());
         validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(1));
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      boolean bool;
      switch (this.deviateType) {
         case ADD:
            if (choice.getDefault() != null) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
               validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=default"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               bool = choice.setDefault((Default)this.defaults.get(0));
               if (!bool) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_CASE.toString(new String[]{"name=" + ((Default)this.defaults.get(0)).getArgStr()})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  choice.setDeviated(true);
               }
            }
            break;
         case DELETE:
            if (choice.getDefault() == null) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
               validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else if (!choice.getDefault().getArgStr().equals(((Default)this.defaults.get(0)).getArgStr())) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
               validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=default"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               choice.setDefault((Default)null);
               choice.setDeviated(true);
            }
            break;
         case REPLACE:
            if (choice.getDefault() == null) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
               validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               bool = choice.setDefault((Default)this.defaults.get(0));
               if (!bool) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_CASE.toString(new String[]{"name=" + ((Default)this.defaults.get(0)).getArgStr()})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  choice.setDeviated(true);
               }
            }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult deviateLeafListDefault() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      LeafList leafList = (LeafList)this.target;
      Iterator var3 = this.defaults.iterator();

      while(var3.hasNext()) {
         Default defl = (Default)var3.next();
         ValidatorRecordBuilder validatorRecordBuilder;
         switch (this.deviateType) {
            case ADD:
               if (leafList.getDefault(defl.getArgStr()) != null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(defl.getElementPosition());
                  validatorRecordBuilder.setBadElement(defl);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=default"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  validatorResultBuilder.merge(leafList.addDefault(defl));
                  leafList.setDeviated(true);
               }
               break;
            case DELETE:
               if (leafList.getDefault(defl.getArgStr()) == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(defl.getElementPosition());
                  validatorRecordBuilder.setBadElement(defl);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  leafList.removeDefault(defl.getArgStr());
                  leafList.setDeviated(true);
               }
               break;
            case REPLACE:
               if (leafList.getDefault(defl.getArgStr()) == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(defl.getElementPosition());
                  validatorRecordBuilder.setBadElement(defl);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  validatorResultBuilder.merge(leafList.updateDefault(defl));
                  leafList.setDeviated(true);
               }
         }
      }

      leafList.setDefaults(this.defaults);
      return validatorResultBuilder.build();
   }

   private ValidatorResult deviateDefault() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (this.target instanceof Leaf) {
         validatorResultBuilder.merge(this.deviateLeafDefault());
      } else if (this.target instanceof Choice) {
         validatorResultBuilder.merge(this.deviateChoiceDefault());
      } else if (this.target instanceof LeafList) {
         validatorResultBuilder.merge(this.deviateLeafListDefault());
      } else {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(((Default)this.defaults.get(0)).getElementPosition());
         validatorRecordBuilder.setBadElement((YangStatement)this.defaults.get(0));
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult deviateMandatory() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof MandatorySupport)) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.mandatory.getElementPosition());
         validatorRecordBuilder.setBadElement(this.mandatory);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         MandatorySupport mandatorySupport = (MandatorySupport)this.target;
         ValidatorRecordBuilder validatorRecordBuilder;
         switch (this.deviateType) {
            case ADD:
               if (mandatorySupport.getMandatory() != null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.mandatory.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.mandatory);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=mandatory"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  mandatorySupport.setMandatory(this.mandatory);
                  ((SchemaNode)mandatorySupport).setDeviated(true);
               }
               break;
            case DELETE:
               if (mandatorySupport.getMandatory() == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.mandatory.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.mandatory);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=mandatory"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else if (!mandatorySupport.getMandatory().getArgStr().equals(this.mandatory.getArgStr())) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.mandatory.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.mandatory);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=mandatory"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  mandatorySupport.setMandatory((Mandatory)null);
                  ((SchemaNode)mandatorySupport).setDeviated(true);
               }
               break;
            case REPLACE:
               if (mandatorySupport.getMandatory() == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.mandatory.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.mandatory);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=mandatory"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  mandatorySupport.setMandatory(this.mandatory);
                  ((SchemaNode)mandatorySupport).setDeviated(true);
               }
         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateMaxElements() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof MultiInstancesDataNode)) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.maxElements.getElementPosition());
         validatorRecordBuilder.setBadElement(this.maxElements);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         MultiInstancesDataNode multiInstancesDataNode = (MultiInstancesDataNode)this.target;
         ValidatorRecordBuilder validatorRecordBuilder;
         switch (this.deviateType) {
            case ADD:
               if (multiInstancesDataNode.getMaxElements() != null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.maxElements.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.maxElements);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=max-elements"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  multiInstancesDataNode.setMaxElements(this.maxElements);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
               break;
            case DELETE:
               if (multiInstancesDataNode.getMaxElements() == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.maxElements.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.maxElements);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=max-elements"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else if (!multiInstancesDataNode.getMaxElements().getArgStr().equals(this.maxElements.getArgStr())) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.maxElements.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.maxElements);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=max-elements"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  multiInstancesDataNode.setMaxElements((MaxElements)null);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
               break;
            case REPLACE:
               if (multiInstancesDataNode.getMaxElements() == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.maxElements.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.maxElements);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=max-elements"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  multiInstancesDataNode.setMaxElements(this.maxElements);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateMinElements() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof MultiInstancesDataNode)) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.minElements.getElementPosition());
         validatorRecordBuilder.setBadElement(this.minElements);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         MultiInstancesDataNode multiInstancesDataNode = (MultiInstancesDataNode)this.target;
         ValidatorRecordBuilder validatorRecordBuilder;
         switch (this.deviateType) {
            case ADD:
               if (multiInstancesDataNode.getMinElements() != null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.minElements.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.minElements);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=min-elements"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  multiInstancesDataNode.setMinElements(this.minElements);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
               break;
            case DELETE:
               if (multiInstancesDataNode.getMinElements() == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.minElements.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.minElements);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=min-elements"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else if (!multiInstancesDataNode.getMinElements().getArgStr().equals(this.minElements.getArgStr())) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.minElements.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.minElements);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=min-elements"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  multiInstancesDataNode.setMinElements((MinElements)null);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
               break;
            case REPLACE:
               if (multiInstancesDataNode.getMinElements() == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.minElements.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.minElements);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=min-elements"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  multiInstancesDataNode.setMinElements(this.minElements);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateUnique() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof YangList)) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(((Unique)this.uniques.get(0)).getElementPosition());
         validatorRecordBuilder.setBadElement((YangStatement)this.uniques.get(0));
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         YangList list = (YangList)this.target;
         Iterator var3 = this.uniques.iterator();

         while(var3.hasNext()) {
            Unique unique = (Unique)var3.next();
            ValidatorRecordBuilder validatorRecordBuilder;
            switch (this.deviateType) {
               case ADD:
                  if (list.getUnique(unique.getArgStr()) != null) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(unique.getElementPosition());
                     validatorRecordBuilder.setBadElement(unique);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=unique"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     validatorResultBuilder.merge(list.addUnique(unique));
                     list.setDeviated(true);
                  }
                  break;
               case DELETE:
                  if (list.getUnique(unique.getArgStr()) == null) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(unique.getElementPosition());
                     validatorRecordBuilder.setBadElement(unique);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=unique"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     list.removeUnique(unique.getArgStr());
                     list.setDeviated(true);
                  }
                  break;
               case REPLACE:
                  if (list.getUnique(unique.getArgStr()) == null) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(unique.getElementPosition());
                     validatorRecordBuilder.setBadElement(unique);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=unique"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     validatorResultBuilder.merge(list.updateUnique(unique));
                     list.setDeviated(true);
                  }
            }
         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateUnits() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof TypedDataNode)) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.units.getElementPosition());
         validatorRecordBuilder.setBadElement(this.units);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         TypedDataNode typedDataNode = (TypedDataNode)this.target;
         ValidatorRecordBuilder validatorRecordBuilder;
         switch (this.deviateType) {
            case ADD:
               if (typedDataNode.getUnits() != null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.units.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.units);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=units"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  typedDataNode.setUnits(this.units);
                  typedDataNode.setDeviated(true);
               }
               break;
            case DELETE:
               if (typedDataNode.getUnits() == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.units.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.units);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=units"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else if (!typedDataNode.getUnits().getArgStr().equals(this.units.getArgStr())) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.units.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.units);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=units"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  typedDataNode.setUnits((Units)null);
                  typedDataNode.setDeviated(true);
               }
               break;
            case REPLACE:
               if (typedDataNode.getUnits() == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.units.getElementPosition());
                  validatorRecordBuilder.setBadElement(this.units);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=units"})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  typedDataNode.setUnits(this.units);
                  typedDataNode.setDeviated(true);
               }
         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateType() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof TypedDataNode)) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.units.getElementPosition());
         validatorRecordBuilder.setBadElement(this.units);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         TypedDataNode typedDataNode = (TypedDataNode)this.target;
         ValidatorRecordBuilder validatorRecordBuilder;
         switch (this.deviateType) {
            case ADD:
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.type.getElementPosition());
               validatorRecordBuilder.setBadElement(this.type);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=type"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               break;
            case DELETE:
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.type.getElementPosition());
               validatorRecordBuilder.setBadElement(this.type);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MANDATORY_CAN_NOT_DELETED.toString(new String[]{"name=type"})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               break;
            case REPLACE:
               validatorResultBuilder.merge(typedDataNode.setType(this.type));
               typedDataNode.setDeviated(true);
         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateUnknown() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var2 = this.getUnknowns().iterator();

      while(true) {
         while(var2.hasNext()) {
            YangUnknown yangUnknown = (YangUnknown)var2.next();
            YangUnknown same;
            Iterator var8;
            ValidatorRecordBuilder validatorRecordBuilder;
            YangUnknown targetUnknown;
            switch (this.deviateType) {
               case ADD:
                  same = null;
                  var8 = this.target.getUnknowns().iterator();

                  while(var8.hasNext()) {
                     targetUnknown = (YangUnknown)var8.next();
                     if (targetUnknown.equals(yangUnknown)) {
                        same = targetUnknown;
                        break;
                     }
                  }

                  if (same != null) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(yangUnknown.getElementPosition());
                     validatorRecordBuilder.setBadElement(yangUnknown);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=unique"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     this.target.getUnknowns().add(yangUnknown);
                     this.target.setDeviated(true);
                  }
                  break;
               case DELETE:
                  same = null;
                  var8 = this.target.getUnknowns().iterator();

                  while(var8.hasNext()) {
                     targetUnknown = (YangUnknown)var8.next();
                     if (targetUnknown.equals(yangUnknown)) {
                        same = targetUnknown;
                        break;
                     }
                  }

                  if (same == null) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(yangUnknown.getElementPosition());
                     validatorRecordBuilder.setBadElement(yangUnknown);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=unique"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     this.target.getUnknowns().remove(same);
                     this.target.setDeviated(true);
                  }
                  break;
               case REPLACE:
                  same = null;
                  int pos = -1;

                  for(int i = 0; i < this.target.getUnknowns().size(); ++i) {
                     targetUnknown = this.target.getUnknowns().get(i);
                     if (targetUnknown.equals(yangUnknown)) {
                        same = targetUnknown;
                        pos = i;
                        break;
                     }
                  }

                  if (same == null) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(yangUnknown.getElementPosition());
                     validatorRecordBuilder.setBadElement(yangUnknown);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=unique"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     this.target.getUnknowns().set(pos, yangUnknown);
                     this.target.setDeviated(true);
                  }
            }
         }

         return validatorResultBuilder.build();
      }
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      switch (phase) {
         case SCHEMA_MODIFIER:
            if (this.target == null) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.getElementPosition());
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_TARGET.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               return validatorResultBuilder.build();
            } else {
               this.mustSupport.setContextNode(XPathUtil.getXPathContextNode(this.target));
               if (this.deviateType == DeviateType.NOT_SUPPORTED) {
                  this.target.setSupported(false);
                  return validatorResultBuilder.build();
               } else {
                  if (this.config != null) {
                     validatorResultBuilder.merge(this.deviateConfig());
                  }

                  if (this.getMusts().size() > 0) {
                     validatorResultBuilder.merge(this.deviateMust());
                  }

                  if (this.defaults.size() > 0) {
                     validatorResultBuilder.merge(this.deviateDefault());
                  }

                  if (this.mandatory != null) {
                     validatorResultBuilder.merge(this.deviateMandatory());
                  }

                  if (this.maxElements != null) {
                     validatorResultBuilder.merge(this.deviateMaxElements());
                  }

                  if (this.minElements != null) {
                     validatorResultBuilder.merge(this.deviateMinElements());
                  }

                  if (this.uniques.size() > 0) {
                     validatorResultBuilder.merge(this.deviateUnique());
                  }

                  if (this.units != null) {
                     validatorResultBuilder.merge(this.deviateUnits());
                  }

                  if (this.type != null) {
                     validatorResultBuilder.merge(this.deviateType());
                  }

                  if (this.getUnknowns().size() > 0) {
                     validatorResultBuilder.merge(this.deviateUnknown());
                  }
               }
            }
         default:
            return validatorResultBuilder.build();
      }
   }

   public DeviateType getDeviateType() {
      return this.deviateType;
   }

   public SchemaNode getTarget() {
      return this.target;
   }

   public void setTarget(SchemaNode schemaNode) {
      this.target = schemaNode;
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

   public MaxElements getMaxElements() {
      return this.maxElements;
   }

   public MinElements getMinElements() {
      return this.minElements;
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

   public Type getType() {
      return this.type;
   }

   public Units getUnits() {
      return this.units;
   }

   public List<Unique> getUniques() {
      return Collections.unmodifiableList(this.uniques);
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.DEVIATE.getQName();
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

      if (this.type != null) {
         statements.add(this.type);
      }

      statements.addAll(this.mustSupport.getMusts());
      statements.addAll(this.uniques);
      if (this.units != null) {
         statements.add(this.units);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
