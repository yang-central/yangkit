package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface MustSupport {
   Must getMust(int var1);

   Must getMust(String var1);

   List<Must> getMusts();

   void setMusts(List<Must> var1);

   ValidatorResult addMust(Must var1);

   void removeMust(String var1);

   ValidatorResult updateMust(Must var1);

   ValidatorResult validateMusts();
}
