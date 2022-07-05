package org.yangcentral.yangkit.model.api.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModuleSet {
   private String name;
   private List<YangModuleDescription> modules = new ArrayList();
   private List<YangModuleDescription> importOnlyModules = new ArrayList();

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List<YangModuleDescription> getModules() {
      return this.modules;
   }

   public List<YangModuleDescription> getImportOnlyModules() {
      return this.importOnlyModules;
   }

   public void addModule(YangModuleDescription module) {
      this.modules.add(module);
   }

   public YangModuleDescription getModule(ModuleId moduleId) {
      Iterator var2 = this.modules.iterator();

      YangModuleDescription moduleDescription;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         moduleDescription = (YangModuleDescription)var2.next();
      } while(!moduleDescription.getModuleId().equals(moduleId));

      return moduleDescription;
   }

   public void addImportOnlyModule(YangModuleDescription module) {
      this.importOnlyModules.add(module);
   }

   public YangModuleDescription getImportOnlyModule(ModuleId moduleId) {
      Iterator var2 = this.importOnlyModules.iterator();

      YangModuleDescription moduleDescription;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         moduleDescription = (YangModuleDescription)var2.next();
      } while(!moduleDescription.getModuleId().equals(moduleId));

      return moduleDescription;
   }

   public boolean match(ModuleId moduleId) {
      Iterator var2 = this.modules.iterator();

      YangModuleDescription moduleDescription;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         moduleDescription = (YangModuleDescription)var2.next();
      } while(!moduleDescription.match(moduleId));

      return true;
   }
}
