package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface YangStatement extends YangElement {
   QName getYangKeyword();

   String getArgStr();

   void setArgStr(String var1);

   List<YangElement> getSubElements();

   List<YangStatement> getSubStatement(QName var1);

   List<YangUnknown> getUnknowns();

   ValidatorResult build(BuildPhase var1);

   ValidatorResult build();

   ValidatorResult validate();

   ValidatorResult getValidateResult();

   void setValidateResult(ValidatorResult var1);

   boolean isBuilt();

   boolean isBuilding();

   boolean isValidated();

   boolean addChild(YangElement var1);

   boolean addChild(int var1, YangElement var2);

   boolean updateChild(YangStatement var1);

   boolean updateChild(int var1, YangElement var2);

   boolean removeChild(YangElement var1);

   void setChildren(List<YangElement> var1);

   YangStatement getParentStatement();

   void setParentStatement(YangStatement var1);

   YangContext getContext();

   void setContext(YangContext var1);

   ValidatorResult init();

   boolean isInit();

   <T extends YangStatement> T getSelf();

   YangStatement clone();

   YangStatement clonedBy();

   boolean isErrorStatement();

   void setErrorStatement(boolean var1);

   ValidatorResult afterValidate();

   List<YangStatement> getEffectiveSubStatements();
}
