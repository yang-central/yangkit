package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface GroupingDefContainer {
   List<Grouping> getGroupings();

   Grouping getGrouping(String var1);
}
