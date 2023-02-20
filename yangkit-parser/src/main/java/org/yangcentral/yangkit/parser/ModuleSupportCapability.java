package org.yangcentral.yangkit.parser;

import org.yangcentral.yangkit.model.api.schema.ModuleId;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ModuleSupportCapability extends Capability {
   private String module;
   private String revision;
   private List<String> features = new ArrayList<>();
   private List<String> deviations = new ArrayList<>();

   public ModuleSupportCapability(URI uri) {
      super(uri);
   }

   public String getModule() {
      return this.module;
   }

   public void setModule(String module) {
      this.module = module;
   }

   public String getRevision() {
      return this.revision;
   }

   public void setRevision(String revision) {
      this.revision = revision;
   }

   public List<String> getFeatures() {
      return this.features;
   }

   public void setFeatures(List<String> features) {
      this.features = features;
   }

   public void addFeature(String feature) {
      if (null != feature) {
         if (null == this.features) {
            this.features = new ArrayList<>();
         }

         if (!this.features.contains(feature)) {
            this.features.add(feature);
         }
      }
   }

   public List<String> getDeviations() {
      return this.deviations;
   }

   public void setDeviations(List<String> deviations) {
      this.deviations = deviations;
   }

   public void addDeviation(String deviation) {
      if (null != deviation) {
         if (null == this.deviations) {
            this.deviations = new ArrayList<>();
         }

         if (!this.deviations.contains(deviation)) {
            this.deviations.add(deviation);
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder(this.getUri().toString());
      sb.append("?");
      sb.append("module=");
      sb.append(this.module);
      if (null != this.revision) {
         sb.append("&revision=");
         sb.append(this.revision);
      }

      int size;
      int i;
      String deviation;
      if (null != this.features && this.features.size() > 0) {
         sb.append("&features=");
         size = this.features.size();

         for(i = 0; i < size; ++i) {
            deviation = this.features.get(i);
            sb.append(deviation);
            if (i != size - 1) {
               sb.append(",");
            }
         }
      }

      if (null != this.deviations && this.deviations.size() > 0) {
         sb.append("&deviations=");
         size = this.deviations.size();

         for(i = 0; i < size; ++i) {
            deviation = this.deviations.get(i);
            sb.append(deviation);
            if (i != size - 1) {
               sb.append(",");
            }
         }
      }

      return sb.toString();
   }

   public void parse(String uri) {
      String[] strs = uri.split("module=");
      if (strs.length == 2) {
         String moduleStr = strs[1];
         if (!moduleStr.contains("&")) {
            this.module = moduleStr;
         } else {
            String[] paras = moduleStr.split("&");
            String[] var5 = paras;
            int var6 = paras.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               String para = var5[var7];
               String revisionStr;
               String[] deviationArray;
               String[] var11;
               int var12;
               int var13;
               String deviationStr;
               if (para.startsWith("features=")) {
                  revisionStr = para.substring("features=".length());
                  deviationArray = revisionStr.split(",");
                  var11 = deviationArray;
                  var12 = deviationArray.length;

                  for(var13 = 0; var13 < var12; ++var13) {
                     deviationStr = var11[var13];
                     this.addFeature(deviationStr);
                  }
               } else if (para.startsWith("deviations=")) {
                  revisionStr = para.substring("deviations=".length());
                  deviationArray = revisionStr.split(",");
                  var11 = deviationArray;
                  var12 = deviationArray.length;

                  for(var13 = 0; var13 < var12; ++var13) {
                     deviationStr = var11[var13];
                     this.addDeviation(deviationStr);
                  }
               } else if (para.startsWith("revision=")) {
                  revisionStr = para.substring("revision=".length());
                  this.revision = revisionStr;
               } else {
                  this.module = para;
               }
            }

         }
      }
   }

   public boolean match(ModuleId moduleId) {
      if (this.getModule().equals(moduleId.getModuleName()) && this.getRevision().equals(moduleId.getRevision())) {
         return true;
      }
      if (this.deviations != null) {
         for (String deviation: this.deviations) {
            if (deviation.equals(moduleId.getModuleName())) {
               return true;
            }
         }
      }
      return false;
   }
}
