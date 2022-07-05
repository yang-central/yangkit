package org.yangcentral.yangkit.xpath.impl;

import org.jaxen.expr.DefaultNameStep;
import org.jaxen.expr.PredicateSet;
import org.jaxen.expr.iter.IterableAxis;

public class YangNameStep extends DefaultNameStep {
   public YangNameStep(IterableAxis axis, String prefix, String localName, PredicateSet predicateSet) {
      super(axis, prefix, localName, predicateSet);
   }

   public String getText() {
      StringBuffer buf = new StringBuffer(64);
      if (this.getAxis() != 1) {
         buf.append(this.getAxisName()).append("::");
      }

      if (this.getPrefix() != null && this.getPrefix().length() > 0) {
         buf.append(this.getPrefix()).append(':');
      }

      return buf.append(this.getLocalName()).append(this.getPredicateSet().getText()).toString();
   }
}
