package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.*;

public class SchemaNodeContainerImpl implements SchemaNodeContainer {
   private List<SchemaNode> schemaNodes = new ArrayList();
   private YangContext yangContext;
   private SchemaNodeContainer self;

   public SchemaNodeContainerImpl(SchemaNodeContainer self) {
      this.self = self;
   }

   public YangContext getYangContext() {
      return this.yangContext;
   }

   public void setYangContext(YangContext yangContext) {
      this.yangContext = yangContext;
   }

   public List<SchemaNode> getSchemaNodeChildren() {
      return Collections.unmodifiableList(this.schemaNodes);
   }

   public ValidatorResult addSchemaNodeChild(SchemaNode schemaNode) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      if (!(schemaNode instanceof VirtualSchemaNode)) {
         SchemaNode child = this.getSchemaNodeChild(schemaNode.getIdentifier());
         if (child != null) {
            validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(child, schemaNode));
            schemaNode.setErrorStatement(true);
            return validatorResultBuilder.build();
         }

         if (this.self != null) {
            SchemaNode parent;
            if (schemaNode instanceof Action) {
               if (!(this.self instanceof SchemaNode)) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(schemaNode,
                          ErrorCode.ACTION_NOT_TOP.getFieldName()));
                  return validatorResultBuilder.build();
               }

               parent = (SchemaNode)this.self;
               SchemaNodeContainer closestAncestorNode = parent.getClosestAncestorNode();
               ValidatorRecordBuilder validatorRecordBuilder;
               if (!(this.self instanceof DataNode) && closestAncestorNode instanceof YangSchemaContext) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(schemaNode,
                          ErrorCode.ACTION_NOT_TOP.getFieldName()));
                  return validatorResultBuilder.build();
               }

               if (parent.getSchemaTreeType() == SchemaTreeType.RPCTREE || parent.getSchemaTreeType() == SchemaTreeType.NOTIFICATIONTREE) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(schemaNode,
                          ErrorCode.ACTION_IN_DATATREE.getFieldName()));
                  return validatorResultBuilder.build();
               }
            } else if (schemaNode instanceof Notification && this.self instanceof SchemaNode) {
               parent = (SchemaNode)this.self;
               if (parent.getSchemaTreeType() == SchemaTreeType.RPCTREE || parent.getSchemaTreeType() == SchemaTreeType.NOTIFICATIONTREE) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(schemaNode,
                          ErrorCode.NOTIFICATION_NOT_IN_DATATREE.getFieldName()));
                  return validatorResultBuilder.build();
               }
            }
         }
      }

      this.schemaNodes.add(schemaNode);
      if (this.self != null) {
         schemaNode.setParentSchemaNode(this.self);
      }

      if (!(schemaNode instanceof Rpc) && !(schemaNode instanceof Action)) {
         if (schemaNode instanceof Notification) {
            schemaNode.setSchemaTreeType(SchemaTreeType.NOTIFICATIONTREE);
         } else if (this.self != null && this.self instanceof SchemaNode) {
            schemaNode.setSchemaTreeType(((SchemaNode)this.self).getSchemaTreeType());
         }
      } else {
         schemaNode.setSchemaTreeType(SchemaTreeType.RPCTREE);
      }

      return validatorResultBuilder.build();
   }

   public ValidatorResult addSchemaNodeChildren(List<SchemaNode> schemaNodes) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator schemaNodeIterator = schemaNodes.iterator();

      while(schemaNodeIterator.hasNext()) {
         SchemaNode node = (SchemaNode)schemaNodeIterator.next();
         ValidatorResult result = this.addSchemaNodeChild(node);
         validatorResultBuilder.merge(result);
      }

      return validatorResultBuilder.build();
   }

   public SchemaNode getSchemaNodeChild(QName identifier) {
      try {
         Iterator schemaNodeIterator = this.schemaNodes.iterator();

         while(schemaNodeIterator.hasNext()) {
            SchemaNode schemaNode = (SchemaNode)schemaNodeIterator.next();
            if (schemaNode instanceof VirtualSchemaNode) {
               VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode)schemaNode;
               SchemaNode node = virtualSchemaNode.getSchemaNodeChild(identifier);
               if (node != null) {
                  return node;
               }
            } else if (schemaNode.getIdentifier().equals(identifier)) {
               return schemaNode;
            }
         }
      } catch (RuntimeException e) {
         e.printStackTrace();
      }

      return null;
   }

   public DataNode getDataNodeChild(QName identifier) {
      Iterator<SchemaNode> schemaNodeIterator = this.schemaNodes.iterator();
      while (schemaNodeIterator.hasNext()){
         SchemaNode schemaNode = schemaNodeIterator.next();
         if (!(schemaNode instanceof VirtualSchemaNode) && !(schemaNode instanceof Choice) && !(schemaNode instanceof Case) && !(schemaNode instanceof Input) && !(schemaNode instanceof Output)) {
            if (schemaNode.getIdentifier().equals(identifier)) {
               if(schemaNode instanceof DataNode){
                  return (DataNode) schemaNode;
               }
               return null;
            }
         } else {
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer)schemaNode;
            DataNode node = schemaNodeContainer.getDataNodeChild(identifier);
            if(node !=null){
               return node;
            }
         }
      }

      return null;
   }

   public List<DataNode> getDataNodeChildren() {
      List<DataNode> dataNodeChildren = new ArrayList();
      Iterator<SchemaNode> schemaNodeIterator = this.schemaNodes.iterator();
      while (schemaNodeIterator.hasNext()){
         SchemaNode schemaNode = schemaNodeIterator.next();
         if (!(schemaNode instanceof VirtualSchemaNode) && !(schemaNode instanceof Choice) && !(schemaNode instanceof Case) && !(schemaNode instanceof Input) && !(schemaNode instanceof Output)) {
            if (schemaNode instanceof DataNode) {
               dataNodeChildren.add((DataNode)schemaNode);
            }
         } else {
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer)schemaNode;
            dataNodeChildren.addAll(schemaNodeContainer.getDataNodeChildren());
         }
      }

      return dataNodeChildren;

   }

   public void removeSchemaNodeChild(QName identifier) {
      SchemaNode target = null;
      Iterator iterator = this.schemaNodes.iterator();

      while(iterator.hasNext()) {
         SchemaNode schemaNode = (SchemaNode)iterator.next();
         if (schemaNode instanceof VirtualSchemaNode) {
            VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode)schemaNode;
            virtualSchemaNode.removeSchemaNodeChild(identifier);
         } else if (schemaNode.getIdentifier().equals(identifier)) {
            target = schemaNode;
         }
      }

      if (null != target) {
         this.schemaNodes.remove(target);
      }

   }

   public void removeSchemaNodeChild(SchemaNode schemaNode) {
      if (this.schemaNodes.contains(schemaNode)) {
         this.schemaNodes.remove(schemaNode);
      } else {
         Iterator iterator = this.schemaNodes.iterator();

         while(iterator.hasNext()) {
            SchemaNode node = (SchemaNode)iterator.next();
            if (node instanceof VirtualSchemaNode) {
               VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode)node;
               virtualSchemaNode.removeSchemaNodeChild(schemaNode);
            }
         }

      }
   }

   public void removeSchemaNodeChildren(){
      schemaNodes.clear();
   }

   public SchemaNode getMandatoryDescendant() {
      Iterator<SchemaNode> schemaNodeIterator = this.schemaNodes.iterator();
      while(schemaNodeIterator.hasNext()) {
         SchemaNode child = schemaNodeIterator.next();
         if (!(child instanceof MandatorySupport) && !(child instanceof MultiInstancesDataNode)) {
            SchemaNode schemaNode;
            if (child instanceof Container) {
               Container container = (Container)child;
               if (container.getPresence() == null) {
                  schemaNode = container.getMandatoryDescendant();
                  if (schemaNode != null) {
                     return schemaNode;
                  }
               }
            } else if (child instanceof SchemaNodeContainer) {
               SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer)child;
               schemaNode = schemaNodeContainer.getMandatoryDescendant();
               if (schemaNode != null) {
                  return schemaNode;
               }
            }
         } else if (child.isMandatory()) {
            return child;
         }
      }

     return null;
   }
   @Override
   public int hashCode() {
      return Objects.hash(schemaNodes);
   }
}
