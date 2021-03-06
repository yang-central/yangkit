package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.register.*;
import org.yangcentral.yangkit.util.ModelUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class YangStatementImpl implements YangStatement {
   private YangContext context;
   private Position position;
   private String argStr;
   private List<YangElement> subElements = new ArrayList();
   private List<YangUnknown> unknowns = new ArrayList();
   protected boolean isBuilt;
   protected boolean isBuilding;
   protected boolean isValidated;
   protected boolean isValidating;
   private boolean init;
   private BuildPhase buildPhase;
   private YangStatement parentStmt;
   private ValidatorResult validatorResult;
   private Map<BuildPhase, ValidatorResult> phaseResultMap = new ConcurrentHashMap();
   private boolean isError = false;
   private YangStatement clonedBy;

   public YangStatementImpl(String argStr) {
      this.argStr = argStr;
   }

   public ValidatorResult afterValidate() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      ValidatorResult selfResult = this.afterValidateSelf();
      validatorResultBuilder.merge(selfResult);
      validatorResultBuilder.merge(this.afterValidateChildren());
      return validatorResultBuilder.build();
   }

   protected ValidatorResult afterValidateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (this instanceof Referencable) {
         Referencable referencable = (Referencable)this;
         if(!referencable.getReferencedBy().isEmpty()){
            List<YangStatement> unAvailableStatements = new ArrayList<>();
            for(int i = 0; i < referencable.getReferencedBy().size();i++){
               YangStatement ref = referencable.getReferencedBy().get(i);
               if(!ModelUtil.isAvailableStatement(ref)){
                  unAvailableStatements.add(ref);
               }
            }
            for(YangStatement ref:unAvailableStatements){
               referencable.getReferencedBy().remove(ref);
            }
         }
         if (!referencable.isReferenced()) {
            ErrorCode errorCode = null;
            if (referencable instanceof Typedef) {
               errorCode = ErrorCode.UNUSED_TYPEDEF;
            } else if (referencable instanceof Grouping) {
               errorCode = ErrorCode.UNUSED_GROUPING;
            } else {
               errorCode = ErrorCode.UNUSED_IMPORT;
            }
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,Severity.WARNING,ErrorTag.BAD_ELEMENT,
                    errorCode.toString(new String[]{"name=" + this.getArgStr()})));
         }
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult afterValidateChildren() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator elementIterator = this.subElements.iterator();

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangStatement) {
            YangStatement statement = (YangStatement)subElement;
            if (!statement.isErrorStatement()) {
               validatorResultBuilder.merge(statement.afterValidate());
            }
         }
      }

      return validatorResultBuilder.build();
   }

   public void setArgStr(String argStr) {
      this.argStr = argStr;
      this.init = false;
      this.setBuilt(false);
      this.isValidated = false;
   }

   public Position getElementPosition() {
      return this.position;
   }

   public void setElementPosition(Position position) {
      this.position = position;
   }

   public String getArgStr() {
      return this.argStr;
   }

   public List<YangElement> getSubElements() {
      return this.subElements;
   }

   public List<YangStatement> getSubStatement(QName keyword) {
      List<YangStatement> matched = new ArrayList();
      Iterator yangElementIterator = this.subElements.iterator();

      while(yangElementIterator.hasNext()) {
         YangElement element = (YangElement)yangElementIterator.next();
         if (element instanceof YangStatement) {
            YangStatement subStatement = (YangStatement)element;
            if (subStatement.getYangKeyword().equals(keyword)) {
               matched.add(subStatement);
            }
         }
      }

      return matched;
   }

   @Override
   public YangStatement getSubStatement(QName keyword, String arg) {
      List<YangStatement> matched = getSubStatement(keyword);
      if(matched == null || matched.isEmpty()){
         return null;
      }
      for(YangStatement statement:matched){
         if(statement.getArgStr().equals(arg)){
            return statement;
         }
      }
      return null;
   }

   public List<YangUnknown> getUnknowns() {
      return this.unknowns;
   }

   @Override
   public List<YangUnknown> getUnknowns(QName keyword) {
      List<YangUnknown> matched = new ArrayList<>();
      for(YangUnknown unknown:unknowns){
         if (unknown.getYangKeyword().equals(keyword)) {
            matched.add(unknown);
         }
      }
      return matched;
   }

   @Override
   public YangUnknown getUnknown(QName keyword, String arg) {
      if(arg == null){
         List<YangUnknown> unknownList = getUnknowns(keyword);
         if(unknownList.isEmpty()){
            return null;
         }
         return unknownList.get(0);
      }
      for(YangUnknown unknown:unknowns){
         if(unknown.getYangKeyword() == null || unknown.getArgStr() == null){
            continue;
         }
         if (unknown.getYangKeyword().equals(keyword) && unknown.getArgStr().equals(arg)) {
            return unknown;
         }
      }
      return null;
   }

   private List<YangUnknown> getUnknowns(String keyword) {
      List<YangUnknown> targetUnknowns = new ArrayList();
      Iterator unknownIterator = this.unknowns.iterator();

      while(unknownIterator.hasNext()) {
         YangUnknown unknown = (YangUnknown)unknownIterator.next();
         if (unknown.getKeyword().equals(keyword)) {
            targetUnknowns.add(unknown);
         }
      }

      return targetUnknowns;
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      return validatorResultBuilder.build();
   }

   protected ValidatorResult validateChildren() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator elementIterator = this.subElements.iterator();

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangStatement) {
            YangStatement statement = (YangStatement)subElement;
            if (!statement.isErrorStatement()) {
               validatorResultBuilder.merge(statement.validate());
            }
         }
      }

      return validatorResultBuilder.build();
   }

   public ValidatorResult validate() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (this.isValidated()) {
         return validatorResultBuilder.build();
      } else if (this.isValidating) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.CIRCLE_REFERNCE.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         this.isValidating = true;
         ValidatorResult selfResult = this.validateSelf();
         this.setValidateResult(selfResult);
         validatorResultBuilder.merge(selfResult);
         validatorResultBuilder.merge(this.validateChildren());
         ValidatorResult result = validatorResultBuilder.build();
         if (result.isOk()) {
            this.isValidated = true;
         }

         this.isValidating = false;
         return result;
      }
   }

   public synchronized ValidatorResult build() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (this.isBuilt()) {
         return validatorResultBuilder.build();
      } else {
         BuildPhase[] buildPhases = BuildPhase.values();
         int length = buildPhases.length;

         for(int i = 0; i < length; ++i) {
            BuildPhase phase = buildPhases[i];
            ValidatorResult phaseResult = this.build(phase);
            validatorResultBuilder.merge(phaseResult);
            if (!phaseResult.isOk()) {
               break;
            }
         }

         ValidatorResult result = validatorResultBuilder.build();
         if (result.isOk()) {
            this.setBuilt(true);
         }

         return result;
      }
   }

   public synchronized ValidatorResult build(BuildPhase buildPhase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (this.isBuilt()) {
         return validatorResultBuilder.build();
      } else if (this.isBuilding) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.CIRCLE_REFERNCE.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         this.isBuilding = true;
         YangStatementParserPolicy parserPolicy = YangStatementRegister.getInstance().getStatementParserPolicy(this.getYangKeyword());
         if (parserPolicy == null && !(this instanceof DefaultYangUnknown)) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.MISSING_CLASS_REG.toString(new String[]{"keyword=" + this.getYangKeyword().getQualifiedName()})));
            return validatorResultBuilder.build();
         } else {
            ValidatorResult selfResult;
            if ((parserPolicy!= null && parserPolicy.getPhases().contains(buildPhase))
            || (this instanceof DefaultYangUnknown)) {
               if (this.phaseResultMap.containsKey(buildPhase)) {
                  validatorResultBuilder.merge((ValidatorResult)this.phaseResultMap.get(buildPhase));
               } else {
                  this.buildPhase = buildPhase;
                  selfResult = this.buildSelf(buildPhase);
                  this.phaseResultMap.put(buildPhase, selfResult);
                  this.setValidateResult(selfResult);
                  validatorResultBuilder.merge(selfResult);
               }
            }

            validatorResultBuilder.merge(this.buildChildren(buildPhase));
            this.isBuilding = false;
            selfResult = validatorResultBuilder.build();
            return selfResult;
         }
      }
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      switch (phase) {
         case GRAMMAR:
            if (this.getContext() != null && this.getContext().getNamespace() == null) {
               this.getContext().setNamespace(ModelUtil.getNamespace(this.getContext().getCurModule()));
            }
         default:
            return (new ValidatorResultBuilder()).build();
      }
   }

   protected ValidatorResult buildChildren(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      List<YangUnknown> buildUnknowns = null;
      Iterator elementIterator = this.subElements.iterator();

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (!(subElement instanceof YangComment)) {
            if (subElement instanceof YangUnknownBlock) {
               if (BuildPhase.GRAMMAR == phase) {
                  YangUnknown unknown = ((YangUnknownBlock)subElement).build(this.getContext());
                  if (buildUnknowns == null) {
                     buildUnknowns = new ArrayList();
                  }

                  buildUnknowns.add(unknown);
               }
            } else {
               YangStatement statement = (YangStatement)subElement;
               if (!statement.isErrorStatement()) {
                  validatorResultBuilder.merge(statement.build(phase));
               }
            }
         }
      }

      if (buildUnknowns != null) {
         elementIterator = buildUnknowns.iterator();

         while(elementIterator.hasNext()) {
            YangUnknown yangUnknown = (YangUnknown)elementIterator.next();
            this.addChild(yangUnknown);
            yangUnknown.setContext(new YangContext(this.getContext()));
            yangUnknown.init();
            validatorResultBuilder.merge(yangUnknown.build(phase));
         }
      }

      return validatorResultBuilder.build();
   }

   public ValidatorResult getValidateResult() {
      return this.validatorResult;
   }

   public void setValidateResult(ValidatorResult validatorResult) {
      if (this.validatorResult == null) {
         this.validatorResult = validatorResult;
      } else {
         ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
         validatorResultBuilder.merge(this.validatorResult);
         validatorResultBuilder.merge(validatorResult);
         this.validatorResult = validatorResultBuilder.build();
      }
   }

   public boolean isBuilt() {
      if (this.isBuilt) {
         return true;
      } else if (!this.isInit()) {
         return false;
      } else {
         YangStatementParserPolicy parserPolicy = YangStatementRegister.getInstance().getStatementParserPolicy(this.getYangKeyword());
         if (parserPolicy == null) {
            return false;
         } else if (this.getValidateResult() != null && this.getValidateResult().isOk()) {
            if (!parserPolicy.getPhases().isEmpty() && !parserPolicy.isLastPhase(this.buildPhase)) {
               return false;
            } else {
               if (this.subElements.size() > 0) {
                  Iterator elementIterator = this.subElements.iterator();

                  while(elementIterator.hasNext()) {
                     YangElement subElement = (YangElement)elementIterator.next();
                     if (subElement instanceof YangStatement) {
                        YangStatement subStatement = (YangStatement)subElement;
                        if (!subStatement.isBuilt()) {
                           return false;
                        }
                     }
                  }
               }

               this.isBuilt = true;
               return this.isBuilt;
            }
         } else {
            return false;
         }
      }
   }

   public boolean isBuilding() {
      return this.isBuilding;
   }

   public boolean isValidated() {
      return this.isValidated;
   }

   public void setBuilt(boolean built) {
      this.isBuilt = built;
   }

   public boolean addChild(YangElement yangElement) {
      boolean result = this.subElements.add(yangElement);
      if (!result) {
         return false;
      } else {
         if (yangElement instanceof YangStatement) {
            YangStatementImpl yangStatement = (YangStatementImpl)yangElement;
            yangStatement.setParentStatement(this);
         }

         this.setBuilt(false);
         this.init = false;
         this.isValidated = false;
         return true;
      }
   }

   public boolean addChild(int index, YangElement yangElement) {
      this.subElements.add(index, yangElement);
      if (yangElement instanceof YangStatement) {
         YangStatementImpl yangStatement = (YangStatementImpl)yangElement;
         yangStatement.setParentStatement(this);
      }

      this.setBuilt(false);
      this.init = false;
      this.isValidated = false;
      return true;
   }

   public boolean updateChild(int index, YangElement yangElement) {
      if (this.subElements.get(index) == null) {
         return false;
      } else {
         this.subElements.set(index, yangElement);
         if (yangElement instanceof YangStatement) {
            YangStatementImpl yangStatement = (YangStatementImpl)yangElement;
            yangStatement.setParentStatement(this);
         }

         this.setBuilt(false);
         this.init = false;
         this.isValidated = false;
         return true;
      }
   }

   public boolean updateChild(YangStatement statement) {
      int index = -1;

      for(int i = 0; i < this.subElements.size(); ++i) {
         YangElement element = (YangElement)this.subElements.get(i);
         if (element instanceof YangStatement) {
            YangStatement yangStatement = (YangStatement)element;
            if (yangStatement.equals(statement)) {
               index = i;
            }
         }
      }

      if (-1 == index) {
         return false;
      } else {
         return this.updateChild(index, statement);
      }
   }

   public boolean removeChild(YangElement yangElement) {
      int pos = -1;

      for(int i = 0; i < this.subElements.size(); ++i) {
         YangElement subElement = (YangElement)this.subElements.get(i);
         if (subElement == yangElement) {
            pos = i;
            break;
         }
      }

      if (-1 == pos) {
         return false;
      } else {
         YangElement element = (YangElement)this.subElements.remove(pos);
         if (element instanceof YangStatement) {
            YangStatementImpl statement = (YangStatementImpl)element;
            statement.setParentStatement((YangStatement)null);
         }
         this.setBuilt(false);
         this.init = false;
         this.isValidated = false;
         return true;
      }
   }

   public void setChildren(List<YangElement> yangElements) {
      this.subElements.clear();
      if (null != yangElements) {
         Iterator elementIterator = yangElements.iterator();

         while(elementIterator.hasNext()) {
            YangElement yangElement = (YangElement)elementIterator.next();
            if (null != yangElement) {
               this.addChild(yangElement);
            }
         }

      }
   }

   public YangStatement getParentStatement() {
      return this.parentStmt;
   }

   public void setParentStatement(YangStatement parentStatement) {
      this.parentStmt = parentStatement;
   }

   protected void clear() {
      this.unknowns.clear();
      this.isBuilt = false;
      this.init = false;
      this.isValidated = false;
      this.isBuilding = false;
      this.isValidating = false;
      this.buildPhase = null;
      this.validatorResult = null;
      this.isError = false;
      this.clonedBy = null;
   }

   public YangContext getContext() {
      return this.context;
   }

   public void setContext(YangContext context) {
      this.context = context;
      context.setSelf(this);
   }

   protected ValidatorResult initChildren() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      unknowns.clear();
      for(YangElement subElement:subElements){
         if(subElement == null){
            continue;
         }
         if(!(subElement instanceof YangStatement)){
            continue;
         }
         if(((YangStatement) subElement).isErrorStatement()){
            continue;
         }
         YangStatement yangStatement = (YangStatement) subElement;
         YangContext childContext = new YangContext(context);
         if(this instanceof Grouping){
            childContext.setCurGrouping((Grouping) this);
         }
         yangStatement.setContext(childContext);
         if (subElement instanceof YangUnknown){
            unknowns.add((YangUnknown) subElement);
         }
         validatorResultBuilder.merge(yangStatement.init());
      }
      return validatorResultBuilder.build();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (this instanceof YangUnknown) {
         return validatorResultBuilder.build();
      }
      YangSpecification yangSpecification = this.context.getYangSpecification();
      YangStatementDef statementDef = yangSpecification.getStatementDef(this.getYangKeyword());
      ValidatorRecordBuilder validatorRecordBuilder;
      if (null == statementDef) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName() + " keyword:" + this.getYangKeyword().getLocalName() ));
         return validatorResultBuilder.build();
      }

         if (this instanceof Identifiable) {
            if (!ModelUtil.isIdentifier(this.getArgStr())) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.INVALID_IDENTIFIER.getFieldName() + " argument:" + this.getArgStr()));
            }
         } else if (this instanceof IdentifierRef && !ModelUtil.isIdentifierRef(this.getArgStr())) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                    ErrorCode.INVALID_IDENTIFIER_REF.getFieldName() + " argument:" + this.getArgStr() ));
         }

         Map<QName, Cardinality> subStatementInfos = statementDef.getSubStatementInfos();
         Iterator<QName> keys = subStatementInfos.keySet().iterator();

         while(keys.hasNext()) {
            QName key = (QName)keys.next();
            List<YangStatement> filteredStatements = this.getSubStatement(key);
            Cardinality cardinality = (Cardinality)subStatementInfos.get(key);
            if (!cardinality.isValid(filteredStatements.size())) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.CARDINALITY_BROKEN.getFieldName() + " sub-statement:" + key.getLocalName() + "'s cardinality is:" + cardinality ));
            }
         }

         Iterator<YangElement> elementIterator = this.subElements.iterator();
         while (elementIterator.hasNext()){
            YangElement subElement = elementIterator.next();
            if(!(subElement instanceof YangStatement)){
               continue;
            }
            if (subElement instanceof YangUnknown) {
               YangUnknownParserPolicy unknownParserPolicy = YangUnknownRegister.getInstance().getUnknownInfo(((YangUnknown)subElement).getYangKeyword());
               if (unknownParserPolicy == null || unknownParserPolicy.getParentStatements().size() <= 0) {
                  continue;
               }

               if (unknownParserPolicy.getParentStatement(this.getYangKeyword()) == null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError((YangStatement)subElement,
                          ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                  continue;
               }
            }
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            if (!subStatementInfos.containsKey(builtinStatement.getYangKeyword())) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               continue;
            }

         }
         return validatorResultBuilder.build();
   }

   public ValidatorResult init() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      ValidatorResult result;
      if (this.getValidateResult() == null) {
         result = this.initSelf();
         this.setValidateResult(result);
         validatorResultBuilder.merge(result);
      }

      validatorResultBuilder.merge(this.initChildren());
      result = validatorResultBuilder.build();
      if (result.isOk()) {
         this.init = true;
      }

      return result;
   }

   public boolean isInit() {
      return this.init;
   }

   public <T extends YangStatement> T getSelf() {
      return (T) this;
   }

   public YangStatement clonedBy() {
      return this.clonedBy;
   }

   public YangStatement clone() {
      try {
         Constructor<? extends YangStatement> constructor = null;
         YangStatement clonedStatement = null;
         if (this instanceof YangUnknown) {
            constructor = this.getClass().getConstructor(String.class, String.class);
            clonedStatement = (YangStatement)constructor.newInstance(((YangUnknown)this).getKeyword(), this.getArgStr());
         } else {
            constructor = this.getClass().getConstructor(String.class);
            clonedStatement = (YangStatement)constructor.newInstance(this.getArgStr());
         }

         clonedStatement.setElementPosition(this.getElementPosition());
         Iterator elementIterator = this.getSubElements().iterator();

         while(elementIterator.hasNext()) {
            YangElement subElement = (YangElement)elementIterator.next();
            if (subElement instanceof YangStatement) {
               YangStatement subStatement = (YangStatement)subElement;
               clonedStatement.addChild(subStatement.clone());
            }
         }

         ((YangStatementImpl)clonedStatement).clonedBy = this;
         return clonedStatement;
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }

      return null;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      QName keyword = this.getYangKeyword();
      if (keyword.getNamespace().equals(Yang.NAMESPACE.getUri())) {
         sb.append(keyword.getLocalName());
      } else {
         sb.append(keyword.getQualifiedName());
      }

      if (this.getArgStr() != null) {
         sb.append(" ");
         sb.append(this.getArgStr());
      }

      return sb.toString();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof YangStatementImpl)) {
         return false;
      } else {
         YangStatementImpl that = (YangStatementImpl)o;
         return Objects.equals(this.getArgStr(), that.getArgStr()) && Objects.equals(this.getYangKeyword(), that.getYangKeyword());
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.getArgStr()});
   }

   public boolean isErrorStatement() {
      return this.isError;
   }

   public void setErrorStatement(boolean errorStatement) {
      this.isError = errorStatement;
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> subStatements = new ArrayList();
      Iterator unknownIterator = this.unknowns.iterator();

      while(unknownIterator.hasNext()) {
         YangUnknown unknown = (YangUnknown)unknownIterator.next();
         subStatements.add(unknown);
      }

      return subStatements;
   }
}
