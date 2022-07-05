package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.stmt.type.Range;

public interface YangInteger<T extends Comparable> extends Restriction<T> {
   Range getRange();

   Range getEffectiveRange();

   ValidatorResult setRange(Range var1);

   T getMax();

   T getMin();
}
