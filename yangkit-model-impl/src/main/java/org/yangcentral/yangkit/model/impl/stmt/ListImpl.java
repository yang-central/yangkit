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
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.stmt.Key;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.MaxElements;
import org.yangcentral.yangkit.model.api.stmt.MinElements;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.OrderedBy;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.Unique;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.impl.schema.SchemaPathImpl;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListImpl extends ContainerDataNodeImpl implements YangList {
   private Key key;
   private MinElements minElements;
   private MaxElements maxElements;
   private OrderedBy orderedBy;
   private List<Unique> uniques = new ArrayList();

   public ListImpl(String argStr) {
      super(argStr);
   }

   public MinElements getMinElements() {
      return this.minElements;
   }

   public void setMinElements(MinElements minElements) {
      this.minElements = minElements;
   }

   public MaxElements getMaxElements() {
      return this.maxElements;
   }

   public void setMaxElements(MaxElements maxElements) {
      this.maxElements = maxElements;
   }

   public OrderedBy getOrderedBy() {
      return this.orderedBy;
   }

   public boolean isMandatory() {
      if (null == this.minElements) {
         return false;
      } else {
         return this.minElements.getValue() > 0;
      }
   }

   public boolean hasDefault() {
      return false;
   }

   public Key getKey() {
      return this.key;
   }

   public List<Unique> getUniques() {
      return this.uniques;
   }

   public Unique getUnique(String arg) {
      Iterator var2 = this.uniques.iterator();

      Unique unique;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         unique = (Unique)var2.next();
      } while(!unique.getArgStr().equals(arg));

      return unique;
   }

   public ValidatorResult addUnique(Unique unique) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      ValidatorResult validatorResult = this.validateUnique(unique);
      if (validatorResult.isOk()) {
         this.uniques.add(unique);
      }

      return validatorResultBuilder.build();
   }

   public void removeUnique(String unique) {
      int index = -1;

      for(int i = 0; i < this.uniques.size(); ++i) {
         if (((Unique)this.uniques.get(i)).getArgStr().equals(unique)) {
            index = i;
         }
      }

      if (index != -1) {
         this.uniques.remove(index);
      }

   }

   public ValidatorResult updateUnique(Unique unique) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      ValidatorResult validatorResult = this.validateUnique(unique);
      if (!validatorResult.isOk()) {
         validatorResultBuilder.merge(validatorResult);
         return validatorResultBuilder.build();
      } else {
         int index = -1;

         for(int i = 0; i < this.uniques.size(); ++i) {
            if (((Unique)this.uniques.get(i)).getArgStr().equals(unique.getArgStr())) {
               index = i;
            }
         }

         if (index != -1) {
            this.uniques.set(index, unique);
         }

         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult validateUnique(Unique unique) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      String[] uniStrs = unique.getArgStr().split(" ");
      int length = uniStrs.length;

      for(int i = 0; i < length; ++i) {
         String uniStr = uniStrs[i];
         uniStr = uniStr.trim();
         if (uniStr.length() != 0) {
            ValidatorRecordBuilder validatorRecordBuilder;
            try {
               SchemaPath path = SchemaPathImpl.from(this.getContext().getCurModule(), this, unique,uniStr);
               if (!(path instanceof SchemaPath.Descendant)) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(unique,ErrorCode.INVALID_SCHEMAPATH.getFieldName()));
               } else {
                  SchemaPath.Descendant descendantPath = (SchemaPath.Descendant)path;
                  SchemaNode schemaNode = descendantPath.getSchemaNode(this.getContext().getSchemaContext());
                  if (null != schemaNode && schemaNode instanceof Leaf) {
                     boolean bool = unique.addUniqueNode((Leaf)schemaNode);
                     if (!bool) {
                        validatorRecordBuilder = new ValidatorRecordBuilder();
                        validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                        validatorRecordBuilder.setSeverity(Severity.ERROR);
                        validatorRecordBuilder.setErrorPath(unique.getElementPosition());
                        validatorRecordBuilder.setBadElement(unique);
                        validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                        validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     }
                  } else {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(unique.getElementPosition());
                     validatorRecordBuilder.setBadElement(unique);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNIQUE_NODE_NOT_FOUND.toString(new String[]{"name=" + uniStr})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  }
               }
            } catch (ModelException var13) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.getElementPosition());
               validatorRecordBuilder.setBadElement(this);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SCHEMAPATH.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult validateKey(Key key) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      String[] keys = key.getArgStr().split(" ");
      String[] var4 = keys;
      int var5 = keys.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String keyStr = var4[var6];
         keyStr = keyStr.trim();
         if (keyStr.length() != 0) {
            SchemaNode child = this.getSchemaNodeChild(new QName(this.getContext().getNamespace(), keyStr));
            if (null != child && child instanceof Leaf) {
               boolean bool = this.getKey().addKeyNode((Leaf)child);
               if (!bool) {
                  ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(this.getKey().getElementPosition());
                  validatorRecordBuilder.setBadElement(this.getKey());
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  ((Leaf)child).setKey(true);
               }
            } else {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(this.getKey().getElementPosition());
               validatorRecordBuilder.setBadElement(this.getKey());
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.KEY_NODE_NOT_FOUND.toString(new String[]{"name=" + keyStr})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         }
      }

      return validatorResultBuilder.build();
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.LIST.getQName();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.UNIQUE.getQName());
      Iterator var3 = matched.iterator();

      while(var3.hasNext()) {
         YangStatement subStatement = (YangStatement)var3.next();
         this.uniques.add((Unique)subStatement);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.KEY.getQName());
      if (null != matched && matched.size() > 0) {
         this.key = (Key)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.MINELEMENTS.getQName());
      if (null != matched && matched.size() > 0) {
         this.minElements = (MinElements)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.MAXELEMENTS.getQName());
      if (null != matched && matched.size() > 0) {
         this.maxElements = (MaxElements)matched.get(0);
      }

      matched = this.getSubStatement(YangBuiltinKeyword.ORDEREDBY.getQName());
      if (null != matched && matched.size() > 0) {
         this.orderedBy = (OrderedBy)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult validateSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.validateSelf());
      if (this.isConfig() && this.key == null) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.LIST_NO_KEY.getFieldName()));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
      }

      if (this.getKey() != null) {
         List<Leaf> keyNodes = this.getKey().getkeyNodes();
         Iterator var3 = keyNodes.iterator();

         while(var3.hasNext()) {
            Leaf keyNode = (Leaf)var3.next();
            ValidatorRecordBuilder validatorRecordBuilder;
            if (this.isConfig() != keyNode.isConfig()) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setBadElement(keyNode);
               validatorRecordBuilder.setErrorPath(keyNode.getElementPosition());
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.KEY_CONFIG_ATTRIBUTE_DIFF_WITH_LIST.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else if (this.isActive() && !keyNode.isActive()) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setBadElement(keyNode);
               validatorRecordBuilder.setErrorPath(keyNode.getElementPosition());
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.KEY_NODE_INACTIVE.toString(new String[]{"name=" + keyNode.getArgStr()})));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         }
      }

      Iterator var10 = this.uniques.iterator();

      while(var10.hasNext()) {
         Unique unique = (Unique)var10.next();
         List<Leaf> uniqueNodes = unique.getUniqueNodes();
         Boolean config = null;
         Iterator var6 = uniqueNodes.iterator();

         while(var6.hasNext()) {
            Leaf uniqueNode = (Leaf)var6.next();
            if (config == null) {
               config = uniqueNode.isConfig();
            } else {
               ValidatorRecordBuilder validatorRecordBuilder;
               if (config != uniqueNode.isConfig()) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setBadElement(uniqueNode);
                  validatorRecordBuilder.setErrorPath(uniqueNode.getElementPosition());
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNIQUE_NODE_CONFIG_ATTRI_DIFF.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else if (this.isActive() && !uniqueNode.isActive()) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setBadElement(uniqueNode);
                  validatorRecordBuilder.setErrorPath(uniqueNode.getElementPosition());
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNIQUE_NODE_INACTIVE.toString(new String[]{"name=" + uniqueNode.getArgStr()})));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }
         }
      }

      return validatorResultBuilder.build();
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.buildSelf(phase));
      switch (phase) {
         case SCHEMA_TREE:
            if (this.getKey() != null) {
               validatorResultBuilder.merge(this.validateKey(this.getKey()));
            }

            Iterator var3 = this.getUniques().iterator();

            while(var3.hasNext()) {
               Unique unique = (Unique)var3.next();
               validatorResultBuilder.merge(this.validateUnique(unique));
            }
         default:
            ValidatorResult result = validatorResultBuilder.build();
            return result;
      }
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.key != null) {
         statements.add(this.key);
      }

      if (this.minElements != null) {
         statements.add(this.minElements);
      } else {
         MinElements newMinElements = new MinElementsImpl("0");
         newMinElements.setContext(new YangContext(this.getContext()));
         newMinElements.setElementPosition(this.getElementPosition());
         newMinElements.setParentStatement(this);
         newMinElements.init();
         newMinElements.build();
         statements.add(newMinElements);
      }

      if (this.maxElements != null) {
         statements.add(this.maxElements);
      } else {
         MaxElements newMaxElements = new MaxElementsImpl("unbounded");
         newMaxElements.setContext(new YangContext(this.getContext()));
         newMaxElements.setElementPosition(this.getElementPosition());
         newMaxElements.setParentStatement(this);
         newMaxElements.init();
         newMaxElements.build();
         statements.add(newMaxElements);
      }

      if (this.orderedBy != null) {
         statements.add(this.orderedBy);
      } else {
         OrderedBy newOrderedBy = new OrderedByImpl("system");
         newOrderedBy.setContext(new YangContext(this.getContext()));
         newOrderedBy.setElementPosition(this.getElementPosition());
         newOrderedBy.setParentStatement(this);
         newOrderedBy.init();
         newOrderedBy.build();
         statements.add(newOrderedBy);
      }

      statements.addAll(this.uniques);
      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
