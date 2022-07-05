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
   private List<SchemaNode> schemaNodes = new ArrayList();
   private Namespace namespace;

   public YangXPathRoot(Module module) {
      this.namespace = new Namespace(module.getMainModule().getNamespace().getUri(), module.getMainModule().getPrefix().getArgStr());
      List<SchemaNode> contextSchemaNodes = module.getContext().getSchemaContext().getSchemaNodeChildren();
      Iterator var3 = contextSchemaNodes.iterator();

      while(var3.hasNext()) {
         SchemaNode contextSchemaNode = (SchemaNode)var3.next();
         if (contextSchemaNode instanceof DataDefinition) {
            this.addSchemaNodeChild(contextSchemaNode);
         }
      }

   }

   public YangXPathRoot(SchemaNode schemaNode) {
      this.namespace = schemaNode.getContext().getNamespace();
      List<SchemaNode> contextSchemaNodes = schemaNode.getContext().getSchemaContext().getSchemaNodeChildren();
      Iterator var3 = contextSchemaNodes.iterator();

      while(var3.hasNext()) {
         SchemaNode contextSchemaNode = (SchemaNode)var3.next();
         if (contextSchemaNode instanceof DataDefinition) {
            this.addSchemaNodeChild(contextSchemaNode);
         }
      }

      SchemaNode notificationChild;
      List notificationChildren;
      Iterator var7;
      if (schemaNode instanceof Rpc) {
         notificationChildren = ((Rpc)schemaNode).getSchemaNodeChildren();
         var7 = notificationChildren.iterator();

         while(var7.hasNext()) {
            notificationChild = (SchemaNode)var7.next();
            this.addSchemaNodeChild(notificationChild);
         }
      }

      if (schemaNode instanceof Notification && schemaNode.getParentSchemaNode() instanceof Module) {
         notificationChildren = ((Notification)schemaNode).getSchemaNodeChildren();
         var7 = notificationChildren.iterator();

         while(var7.hasNext()) {
            notificationChild = (SchemaNode)var7.next();
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

   public ValidatorResult addSchemaNodeChildren(List<SchemaNode> list) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var3 = this.schemaNodes.iterator();

      while(var3.hasNext()) {
         SchemaNode node = (SchemaNode)var3.next();
         ValidatorResult result = this.addSchemaNodeChild(node);
         validatorResultBuilder.merge(result);
      }

      return validatorResultBuilder.build();
   }

   public SchemaNode getSchemaNodeChild(QName identifier) {
      try {
         Iterator var2 = this.schemaNodes.iterator();

         while(var2.hasNext()) {
            SchemaNode schemaNode = (SchemaNode)var2.next();
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
      } catch (RuntimeException var6) {
         var6.printStackTrace();
      }

      return null;
   }

   public DataNode getDataNodeChild(QName identifier) {
      Iterator var2 = this.schemaNodes.iterator();

      SchemaNode schemaNode;
      label35:
      do {
         DataNode node;
         do {
            if (!var2.hasNext()) {
               return null;
            }

            schemaNode = (SchemaNode)var2.next();
            if (!(schemaNode instanceof VirtualSchemaNode) && !(schemaNode instanceof Choice) && !(schemaNode instanceof Case) && !(schemaNode instanceof Input) && !(schemaNode instanceof Output)) {
               continue label35;
            }

            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer)schemaNode;
            node = schemaNodeContainer.getDataNodeChild(identifier);
         } while(node == null);

         return node;
      } while(!schemaNode.getIdentifier().equals(identifier));

      if (schemaNode instanceof DataNode) {
         return (DataNode)schemaNode;
      } else {
         return null;
      }
   }

   public List<DataNode> getDataNodeChildren() {
      List<DataNode> dataNodeChildren = new ArrayList();
      Iterator var2 = this.schemaNodes.iterator();

      while(true) {
         while(var2.hasNext()) {
            SchemaNode schemaNode = (SchemaNode)var2.next();
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
   }

   public void removeSchemaNodeChild(QName identifier) {
      SchemaNode target = null;
      Iterator var3 = this.schemaNodes.iterator();

      while(var3.hasNext()) {
         SchemaNode schemaNode = (SchemaNode)var3.next();
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
         Iterator var2 = this.schemaNodes.iterator();

         while(var2.hasNext()) {
            SchemaNode node = (SchemaNode)var2.next();
            if (node instanceof VirtualSchemaNode) {
               VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode)node;
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
