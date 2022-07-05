package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.type.YangEnum;

import java.util.List;

public interface Enumeration extends Restriction<String> {
   int MAX_VALUE = Integer.MAX_VALUE;
   int MIN_VALUE = Integer.MIN_VALUE;

   Integer getEnumActualValue(String var1);

   Integer getHighestValue();

   List<YangEnum> getEnums();

   List<YangEnum> getEffectiveEnums();
}
