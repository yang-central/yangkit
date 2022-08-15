package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.stmt.Action;
import org.yangcentral.yangkit.model.api.stmt.Choice;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.MandatorySupport;
import org.yangcentral.yangkit.model.api.stmt.MultiInstancesDataNode;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.Rpc;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.VirtualSchemaNode;
import org.yangcentral.yangkit.model.impl.schema.AbsoluteSchemaPath;

import java.util.Iterator;

public abstract class SchemaNodeImpl extends EntityImpl implements SchemaNode {
   private Boolean support;
   private SchemaNodeContainer schemaParent;
   private SchemaPath.Absolute schemaPath;
   private SchemaTreeType schemaTreeType;
   private boolean deviated;

   public SchemaNodeImpl(String argStr) {
      super(argStr);
      this.schemaTreeType = SchemaTreeType.DATATREE;
   }

   public boolean supported() {
      if (null == this.support) {
         SchemaNodeContainer parent = this.getParentSchemaNode();
         return parent instanceof SchemaNode ? ((SchemaNode)parent).supported() : true;
      } else {
         return this.support;
      }
   }

   public void setSupported(boolean supported) {
      this.support = supported;
   }

   public SchemaPath.Absolute getSchemaPath() {
      return this.schemaPath;
   }

   public SchemaNodeContainer getParentSchemaNode() {
      return this.schemaParent;
   }

   public void setParentSchemaNode(SchemaNodeContainer schemaNodeContainer) {
      this.schemaParent = schemaNodeContainer;
   }

   public SchemaNodeContainer getClosestAncestorNode() {
      SchemaNodeContainer parent = this.getParentSchemaNode();
      if (parent == null) {
         return null;
      } else if (parent instanceof DataNode) {
         return parent;
      } else if (!(parent instanceof Rpc) && !(parent instanceof Action) && !(parent instanceof Notification)) {
         return parent instanceof SchemaNode ? ((SchemaNode)parent).getClosestAncestorNode() : parent;
      } else {
         return parent;
      }
   }

   public boolean isMandatory() {
      if (this instanceof MandatorySupport) {
         MandatorySupport mandatorySupport = (MandatorySupport)this;
         return mandatorySupport.getMandatory() != null ? mandatorySupport.getMandatory().getValue() : false;
      } else if (this instanceof MultiInstancesDataNode) {
         MultiInstancesDataNode multiInstancesDataNode = (MultiInstancesDataNode)this;
         if (multiInstancesDataNode.getMinElements() != null) {
            return multiInstancesDataNode.getMinElements().getValue() > 0;
         } else {
            return false;
         }
      } else {
         if (this instanceof Container) {
            Container container = (Container)this;
            if (container.getPresence() != null) {
               return false;
            }
         }

         if (this instanceof VirtualSchemaNode || this instanceof Container) {
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer)this;
            Iterator schemaNodeIterator = schemaNodeContainer.getSchemaNodeChildren().iterator();

            while(schemaNodeIterator.hasNext()) {
               SchemaNode schemaNode = (SchemaNode)schemaNodeIterator.next();
               if (schemaNode.isMandatory()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean hasDefault() {
      if (this instanceof Leaf) {
         Leaf leaf = (Leaf)this;
         return leaf.getDefault() != null;
      } else if (this instanceof LeafList) {
         LeafList leafList = (LeafList)this;
         return leafList.getDefaults().size() > 0;
      } else if (this instanceof Choice) {
         Choice choice = (Choice)this;
         return choice.getDefault() != null;
      } else {
         if (this instanceof SchemaNodeContainer) {
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer)this;
            Iterator schemaNodeIterator = schemaNodeContainer.getSchemaNodeChildren().iterator();

            while(schemaNodeIterator.hasNext()) {
               SchemaNode schemaNode = (SchemaNode)schemaNodeIterator.next();
               if (schemaNode.hasDefault()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public SchemaTreeType getSchemaTreeType() {
      return this.schemaTreeType;
   }

   public void setSchemaTreeType(SchemaTreeType treeType) {
      this.schemaTreeType = treeType;
   }

   public boolean isAncestorNode(SchemaNode ancestor) {
      if (ancestor == null) {
         return false;
      } else {
         SchemaNodeContainer parent = this.getParentSchemaNode();
         if (!(parent instanceof SchemaNode)) {
            return false;
         } else {
            return parent == ancestor ? true : ((SchemaNode)parent).isAncestorNode(ancestor);
         }
      }
   }

   public boolean isDeviated() {
      return this.deviated;
   }

   public void setDeviated(boolean deviated) {
      this.deviated = deviated;
   }

   public SchemaNodeContainer getSchemaTreeRoot() {
      SchemaNodeContainer parent = this.getParentSchemaNode();
      return parent.isSchemaTreeRoot() ? parent : ((SchemaNode)parent).getSchemaTreeRoot();
   }

   protected SchemaNode getRealSchemaNode(SchemaNode schemaNode) {
      if (schemaNode instanceof VirtualSchemaNode) {
         SchemaNodeContainer parent = schemaNode.getParentSchemaNode();
         return parent instanceof SchemaNode ? this.getRealSchemaNode((SchemaNode)parent) : null;
      } else {
         return schemaNode;
      }
   }
   private SchemaPath.Absolute getSchemaPath(SchemaNode schemaNode){
      if(schemaNode.getSchemaPath() != null){
         return schemaNode.getSchemaPath();
      }
      SchemaNodeContainer schemaNodeParent = schemaNode.getParentSchemaNode();
      if(!(schemaNodeParent instanceof SchemaNode)){
         SchemaPath.Absolute schemaPath = new AbsoluteSchemaPath();
         schemaPath.addStep(getIdentifier());
         return schemaPath;
      }

      SchemaNode parentSchemaNode = getRealSchemaNode((SchemaNode) schemaNodeParent);
      if(null == parentSchemaNode){
         SchemaPath.Absolute schemaPath = new AbsoluteSchemaPath();
         schemaPath.addStep(getIdentifier());
         return schemaPath;
      }
      SchemaPath parentSchemaPath = parentSchemaNode.getSchemaPath();
      if(parentSchemaPath == null){
         parentSchemaPath = getSchemaPath(parentSchemaNode);
      }
      SchemaPath.Absolute schemaPath = new AbsoluteSchemaPath(parentSchemaPath.getPath());
      schemaPath.addStep(getIdentifier());
      return schemaPath;
   }
   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.buildSelf(phase));
      switch (phase) {
         case SCHEMA_TREE:
            if (!(this instanceof VirtualSchemaNode)) {
               schemaPath = getSchemaPath(this);
            }
            break;
         default:
            return validatorResultBuilder.build();
      }
      return validatorResultBuilder.build();
   }

   @Override
   public boolean equals(Object obj) {
      if( !super.equals(obj)){
         return false;
      }
      if(!(obj instanceof SchemaNode)){
         return false;
      }
      SchemaNode another = (SchemaNode) obj;
      return this.getIdentifier().equals(another.getIdentifier());
   }
}
