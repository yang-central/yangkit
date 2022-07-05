package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.InstanceIdentifier;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.type.RequireInstance;
import org.yangcentral.yangkit.model.impl.stmt.type.RequireInstanceImpl;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;

public class InstanceIdentifierImpl extends RestrictionImpl<YangAbsoluteLocationPath> implements InstanceIdentifier {
   private RequireInstance requireInstance;

   public InstanceIdentifierImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public InstanceIdentifierImpl(YangContext context) {
      super(context);
   }

   public RequireInstance getRequireInstance() {
      return this.requireInstance;
   }

   public void setRequireInstance(RequireInstance requireInstance) {
      this.requireInstance = requireInstance;
   }

   public boolean isRequireInstance() {
      RequireInstance requireInstance = this.getRequireInstance();
      if (null != requireInstance) {
         return requireInstance.value();
      } else if (this.getDerived() != null) {
         InstanceIdentifier instanceIdentifier = (InstanceIdentifier)this.getDerived().getType().getRestriction();
         return instanceIdentifier.isRequireInstance();
      } else {
         return true;
      }
   }

   public RequireInstance getEffectiveRequireInstance() {
      RequireInstance requireInstance = this.getRequireInstance();
      if (requireInstance != null) {
         return requireInstance;
      } else if (this.getDerived() != null) {
         InstanceIdentifier instanceIdentifier = (InstanceIdentifier)this.getDerived().getType().getRestriction();
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

   public boolean evaluated(YangAbsoluteLocationPath yangAbsoluteLocationPath) {
      return true;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof InstanceIdentifier)) {
         return false;
      } else {
         InstanceIdentifier another = (InstanceIdentifier)obj;
         return this.isRequireInstance() == another.isRequireInstance();
      }
   }
}
