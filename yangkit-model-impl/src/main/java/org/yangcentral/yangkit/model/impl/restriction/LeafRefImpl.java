package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.type.Path;
import org.yangcentral.yangkit.model.api.stmt.type.RequireInstance;
import org.yangcentral.yangkit.model.impl.stmt.type.RequireInstanceImpl;

public class LeafRefImpl extends RestrictionImpl<Object> implements LeafRef {
   private Path path;
   private RequireInstance requireInstance;
   private TypedDataNode referencedNode;

   public LeafRefImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public LeafRefImpl(YangContext context) {
      super(context);
   }

   public Path getPath() {
      return this.path;
   }

   public Path getEffectivePath() {
      return this.getDerived() != null ? ((LeafRef)this.getDerived().getType().getRestriction()).getEffectivePath() : this.getPath();
   }

   public RequireInstance getRequireInstance() {
      return this.requireInstance;
   }

   public RequireInstance getEffectiveRequireInstance() {
      RequireInstance requireInstance = this.getRequireInstance();
      if (requireInstance != null) {
         return requireInstance;
      } else if (this.getDerived() != null) {
         LeafRef instanceIdentifier = (LeafRef)this.getDerived().getType().getRestriction();
         return instanceIdentifier.getEffectiveRequireInstance();
      } else {
         RequireInstance newRequireInstance = new RequireInstanceImpl("true");
         newRequireInstance.setContext(new YangContext(this.getContext()));
         newRequireInstance.setElementPosition(this.getContext().getSelf().getElementPosition());
         newRequireInstance.setParentStatement(this.getContext().getSelf());
         newRequireInstance.init();
         newRequireInstance.build();
         return newRequireInstance;
      }
   }

   public void setPath(Path path) {
      this.path = path;
   }

   public void setRequireInstance(RequireInstance requireInstance) {
      this.requireInstance = requireInstance;
   }

   public boolean isRequireInstance() {
      return this.requireInstance == null ? true : this.requireInstance.value();
   }

   public TypedDataNode getReferencedNode() {
      return this.referencedNode;
   }

   public void setReferencedNode(TypedDataNode typedDataNode) {
      this.referencedNode = typedDataNode;
   }

   public boolean evaluated(Object o) {
      return false;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof LeafRef)) {
         return false;
      } else {
         LeafRef another = (LeafRef)obj;
         if (!this.getEffectivePath().equals(another.getEffectivePath())) {
            return false;
         } else {
            return this.isRequireInstance() == another.isRequireInstance();
         }
      }
   }
}
