package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

public abstract class RestrictionImpl<T> implements Restriction<T> {
   private Typedef derived;
   private YangContext context;

   public RestrictionImpl(YangContext context, Typedef derived) {
      this(context);
      this.derived = derived;
   }

   public RestrictionImpl(YangContext context) {
      this.context = context;
   }

   public Typedef getDerived() {
      return this.derived;
   }

   public YangContext getContext() {
      return this.context;
   }
}
