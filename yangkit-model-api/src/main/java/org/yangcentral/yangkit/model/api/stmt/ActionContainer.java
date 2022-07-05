package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface ActionContainer {
   Action getAction(String var1);

   List<Action> getActions();

   ValidatorResult addAction(Action var1);
}
