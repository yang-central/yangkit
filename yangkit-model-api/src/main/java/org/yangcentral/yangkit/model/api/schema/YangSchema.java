package org.yangcentral.yangkit.model.api.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YangSchema {
   private String name;
   private List<ModuleSet> moduleSets = new ArrayList();

   public void setName(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public List<ModuleSet> getModuleSets() {
      return this.moduleSets;
   }

   public void addModuleSet(ModuleSet moduleSet) {
      this.moduleSets.add(moduleSet);
   }

   public boolean match(ModuleId moduleId) {
      Iterator var2 = this.moduleSets.iterator();

      ModuleSet moduleSet;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         moduleSet = (ModuleSet)var2.next();
      } while(!moduleSet.match(moduleId));

      return true;
   }
}
