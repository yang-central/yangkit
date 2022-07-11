package org.yangcentral.yangkit.model.api.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * definition a set of yang modules.
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
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
      Iterator moduleDescriptionIterator = this.modules.iterator();

      YangModuleDescription moduleDescription;
      do {
         if (!moduleDescriptionIterator.hasNext()) {
            return null;
         }

         moduleDescription = (YangModuleDescription)moduleDescriptionIterator.next();
      } while(!moduleDescription.getModuleId().equals(moduleId));

      return moduleDescription;
   }

   public void addImportOnlyModule(YangModuleDescription module) {
      this.importOnlyModules.add(module);
   }

   public YangModuleDescription getImportOnlyModule(ModuleId moduleId) {
      Iterator moduleDescriptionIterator = this.importOnlyModules.iterator();

      YangModuleDescription moduleDescription;
      do {
         if (!moduleDescriptionIterator.hasNext()) {
            return null;
         }

         moduleDescription = (YangModuleDescription)moduleDescriptionIterator.next();
      } while(!moduleDescription.getModuleId().equals(moduleId));

      return moduleDescription;
   }
   /**
    * judge whether the specified module-id matches this module-set.
    * @param moduleId module id
    * @version 1.0.0
    * @throws
    * @return boolean true: the module-id matches the module-set
    * (it's the module belongs to module-set or the submodule belongs to the module-set)
    * @author frank feng
    * @since 7/8/2022
    */
   public boolean match(ModuleId moduleId) {
      Iterator iterator = this.modules.iterator();

      YangModuleDescription moduleDescription;
      do {
         if (!iterator.hasNext()) {
            return false;
         }

         moduleDescription = (YangModuleDescription)iterator.next();
      } while(!moduleDescription.match(moduleId));

      return true;
   }
}
