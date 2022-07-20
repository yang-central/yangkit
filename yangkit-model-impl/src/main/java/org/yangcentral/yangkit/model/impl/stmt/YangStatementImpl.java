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
      ValidatorRecordBuilder validatorRecordBuilder;
      if (this instanceof Referencable) {
         Referencable referencable = (Referencable)this;
         if (!referencable.isReferenced()) {
            validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setBadElement(this);
            validatorRecordBuilder.setSeverity(Severity.WARNING);
            validatorRecordBuilder.setErrorPath(this.getElementPosition());
            ErrorCode errorCode = null;
            if (referencable instanceof Typedef) {
               errorCode = ErrorCode.UNUSED_TYPEDEF;
            } else if (referencable instanceof Grouping) {
               errorCode = ErrorCode.UNUSED_GROUPING;
            } else {
               errorCode = ErrorCode.UNUSED_IMPORT;
            }

            validatorRecordBuilder.setErrorMessage(new ErrorMessage(errorCode.toString(new String[]{"name=" + this.getArgStr()})));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         }
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult afterValidateChildren() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var2 = this.subElements.iterator();

      while(var2.hasNext()) {
         YangElement subElement = (YangElement)var2.next();
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
      Iterator var3 = this.subElements.iterator();

      while(var3.hasNext()) {
         YangElement element = (YangElement)var3.next();
         if (element instanceof YangStatement) {
            YangStatement subStatement = (YangStatement)element;
            if (subStatement.getYangKeyword().equals(keyword)) {
               matched.add(subStatement);
            }
         }
      }

      return matched;
   }

   public List<YangUnknown> getUnknowns() {
      return this.unknowns;
   }

   private List<YangUnknown> getUnknowns(String keyword) {
      List<YangUnknown> targetUnknowns = new ArrayList();
      Iterator var3 = this.unknowns.iterator();

      while(var3.hasNext()) {
         YangUnknown unknown = (YangUnknown)var3.next();
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
      Iterator var2 = this.subElements.iterator();

      while(var2.hasNext()) {
         YangElement subElement = (YangElement)var2.next();
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
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.CIRCLE_REFERNCE.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
         BuildPhase[] var2 = BuildPhase.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            BuildPhase phase = var2[var4];
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
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.CIRCLE_REFERNCE.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         this.isBuilding = true;
         YangStatementParserPolicy parserPolicy = YangStatementRegister.getInstance().getStatementParserPolicy(this.getYangKeyword());
         if (parserPolicy == null) {
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setBadElement(this);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(this.getElementPosition());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MISSING_CLASS_REG.toString(new String[]{"keyword=" + this.getYangKeyword().getQualifiedName()})));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            return validatorResultBuilder.build();
         } else {
            ValidatorResult selfResult;
            if (parserPolicy.getPhases().contains(buildPhase)) {
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
      Iterator var4 = this.subElements.iterator();

      while(var4.hasNext()) {
         YangElement subElement = (YangElement)var4.next();
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
         var4 = buildUnknowns.iterator();

         while(var4.hasNext()) {
            YangUnknown yangUnknown = (YangUnknown)var4.next();
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
                  Iterator var2 = this.subElements.iterator();

                  while(var2.hasNext()) {
                     YangElement subElement = (YangElement)var2.next();
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

         return true;
      }
   }

   public void setChildren(List<YangElement> yangElements) {
      this.clear();
      if (null != yangElements) {
         Iterator var2 = yangElements.iterator();

         while(var2.hasNext()) {
            YangElement yangElement = (YangElement)var2.next();
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
      this.subElements.clear();
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
      } else {
         assert this.context != null;

         YangSpecification yangSpecification = this.context.getYangSpecification();
         YangStatementDef statementDef = yangSpecification.getStatementDef(this.getYangKeyword());
         ValidatorRecordBuilder validatorRecordBuilder;
         if (null == statementDef) {
            validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorPath(this.position);
            validatorRecordBuilder.setBadElement(this);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName() + " keyword:" + this.getYangKeyword().getLocalName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            return validatorResultBuilder.build();
         } else {
            if (this instanceof Identifiable) {
               if (!ModelUtil.isIdentifier(this.getArgStr())) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorPath(this.position);
                  validatorRecordBuilder.setBadElement(this);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_IDENTIFIER.getFieldName() + " argument:" + this.getArgStr()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            } else if (this instanceof IdentifierRef && !ModelUtil.isIdentifierRef(this.getArgStr())) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorPath(this.position);
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_IDENTIFIER_REF.getFieldName() + " argument:" + this.getArgStr()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }

            Map<QName, Cardinality> subStatementInfos = statementDef.getSubStatementInfos();
            Iterator<QName> keys = subStatementInfos.keySet().iterator();

            while(keys.hasNext()) {
               QName key = (QName)keys.next();
               List<YangStatement> filteredStatements = this.getSubStatement(key);
               Cardinality cardinality = (Cardinality)subStatementInfos.get(key);
               if (!cardinality.isValid(filteredStatements.size())) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorPath(this.position);
                  validatorRecordBuilder.setBadElement(this);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.CARDINALITY_BROKEN.getFieldName() + " sub-statement:" + key.getLocalName() + "'s cardinality is:" + cardinality));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }

            Iterator var11 = this.subElements.iterator();

            while(true) {
               while(true) {
                  YangElement subElement;
                  do {
                     if (!var11.hasNext()) {
                        return validatorResultBuilder.build();
                     }

                     subElement = (YangElement)var11.next();
                  } while(!(subElement instanceof YangStatement));

                  if (subElement instanceof YangUnknown) {
                     YangUnknownParserPolicy unknownParserPolicy = YangUnknownRegister.getInstance().getUnknownInfo(((YangUnknown)subElement).getYangKeyword());
                     if (unknownParserPolicy == null || unknownParserPolicy.getParentStatements().size() <= 0) {
                        continue;
                     }

                     if (unknownParserPolicy.getParentStatement(this.getYangKeyword()) == null) {
                        validatorRecordBuilder = new ValidatorRecordBuilder();
                        validatorRecordBuilder.setErrorPath(subElement.getElementPosition());
                        validatorRecordBuilder.setBadElement((YangStatement)subElement);
                        validatorRecordBuilder.setSeverity(Severity.ERROR);
                        validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                        validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                        validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  }
               }
            }
         }
      }
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
         Iterator var3 = this.getSubElements().iterator();

         while(var3.hasNext()) {
            YangElement subElement = (YangElement)var3.next();
            if (subElement instanceof YangStatement) {
               YangStatement subStatement = (YangStatement)subElement;
               clonedStatement.addChild(subStatement.clone());
            }
         }

         ((YangStatementImpl)clonedStatement).clonedBy = this;
         return clonedStatement;
      } catch (NoSuchMethodException var6) {
         var6.printStackTrace();
      } catch (InvocationTargetException var7) {
         var7.printStackTrace();
      } catch (InstantiationException var8) {
         var8.printStackTrace();
      } catch (IllegalAccessException var9) {
         var9.printStackTrace();
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
      Iterator var2 = this.unknowns.iterator();

      while(var2.hasNext()) {
         YangUnknown unknown = (YangUnknown)var2.next();
         subStatements.add(unknown);
      }

      return subStatements;
   }
}
