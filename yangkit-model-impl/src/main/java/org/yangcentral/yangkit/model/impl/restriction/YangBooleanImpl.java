package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.YangBoolean;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

public class YangBooleanImpl extends RestrictionImpl<Boolean> implements YangBoolean {
   public YangBooleanImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public YangBooleanImpl(YangContext context) {
      super(context);
   }

   public boolean evaluated(Boolean value) {
      return true;
   }

   public boolean equals(Object obj) {
      return obj instanceof YangBoolean;
   }
}
