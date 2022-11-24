package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
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

   @Override
   protected void clearSelf() {
      this.deviateType = null;
      this.config = null;
      this.mustSupport.removeMusts();
      this.defaults.clear();
      this.mandatory = null;
      this.minElements = null;
      this.maxElements = null;
      this.type = null;
      this.uniques.clear();
      this.units = null;
      super.clearSelf();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());

      try {
         DeviateType deviateType = DeviateType.forValue(this.getArgStr());
         this.deviateType = deviateType;
      } catch (IllegalArgumentException e) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.INVALID_ARG.getFieldName()));
         return validatorResultBuilder.build();
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
      if (!(this.target instanceof ConfigSupport)) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(config,ErrorCode.NOT_SUPPORT_CONFIG.getFieldName()));
      } else {
         switch (this.deviateType) {
            case ADD:
               validatorResultBuilder.addRecord(ModelUtil.reportError(config,
                       ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=config"})));
               break;
            case DELETE:
               validatorResultBuilder.addRecord(ModelUtil.reportError(config,
                       ErrorCode.DEVIATE_NOT_ALLOWED.toString(new String[]{"property=config", "deviate=delete"}) ));
               break;
            case REPLACE:
               ConfigSupport configSupport = (ConfigSupport)this.target;
               configSupport.setConfig(this.config);
               SchemaNode schemaNode = (SchemaNode)configSupport;
               schemaNode.setDeviated(true);
         }

      }
      return validatorResultBuilder.build();
   }

   private ValidatorResult deviateMust() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof MustSupport)) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this.getMust(0),
                 ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
      } else {
         MustSupport mustSupport = (MustSupport)this.target;
         Iterator mustIterator = this.getMusts().iterator();

         while(mustIterator.hasNext()) {
            Must must = (Must)mustIterator.next();
            SchemaNode schemaNode;
            switch (this.deviateType) {
               case ADD: {
                  YangStatement matchedStatement = mustSupport.getMust(must.getArgStr());
                  if (matchedStatement != null) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(must,
                             ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=must"})));
                  } else {
                     Must orig = mustSupport.getMust(must.getArgStr());
                     if( orig != null){
                        mustSupport.removeMust(must.getArgStr());
                     }
                     validatorResultBuilder.merge(mustSupport.addMust(must));
                     schemaNode = (SchemaNode)mustSupport;
                     schemaNode.setDeviated(true);
                  }
                  break;
               }

               case DELETE: {
                  YangStatement matchedStatement = mustSupport.getMust(must.getArgStr());
                  if (matchedStatement == null) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(must,
                             ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=must"})));
                  } else {
                     mustSupport.removeMust(must.getArgStr());
                     schemaNode = (SchemaNode)mustSupport;
                     schemaNode.setDeviated(true);
                  }
                  break;
               }

               case REPLACE:{
                  YangStatement matchedStatement = mustSupport.getMust(must.getArgStr());
                  if (matchedStatement == null) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(must,
                             ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=must"})));
                  } else {
                     mustSupport.updateMust(must);
                     schemaNode = (SchemaNode)mustSupport;
                     schemaNode.setDeviated(true);
                  }
                  break;
               }

            }
         }

      }
      return validatorResultBuilder.build();
   }

   private ValidatorResult deviateLeafDefault() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Leaf leaf = (Leaf)this.target;
      ValidatorRecordBuilder validatorRecordBuilder;
      if (this.defaults.size() > 1) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(1),
                 ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
      }

      switch (this.deviateType) {
         case ADD:{
            Default defaultStmt = leaf.getDefault();
            if (defaultStmt != null ) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                       ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=default"})));
            } else {
               leaf.setDefault(this.defaults.get(0));
               leaf.setDeviated(true);
            }
            break;
         }
         case DELETE:{
            Default defaultStmt = leaf.getDefault();
            if (defaultStmt == null ) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                       ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"})));
            } else if (!defaultStmt.getArgStr().equals((this.defaults.get(0)).getArgStr())) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                       ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=default"})));
            } else {
               leaf.setDefault(null);
               leaf.setDeviated(true);
            }
            break;
         }

         case REPLACE:{
            Default aDefault = leaf.getDefault();
            if (aDefault == null) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                       ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"}) ));
            } else {
               leaf.setDefault(this.defaults.get(0));
               leaf.setDeviated(true);
            }
            break;
         }

      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult deviateChoiceDefault() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Choice choice = (Choice)this.target;
      if (this.defaults.size() > 1) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(1),
                 ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
      }
      Default aDefault = choice.getDefault();
      boolean bool;
      switch (this.deviateType) {
         case ADD:{
            if (aDefault != null) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                       ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=default"}) ));
            } else {
               bool = choice.setDefault((Default)this.defaults.get(0));
               if (!bool) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                          ErrorCode.MISSING_CASE.toString(new String[]{"name=" + ((Default)this.defaults.get(0)).getArgStr()})));
               } else {
                  choice.setDeviated(true);
               }
            }
            break;
         }

         case DELETE:
            if (aDefault == null) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                       ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"})));
            } else if (!aDefault.getArgStr().equals((this.defaults.get(0)).getArgStr())) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                       ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=default"})));
            } else {
               choice.setDefault(null);
               choice.setDeviated(true);
            }
            break;
         case REPLACE:
            if (choice.getDefault() == null) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                       ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"}) ));
            } else {
               bool = choice.setDefault((Default)this.defaults.get(0));
               if (!bool) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                          ErrorCode.MISSING_CASE.toString(new String[]{"name=" + ((Default)this.defaults.get(0)).getArgStr()})));
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
      Iterator defaultIterator = this.defaults.iterator();

      while(defaultIterator.hasNext()) {
         Default defl = (Default)defaultIterator.next();
         Default orig = leafList.getDefault(defl.getArgStr());
         switch (this.deviateType) {
            case ADD:{
               if ( orig != null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(defl,
                          ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=default"})));
               } else {
                  validatorResultBuilder.merge(leafList.addDefault(defl));
                  leafList.setDeviated(true);
               }
               break;
            }

            case DELETE:{
               if (orig == null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(defl,
                          ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"})));
               } else {
                  leafList.removeDefault(defl.getArgStr());
                  leafList.setDeviated(true);
               }
               break;
            }

            case REPLACE:{
               if (orig == null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(defl,
                          ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=default"})));
               } else {
                  validatorResultBuilder.merge(leafList.updateDefault(defl));
                  leafList.setDeviated(true);
               }
               break;
            }

         }
      }
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
         validatorResultBuilder.addRecord(ModelUtil.reportError(this.defaults.get(0),
                 ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult deviateMandatory() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof MandatorySupport)) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this.mandatory,
                 ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         MandatorySupport mandatorySupport = (MandatorySupport)this.target;
         Mandatory matched = mandatorySupport.getMandatory();
         switch (this.deviateType) {
            case ADD:{
               if (matched != null ) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.mandatory,
                          ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=mandatory"})));
               } else {
                  mandatorySupport.setMandatory(this.mandatory);
                  ((SchemaNode)mandatorySupport).setDeviated(true);
               }
               break;
            }

            case DELETE:{
               if (matched == null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.mandatory,
                          ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=mandatory"})));
               } else if (!matched.getArgStr().equals(this.mandatory.getArgStr())) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.mandatory,
                          ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=mandatory"})));
               } else {
                  mandatorySupport.setMandatory(null);
                  ((SchemaNode)mandatorySupport).setDeviated(true);
               }
               break;
            }

            case REPLACE:{
               if (matched == null ) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.mandatory,
                          ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=mandatory"})));
               } else {
                  mandatorySupport.setMandatory(this.mandatory);
                  ((SchemaNode)mandatorySupport).setDeviated(true);
               }
               break;
            }

         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateMaxElements() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof MultiInstancesDataNode)) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this.maxElements,
                 ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         MultiInstancesDataNode multiInstancesDataNode = (MultiInstancesDataNode)this.target;
         MaxElements matched = multiInstancesDataNode.getMaxElements();
         switch (this.deviateType) {
            case ADD:{
               if (matched != null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.maxElements,
                          ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=max-elements"})));
               } else {
                  multiInstancesDataNode.setMaxElements(this.maxElements);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
               break;
            }

            case DELETE:{
               if (matched == null ) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.maxElements,
                          ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=max-elements"})));
               } else if (!matched.getArgStr().equals(this.maxElements.getArgStr())) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.maxElements,
                          ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=max-elements"})));
               } else {
                  multiInstancesDataNode.setMaxElements(null);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
               break;
            }

            case REPLACE:{
               if (matched == null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.maxElements,
                          ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=max-elements"})));
               } else {
                  multiInstancesDataNode.setMaxElements(this.maxElements);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
               break;
            }

         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateMinElements() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof MultiInstancesDataNode)) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this.minElements,
                 ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         MultiInstancesDataNode multiInstancesDataNode = (MultiInstancesDataNode)this.target;
         MinElements matched = multiInstancesDataNode.getMinElements();
         switch (this.deviateType) {
            case ADD:{
               if (matched != null ) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.minElements,
                          ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=min-elements"})));
               } else {
                  multiInstancesDataNode.setMinElements(this.minElements);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
               break;
            }

            case DELETE:{
               if (matched == null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.minElements,
                          ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=min-elements"})));
               } else if (!matched.getArgStr().equals(this.minElements.getArgStr())) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.minElements,
                          ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=min-elements"})));
               } else {
                  multiInstancesDataNode.setMinElements(null);
                  ((SchemaNode)multiInstancesDataNode).setDeviated(true);
               }
               break;
            }

            case REPLACE:
               if (matched == null ) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.minElements,
                          ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=min-elements"})));
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
         validatorResultBuilder.addRecord(ModelUtil.reportError(this.uniques.get(0),
                 ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         YangList list = (YangList)this.target;
         Iterator uniqueIterator = this.uniques.iterator();

         while(uniqueIterator.hasNext()) {
            Unique unique = (Unique)uniqueIterator.next();
            Unique orig = list.getUnique(unique.getArgStr());
            switch (this.deviateType) {
               case ADD:{
                  if (orig != null) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(unique,
                             ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=unique"})));
                  } else {
                     validatorResultBuilder.merge(list.addUnique(unique));
                     list.setDeviated(true);
                  }
                  break;
               }

               case DELETE:{
                  if (orig == null) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(unique,
                             ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=unique"})));
                  } else {
                     list.removeUnique(unique.getArgStr());
                     list.setDeviated(true);
                  }
                  break;
               }

               case REPLACE:{
                  if (orig == null) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(unique,
                             ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=unique"})));
                  } else {
                     validatorResultBuilder.merge(list.updateUnique(unique));
                     list.setDeviated(true);
                  }
               }

            }
         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateUnits() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof TypedDataNode)) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(units,
                 ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         TypedDataNode typedDataNode = (TypedDataNode)this.target;
         Units matched = typedDataNode.getUnits();
         switch (this.deviateType) {
            case ADD:{
               if (matched != null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.units,
                          ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=units"})));
               } else {
                  typedDataNode.setUnits(this.units);
                  typedDataNode.setDeviated(true);
               }
               break;
            }

            case DELETE:{
               if (matched == null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(units,
                          ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=units"})));
               } else if (!matched.getArgStr().equals(this.units.getArgStr())) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(units,
                          ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name=units"})));
               } else {
                  typedDataNode.setUnits(null);
                  typedDataNode.setDeviated(true);
               }
               break;
            }

            case REPLACE:{
               if (matched == null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(units,
                          ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name=units"})));
               } else {
                  typedDataNode.setUnits(this.units);
                  typedDataNode.setDeviated(true);
               }
               break;
            }

         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateType() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(this.target instanceof TypedDataNode)) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this.type,
                 ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         TypedDataNode typedDataNode = (TypedDataNode)this.target;
         switch (this.deviateType) {
            case ADD:{
               validatorResultBuilder.addRecord(ModelUtil.reportError(type,
                       ErrorCode.PROPERTY_EXIST.toString(new String[]{"name=type"})));
               break;
            }

            case DELETE:{
               validatorResultBuilder.addRecord(ModelUtil.reportError(type,
                       ErrorCode.MANDATORY_CAN_NOT_DELETED.toString(new String[]{"name=type"})));
               break;
            }

            case REPLACE:{
               validatorResultBuilder.merge(typedDataNode.setType(this.type));
               typedDataNode.setDeviated(true);
               break;
            }

         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult deviateUnknown() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator unknownIterator = this.getUnknowns().iterator();
      while(unknownIterator.hasNext()) {
         YangUnknown yangUnknown = (YangUnknown)unknownIterator.next();
         boolean isMultiInstance = true;
         YangSpecification yangSpecification = this.target.getContext().getYangSpecification();
         YangStatementDef yangStatementDef = yangSpecification.getStatementDef(this.target.getYangKeyword());
         Cardinality cardinality = yangStatementDef.getSubStatementInfo(yangUnknown.getYangKeyword()).getCardinality();
         if(cardinality != null){
            if(cardinality.getMaxElements() <=1){
               isMultiInstance = false;
            }
         }
         if(!isMultiInstance){
            List<YangUnknown> matched = this.target.getUnknowns(yangUnknown.getYangKeyword());
            switch (this.deviateType) {
               case ADD:{
                  if(matched != null && !matched.isEmpty()){
                     validatorResultBuilder.addRecord(ModelUtil.reportError(yangUnknown,
                             ErrorCode.PROPERTY_EXIST.toString(new String[]{"name="+ yangUnknown.getYangKeyword().getQualifiedName()})));
                  } else {
                     List<YangUnknown> targetUnknowns = this.target.getUnknowns(yangUnknown.getYangKeyword());
                     for(YangUnknown targetUnknown:targetUnknowns){
                        this.target.getUnknowns().remove(targetUnknown);
                     }
                     this.target.getUnknowns().add(yangUnknown);
                     this.target.setDeviated(true);
                  }
                  break;
               }
               case DELETE:{
                  if(matched == null || matched.isEmpty()){
                     validatorResultBuilder.addRecord(ModelUtil.reportError(yangUnknown,
                             ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name="+ yangUnknown.getYangKeyword().getQualifiedName()})));
                  } else {
                     if(!matched.get(0).getArgStr().equals(yangUnknown.getArgStr())){
                        validatorResultBuilder.addRecord(ModelUtil.reportError(yangUnknown,
                                ErrorCode.PROPERTY_NOT_MATCH.toString(new String[]{"name="+ yangUnknown.getYangKeyword().getQualifiedName()})));
                     } else {
                        this.target.getUnknowns().remove(matched.get(0));
                        this.target.setDeviated(true);
                     }

                  }
                  break;
               }

               case REPLACE:{
                  if(matched == null || matched.isEmpty()){
                     validatorResultBuilder.addRecord(ModelUtil.reportError(yangUnknown,
                             ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name="+ yangUnknown.getYangKeyword().getQualifiedName()})));
                  } else {
                     this.target.getUnknowns().remove(matched.get(0));
                     this.target.getUnknowns().add(yangUnknown);
                     this.target.setDeviated(true);
                  }
                  break;
               }
            }

         } else {
            YangUnknown orig =  this.target.getUnknown(yangUnknown.getYangKeyword(),yangUnknown.getArgStr());
            switch (this.deviateType) {
               case ADD:{
                  if(orig != null){
                     validatorResultBuilder.addRecord(ModelUtil.reportError(yangUnknown,
                             ErrorCode.PROPERTY_EXIST.toString(new String[]{"name="+ yangUnknown.getYangKeyword().getQualifiedName()})));
                  } else {
                     YangUnknown targetUnknown = this.target.getUnknown(yangUnknown.getYangKeyword(),yangUnknown.getArgStr());
                     if(null != targetUnknown){
                        this.target.getUnknowns().remove(targetUnknown);
                     }
                     this.target.getUnknowns().add(yangUnknown);
                     this.target.setDeviated(true);
                  }
                  break;
               }
               case DELETE:{
                  if(orig == null){
                     validatorResultBuilder.addRecord(ModelUtil.reportError(yangUnknown,
                             ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name="+ yangUnknown.getYangKeyword().getQualifiedName()})));
                  } else {
                     this.target.getUnknowns().remove(orig);
                     this.target.setDeviated(true);
                  }
                  break;
               }

               case REPLACE:{
                  if(orig == null){
                     validatorResultBuilder.addRecord(ModelUtil.reportError(yangUnknown,
                             ErrorCode.PROPERTY_NOT_EXIST.toString(new String[]{"name="+ yangUnknown.getYangKeyword().getQualifiedName()})));
                  } else {
                     this.target.getUnknowns().remove(orig);
                     this.target.getUnknowns().add(yangUnknown);
                     this.target.setDeviated(true);
                  }
                  break;
               }
            }
         }


      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.buildSelf(phase));
      switch (phase) {
         case SCHEMA_MODIFIER:
            if (this.target == null) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.MISSING_TARGET.getFieldName()));
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

   public void setTarget(SchemaNode target) {
      this.target = target;
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

