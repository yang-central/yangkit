package org.yangcentral.yangkit.xpath.impl;

import org.jaxen.JaxenException;
import org.jaxen.expr.DefaultXPathFactory;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.Step;
import org.jaxen.expr.iter.IterableAncestorAxis;
import org.jaxen.expr.iter.IterableAncestorOrSelfAxis;
import org.jaxen.expr.iter.IterableAttributeAxis;
import org.jaxen.expr.iter.IterableAxis;
import org.jaxen.expr.iter.IterableDescendantAxis;
import org.jaxen.expr.iter.IterableDescendantOrSelfAxis;
import org.jaxen.expr.iter.IterableFollowingAxis;
import org.jaxen.expr.iter.IterableFollowingSiblingAxis;
import org.jaxen.expr.iter.IterableNamespaceAxis;
import org.jaxen.expr.iter.IterableParentAxis;
import org.jaxen.expr.iter.IterablePrecedingAxis;
import org.jaxen.expr.iter.IterablePrecedingSiblingAxis;
import org.jaxen.expr.iter.IterableSelfAxis;

public class YangXPathFactory extends DefaultXPathFactory {
   public LocationPath createRelativeLocationPath() throws JaxenException {
      return new YangRelativeLocationPathImpl();
   }

   public LocationPath createAbsoluteLocationPath() throws JaxenException {
      return new YangAbsoluteLocationPathImpl();
   }

   public Step createNameStep(int axis, String prefix, String localName) throws JaxenException {
      return new YangNameStep(this.getIterableAxis(axis), prefix, localName, this.createPredicateSet());
   }

   protected IterableAxis getIterableAxis(int axis) throws JaxenException {
      switch (axis) {
         case 1:
            return new YangIterableChildAxis(axis);
         case 2:
            return new IterableDescendantAxis(axis);
         case 3:
            return new IterableParentAxis(axis);
         case 4:
            return new IterableAncestorAxis(axis);
         case 5:
            return new IterableFollowingSiblingAxis(axis);
         case 6:
            return new IterablePrecedingSiblingAxis(axis);
         case 7:
            return new IterableFollowingAxis(axis);
         case 8:
            return new IterablePrecedingAxis(axis);
         case 9:
            return new IterableAttributeAxis(axis);
         case 10:
            return new IterableNamespaceAxis(axis);
         case 11:
            return new IterableSelfAxis(axis);
         case 12:
            return new IterableDescendantOrSelfAxis(axis);
         case 13:
            return new IterableAncestorOrSelfAxis(axis);
         default:
            throw new JaxenException("Unrecognized axis code: " + axis);
      }
   }
}
