package org.yangcentral.yangkit.model.api.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * the definition of yang schema, a yang schema maybe contains one or more module sets,it's used in yang library.
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public class YangSchema {
   private String name;
   private List<ModuleSet> moduleSets = new ArrayList<>();

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
      Iterator moduleSetIterator = this.moduleSets.iterator();

      ModuleSet moduleSet;
      do {
         if (!moduleSetIterator.hasNext()) {
            return false;
         }

         moduleSet = (ModuleSet)moduleSetIterator.next();
      } while(!moduleSet.match(moduleId));

      return true;
   }
}
