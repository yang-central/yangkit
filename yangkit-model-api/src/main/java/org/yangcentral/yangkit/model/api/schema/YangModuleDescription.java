package org.yangcentral.yangkit.model.api.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YangModuleDescription {
   private ModuleId moduleId;
   private List<ModuleId> subModules = new ArrayList();
   private List<String> features = new ArrayList();
   private List<String> deviations = new ArrayList();

   public YangModuleDescription(ModuleId moduleId) {
      this.moduleId = moduleId;
   }

   public ModuleId getModuleId() {
      return this.moduleId;
   }

   public List<ModuleId> getSubModules() {
      return this.subModules;
   }

   public void addSubModule(ModuleId subModule) {
      this.subModules.add(subModule);
   }

   public List<String> getFeatures() {
      return this.features;
   }

   public void addFeature(String feature) {
      this.features.add(feature);
   }

   public List<String> getDeviations() {
      return this.deviations;
   }

   public void addDeviation(String deviation) {
      this.deviations.add(deviation);
   }

   public boolean match(ModuleId moduleId) {
      if (this.getModuleId().equals(moduleId)) {
         return true;
      } else {
         Iterator var2 = this.subModules.iterator();

         ModuleId subModule;
         do {
            if (!var2.hasNext()) {
               var2 = this.getDeviations().iterator();

               String deviation;
               do {
                  if (!var2.hasNext()) {
                     return false;
                  }

                  deviation = (String)var2.next();
               } while(!deviation.equals(moduleId.getModuleName()));

               return true;
            }

            subModule = (ModuleId)var2.next();
         } while(!subModule.equals(moduleId));

         return true;
      }
   }
}
