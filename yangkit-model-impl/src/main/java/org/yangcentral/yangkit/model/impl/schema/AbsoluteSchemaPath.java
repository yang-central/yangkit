package org.yangcentral.yangkit.model.impl.schema;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import java.util.ArrayList;
import java.util.List;

public class AbsoluteSchemaPath extends SchemaPathImpl implements SchemaPath.Absolute {
   public AbsoluteSchemaPath(List<QName> steps) {
      super(steps);
   }

   public AbsoluteSchemaPath() {
   }

   public boolean isAbsolute() {
      return true;
   }

   public boolean contains(SchemaPath.Absolute another) {
      List<QName> steps = this.getPath();
      List<QName> anotherSteps = another.getPath();
      if (steps.size() < anotherSteps.size()) {
         return false;
      } else {
         for(int i = 0; i < anotherSteps.size(); ++i) {
            QName step = (QName)steps.get(i);
            QName anotherStep = (QName)anotherSteps.get(i);
            if (!step.equals(anotherStep)) {
               return false;
            }
         }

         return true;
      }
   }

   public List<QName> getRelativeSchemaPath(SchemaPath.Absolute descendent) {
      if (null == descendent) {
         return null;
      } else if (!descendent.contains(this)) {
         return null;
      } else {
         List<QName> steps = new ArrayList();
         int thisSize = this.getPath().size();
         int descendentSie = descendent.getPath().size();

         for(int i = thisSize; i < descendentSie; ++i) {
            steps.add((QName)descendent.getPath().get(i));
         }

         return steps;
      }
   }
}
