package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.stmt.Choice;

public interface ChoiceData extends YangData<Choice>, YangDataContainer {
   CaseData getCaseData();
}
