package org.yangcentral.yangkit.xpath.impl;

import org.yangcentral.yangkit.common.api.Namespace;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class YangXPathRoot implements SchemaNodeContainer {
   private final List<SchemaNode> schemaNodes = new ArrayList<>();
   private final Namespace namespace;

   public YangXPathRoot(Module module) {
      this.namespace = new Namespace(module.getMainModule().getNamespace().getUri(), module.getMainModule().getPrefix().getArgStr());
      List<SchemaNode> contextSchemaNodes = module.getContext().getSchemaContext().getSchemaNodeChildren();

      for (SchemaNode contextSchemaNode : contextSchemaNodes) {
         if (contextSchemaNode instanceof DataDefinition) {
            this.addSchemaNodeChild(contextSchemaNode);
         }
      }

   }

   public YangXPathRoot(SchemaNode schemaNode) {
      this.namespace = schemaNode.getContext().getNamespace();
      List<SchemaNode> contextSchemaNodes = schemaNode.getContext().getSchemaContext().getSchemaNodeChildren();

      for (SchemaNode contextSchemaNode : contextSchemaNodes) {
         if (contextSchemaNode instanceof DataDefinition) {
            this.addSchemaNodeChild(contextSchemaNode);
         }
      }

      List<SchemaNode> notificationChildren;
      if (schemaNode instanceof Rpc) {
         notificationChildren = ((Rpc)schemaNode).getSchemaNodeChildren();

         for (SchemaNode notificationChild : notificationChildren) {
            this.addSchemaNodeChild(notificationChild);
         }
      }

      if (schemaNode instanceof Notification && schemaNode.getParentSchemaNode() instanceof Module) {
         notificationChildren = ((Notification)schemaNode).getSchemaNodeChildren();

         for (SchemaNode notificationChild : notificationChildren) {
            this.addSchemaNodeChild(notificationChild);
         }
      }

   }

   public List<SchemaNode> getSchemaNodeChildren() {
      return Collections.unmodifiableList(this.schemaNodes);
   }

   public ValidatorResult addSchemaNodeChild(SchemaNode schemaNode) {
      this.schemaNodes.add(schemaNode);
      return (new ValidatorResultBuilder()).build();
   }

   public ValidatorResult addSchemaNodeChildren(List<SchemaNode> schemaNodes) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();

      for (SchemaNode node : this.schemaNodes) {
         ValidatorResult result = this.addSchemaNodeChild(node);
         validatorResultBuilder.merge(result);
      }

      return validatorResultBuilder.build();
   }

   public SchemaNode getSchemaNodeChild(QName identifier) {
      try {

         for (SchemaNode schemaNode : this.schemaNodes) {
            if (schemaNode.getIdentifier().equals(identifier)) {
               return schemaNode;
            }
         }
      } catch (RuntimeException var6) {
         var6.printStackTrace();
      }

      return null;
   }

   public DataNode getDataNodeChild(QName identifier) {
      for (SchemaNode schemaNode : this.schemaNodes) {
         if (schemaNode instanceof DataNode) {
            if(schemaNode.getIdentifier().equals(identifier)){
               return (DataNode) schemaNode;
            }

         }else {
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) schemaNode;
            DataNode node = schemaNodeContainer.getDataNodeChild(identifier);
            if (node != null) {
               return node;
            }
         }
      }

      return null;
   }

   public List<DataNode> getDataNodeChildren() {
      List<DataNode> dataNodeChildren = new ArrayList<>();
      for (SchemaNode schemaNode : this.schemaNodes) {
         if (schemaNode instanceof DataNode) {
            dataNodeChildren.add((DataNode) schemaNode);
         } else {
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) schemaNode;
            dataNodeChildren.addAll(schemaNodeContainer.getDataNodeChildren());
         }
      }

      return dataNodeChildren;
   }

   @Override
   public List<SchemaNode> getTreeNodeChildren() {
      List<SchemaNode> treeNodeChildren = new ArrayList<>();
      for (SchemaNode schemaNode : this.schemaNodes) {
         if (schemaNode instanceof TreeNode) {
            treeNodeChildren.add(schemaNode);
         } else {
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) schemaNode;
            treeNodeChildren.addAll(schemaNodeContainer.getTreeNodeChildren());
         }
      }

      return treeNodeChildren;
   }

   @Override
   public SchemaNode getTreeNodeChild(QName identifier) {
      for (SchemaNode schemaNode : this.schemaNodes) {
         if (schemaNode instanceof TreeNode) {
            if(schemaNode.getIdentifier().equals(identifier)){
               return  schemaNode;
            }

         }else {
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) schemaNode;
            SchemaNode node = schemaNodeContainer.getTreeNodeChild(identifier);
            if (node != null) {
               return node;
            }
         }
      }

      return null;
   }

   @Override
   public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) {
      List<SchemaNode> effectiveSchemaNodes = new ArrayList<>();
      for(SchemaNode schemaNode:schemaNodes){
         if(!schemaNode.isActive()){
            continue;
         }
         if(schemaNode.getContext().getNamespace() == null){
            continue;
         }
         if(!ignoreNamespace && !this.namespace.equals(schemaNode.getContext().getNamespace())){
            continue;
         }
         if(schemaNode instanceof VirtualSchemaNode){
            VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode) schemaNode;
            effectiveSchemaNodes.addAll(virtualSchemaNode.getEffectiveSchemaNodeChildren());
            continue;
         }
         effectiveSchemaNodes.add(schemaNode);

      }
      return effectiveSchemaNodes;
   }

   public void removeSchemaNodeChild(QName identifier) {
      SchemaNode target = null;

      for (SchemaNode schemaNode : this.schemaNodes) {
         if (schemaNode instanceof VirtualSchemaNode) {
            VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode) schemaNode;
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

         for (SchemaNode node : this.schemaNodes) {
            if (node instanceof VirtualSchemaNode) {
               VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode) node;
               virtualSchemaNode.removeSchemaNodeChild(schemaNode);
            }
         }

      }
   }

   @Override
   public SchemaNode getMandatoryDescendant() {
      for(SchemaNode child:schemaNodes){
         if((child instanceof MandatorySupport) || (child instanceof MultiInstancesDataNode)){
            if(child.isMandatory()){
               return child;
            }
         } else if(child instanceof Container){
            Container container = (Container) child;
            if(container.getPresence() == null){
               SchemaNode schemaNode = container.getMandatoryDescendant();
               if(schemaNode != null){
                  return schemaNode;
               }
            }
         } else {
            if(child instanceof SchemaNodeContainer){
               SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) child;
               SchemaNode schemaNode = schemaNodeContainer.getMandatoryDescendant();
               if(schemaNode != null){
                  return schemaNode;
               }
            }
         }
      }
      return null;
   }

   public boolean isSchemaTreeRoot() {
      return true;
   }
}
