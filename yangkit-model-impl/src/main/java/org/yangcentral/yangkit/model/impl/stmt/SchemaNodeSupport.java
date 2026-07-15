package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.common.api.QName;
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

/**
 * Composable support for {@link SchemaNode} role.
 * Extracted from the former SchemaNodeImpl abstract base class so that
 * SchemaNode (a role/contract) no longer sits in the ontology inheritance
 * chain.  Classes that implement SchemaNode compose this class instead of
 * extending SchemaNodeImpl.
 */
public class SchemaNodeSupport {
   private final SchemaNode owner;
   private Boolean support;
   private SchemaNodeContainer schemaParent;
   private SchemaPath.Absolute schemaPath;
   private SchemaTreeType schemaTreeType;
   private boolean deviated;

   public SchemaNodeSupport(SchemaNode owner) {
      this.owner = owner;
      this.schemaTreeType = SchemaTreeType.DATATREE;
   }

   public boolean supported() {
      if (null == this.support) {
         SchemaNodeContainer parent = owner.getParentSchemaNode();
         return parent instanceof SchemaNode ? ((SchemaNode)parent).supported() : true;
      } else {
         return this.support;
      }
   }

   public void setSupported(boolean supported) {
      this.support = supported;
   }

   public SchemaPath.Absolute getSchemaPath() {
      return schemaPath;
   }

   public SchemaNodeContainer getParentSchemaNode() {
      return this.schemaParent;
   }

   public void setParentSchemaNode(SchemaNodeContainer schemaNodeContainer) {
      this.schemaParent = schemaNodeContainer;
   }

   public SchemaNodeContainer getClosestAncestorNode() {
      SchemaNodeContainer parent = owner.getParentSchemaNode();
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
      if (owner instanceof MandatorySupport) {
         MandatorySupport mandatorySupport = (MandatorySupport) owner;
         return mandatorySupport.getMandatory() != null ? mandatorySupport.getMandatory().getValue() : false;
      } else if (owner instanceof MultiInstancesDataNode) {
         MultiInstancesDataNode multiInstancesDataNode = (MultiInstancesDataNode) owner;
         if (multiInstancesDataNode.getMinElements() != null) {
            return multiInstancesDataNode.getMinElements().getValue() > 0;
         } else {
            return false;
         }
      } else {
         if (owner instanceof Container) {
            Container container = (Container) owner;
            if (container.getPresence() != null) {
               return false;
            }
         }

         if (owner instanceof VirtualSchemaNode || owner instanceof Container) {
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) owner;

            for (SchemaNode schemaNode : schemaNodeContainer.getSchemaNodeChildren()) {
               if (schemaNode.isMandatory()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean hasDefault() {
      if (owner instanceof Leaf) {
         Leaf leaf = (Leaf) owner;
         return leaf.getDefault() != null;
      } else if (owner instanceof LeafList) {
         LeafList leafList = (LeafList) owner;
         return leafList.getDefaults().size() > 0;
      } else if (owner instanceof Choice) {
         Choice choice = (Choice) owner;
         return choice.getDefault() != null;
      } else {
         if (owner instanceof SchemaNodeContainer) {
            SchemaNodeContainer schemaNodeContainer = (SchemaNodeContainer) owner;

            for (SchemaNode schemaNode : schemaNodeContainer.getSchemaNodeChildren()) {
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
         SchemaNodeContainer parent = owner.getParentSchemaNode();
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
      SchemaNodeContainer parent = owner.getParentSchemaNode();
      return parent.isSchemaTreeRoot() ? parent : ((SchemaNode)parent).getSchemaTreeRoot();
   }

   public QName getIdentifier() {
      // delegate to owner's YangStatement identity
      return new QName(owner.getContext().getNamespace(), ((YangStatementImpl) owner).getArgStr());
   }

   protected SchemaNode getRealSchemaNode(SchemaNode schemaNode) {
      if (schemaNode instanceof VirtualSchemaNode) {
         SchemaNodeContainer parent = schemaNode.getParentSchemaNode();
         return parent instanceof SchemaNode ? getRealSchemaNode((SchemaNode) parent) : null;
      } else {
         return schemaNode;
      }
   }

   private SchemaPath.Absolute getSchemaPath(SchemaNode schemaNode) {
      if (schemaNode.getSchemaPath() != null) {
         return schemaNode.getSchemaPath();
      }
      SchemaNodeContainer schemaNodeParent = schemaNode.getParentSchemaNode();
      if (!(schemaNodeParent instanceof SchemaNode)) {
         SchemaPath.Absolute path = new AbsoluteSchemaPath();
         path.addStep(owner.getIdentifier());
         return path;
      }

      SchemaNode parentSchemaNode = getRealSchemaNode((SchemaNode) schemaNodeParent);
      if (null == parentSchemaNode) {
         SchemaPath.Absolute path = new AbsoluteSchemaPath();
         path.addStep(owner.getIdentifier());
         return path;
      }
      SchemaPath parentSchemaPath = parentSchemaNode.getSchemaPath();
      if (parentSchemaPath == null) {
         parentSchemaPath = getSchemaPath(parentSchemaNode);
      }
      SchemaPath.Absolute path = new AbsoluteSchemaPath(parentSchemaPath.getPath());
      path.addStep(owner.getIdentifier());
      return path;
   }

   public ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      switch (phase) {
         case SCHEMA_TREE:
            if (!(owner instanceof VirtualSchemaNode)) {
               schemaPath = getSchemaPath(owner);
            }
            break;
         default:
            return validatorResultBuilder.build();
      }
      return validatorResultBuilder.build();
   }

   public boolean schemaEquals(Object obj) {
      if (!(obj instanceof SchemaNode)) {
         return false;
      }
      SchemaNode another = (SchemaNode) obj;
      return owner.getIdentifier().equals(another.getIdentifier());
   }
}
