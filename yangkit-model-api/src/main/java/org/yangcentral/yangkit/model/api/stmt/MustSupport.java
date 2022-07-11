package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface MustSupport {
   Must getMust(int index);

   Must getMust(String condition);

   List<Must> getMusts();

   void setMusts(List<Must> musts);

   ValidatorResult addMust(Must must);

   void removeMust(String condition);

   ValidatorResult updateMust(Must must);

   ValidatorResult validateMusts();
}
