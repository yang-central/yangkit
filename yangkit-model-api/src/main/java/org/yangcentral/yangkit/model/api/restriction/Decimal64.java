package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.stmt.type.FractionDigits;
import org.yangcentral.yangkit.model.api.stmt.type.Range;

import java.math.BigDecimal;

public interface Decimal64 extends Restriction<BigDecimal> {
   FractionDigits getFractionDigits();

   FractionDigits getEffectiveFractionDigits();

   Range getRange();

   Range getEffectiveRange();

   ValidatorResult validate();

   BigDecimal getRangeMax();

   BigDecimal getRangeMin();
}
