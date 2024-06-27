package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.model.api.restriction.Empty;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.apache.commons.lang3.ObjectUtils;

public class EmptyImpl extends RestrictionImpl<ObjectUtils.Null> implements Empty {
   public EmptyImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public EmptyImpl(YangContext context) {
      super(context);
   }

   public boolean evaluate(ObjectUtils.Null value) {
      return true;
   }

   public boolean equals(Object obj) {
      return obj instanceof Empty;
   }
}
