package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import java.util.List;

public interface ActionContainer {
   Action getAction(String actionName);

   List<Action> getActions();

   ValidatorResult addAction(Action action);
}
