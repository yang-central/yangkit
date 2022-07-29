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
      Iterator uniqueIterator = this.uniques.iterator();

      Unique unique;
      do {
         if (!uniqueIterator.hasNext()) {
            return null;
         }

         unique = (Unique)uniqueIterator.next();
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
                        validatorResultBuilder.addRecord(ModelUtil.reportError(unique,
                                ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                     }
                  } else {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(unique,
                             ErrorCode.UNIQUE_NODE_NOT_FOUND.toString(new String[]{"name=" + uniStr})));
                  }
               }
            } catch (ModelException e) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                       ErrorCode.INVALID_SCHEMAPATH.getFieldName()));
            }
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult validateKey(Key key) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      String[] keys = key.getArgStr().split(" ");
      int length = keys.length;

      for(int i = 0; i < length; ++i) {
         String keyStr = keys[i];
         keyStr = keyStr.trim();
         if (keyStr.length() != 0) {
            SchemaNode child = this.getSchemaNodeChild(new QName(this.getContext().getNamespace(), keyStr));
            if (null != child && child instanceof Leaf) {
               boolean bool = this.getKey().addKeyNode((Leaf)child);
               if (!bool) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(this.getKey(),
                          ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
               } else {
                  ((Leaf)child).setKey(true);
               }
            } else {
               validatorResultBuilder.addRecord(ModelUtil.reportError(this.getKey(),
                       ErrorCode.KEY_NODE_NOT_FOUND.toString(new String[]{"name=" + keyStr})));
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
      this.uniques.clear();
      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.UNIQUE.getQName());
      Iterator statementIterator = matched.iterator();

      while(statementIterator.hasNext()) {
         YangStatement subStatement = (YangStatement)statementIterator.next();
         this.uniques.add((Unique)subStatement);
      }
      this.key = null;
      matched = this.getSubStatement(YangBuiltinKeyword.KEY.getQName());
      if (null != matched && matched.size() > 0) {
         this.key = (Key)matched.get(0);
      }
      this.minElements = null;
      matched = this.getSubStatement(YangBuiltinKeyword.MINELEMENTS.getQName());
      if (null != matched && matched.size() > 0) {
         this.minElements = (MinElements)matched.get(0);
      }
      this.maxElements = null;
      matched = this.getSubStatement(YangBuiltinKeyword.MAXELEMENTS.getQName());
      if (null != matched && matched.size() > 0) {
         this.maxElements = (MaxElements)matched.get(0);
      }
      this.orderedBy = null;
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
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.LIST_NO_KEY.getFieldName()));
      }

      if (this.getKey() != null) {
         List<Leaf> keyNodes = this.getKey().getkeyNodes();
         Iterator iterator = keyNodes.iterator();

         while(iterator.hasNext()) {
            Leaf keyNode = (Leaf)iterator.next();
            if (this.isConfig() != keyNode.isConfig()) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(keyNode,
                       ErrorCode.KEY_CONFIG_ATTRIBUTE_DIFF_WITH_LIST.getFieldName()));
            } else if (this.isActive() && !keyNode.isActive()) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(keyNode,
                       ErrorCode.KEY_NODE_INACTIVE.toString(new String[]{"name=" + keyNode.getArgStr()})));
            }
         }
      }

      Iterator uniqueIterator = this.uniques.iterator();

      while(uniqueIterator.hasNext()) {
         Unique unique = (Unique)uniqueIterator.next();
         List<Leaf> uniqueNodes = unique.getUniqueNodes();
         Boolean config = null;
         Iterator uniqueNodeIt = uniqueNodes.iterator();

         while(uniqueNodeIt.hasNext()) {
            Leaf uniqueNode = (Leaf)uniqueNodeIt.next();
            if (config == null) {
               config = uniqueNode.isConfig();
            } else {
               if (config != uniqueNode.isConfig()) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(uniqueNode,
                          ErrorCode.UNIQUE_NODE_CONFIG_ATTRI_DIFF.getFieldName()));
               } else if (this.isActive() && !uniqueNode.isActive()) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(uniqueNode,
                          ErrorCode.UNIQUE_NODE_INACTIVE.toString(new String[]{"name=" + uniqueNode.getArgStr()})));
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
         case SCHEMA_TREE:{
            List<YangStatement> leafs = this.getSubStatement(YangBuiltinKeyword.LEAF.getQName());
            for(YangStatement statement:leafs){
               Leaf leaf = (Leaf) statement;
               leaf.setKey(false);
            }
            if (this.getKey() != null) {
               validatorResultBuilder.merge(this.validateKey(this.getKey()));
            }

            Iterator uniqueIterator = this.getUniques().iterator();

            while(uniqueIterator.hasNext()) {
               Unique unique = (Unique)uniqueIterator.next();
               validatorResultBuilder.merge(this.validateUnique(unique));
            }
            break;
         }

         default:
            ValidatorResult result = validatorResultBuilder.build();
            return result;
      }
      return validatorResultBuilder.build();
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
