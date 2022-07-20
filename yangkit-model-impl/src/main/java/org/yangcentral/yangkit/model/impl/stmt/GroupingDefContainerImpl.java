package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Grouping;
import org.yangcentral.yangkit.model.api.stmt.GroupingDefContainer;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class GroupingDefContainerImpl implements GroupingDefContainer {
   private List<Grouping> groupings = new ArrayList();
   private YangContext yangContext;

   public List<Grouping> getGroupings() {
      return Collections.unmodifiableList(this.groupings);
   }

   public YangContext getYangContext() {
      return this.yangContext;
   }

   public void setYangContext(YangContext yangContext) {
      this.yangContext = yangContext;
   }

   public Grouping getGrouping(String name) {
      return this.getYangContext().getGrouping(name);
   }

   public Grouping removeGrouping(String name) {
      Grouping target = getGrouping(name);
      if(!groupings.remove(target)){
         return null;
      }
      getYangContext().getGroupingIdentifierCache().remove(name);
      return target;
   }

   public void removeGroupings() {
      for(Grouping grouping: groupings){
         getYangContext().getGroupingIdentifierCache().remove(grouping.getArgStr());
      }
      groupings.clear();
   }

   public ValidatorResult addGrouping(Grouping grouping) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Grouping orig = this.getGrouping(grouping.getArgStr());
      if (orig != null) {
         validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(orig, grouping));
         grouping.setErrorStatement(true);
         return validatorResultBuilder.build();
      } else {
         this.groupings.add(grouping);
         this.getYangContext().getGroupingIdentifierCache().put(grouping.getArgStr(), grouping);
         return validatorResultBuilder.build();
      }
   }
}
