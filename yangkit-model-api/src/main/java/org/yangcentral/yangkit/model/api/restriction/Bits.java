package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.type.Bit;

import java.util.List;

public interface Bits extends Restriction<List<String>> {
   long MAX_POSITION = 4294967295L;
   long MIN_POSITION = 0L;

   List<Bit> getBits();

   List<Bit> getEffectiveBits();

   Long getBitActualPosition(String var1);

   Long getMaxPosition();
}
