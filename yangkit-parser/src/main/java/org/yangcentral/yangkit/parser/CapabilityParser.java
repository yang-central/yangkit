package org.yangcentral.yangkit.parser;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.ModuleSet;
import org.yangcentral.yangkit.model.api.schema.YangModuleDescription;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

public class CapabilityParser {
   private Element root;

   public CapabilityParser(String capabilityFile) {
      SAXReader reader = SAXReader.createDefault();
      Document capabilitiesDoc = null;
      try {
         capabilitiesDoc = reader.read(new File(capabilityFile));
      } catch (DocumentException e) {
         throw new RuntimeException(e);
      }
      this.root = capabilitiesDoc.getRootElement();
   }
   public CapabilityParser(InputStream inputStream) {
      SAXReader reader = SAXReader.createDefault();
      Document capabilitiesDoc = null;
      try {
         capabilitiesDoc = reader.read(inputStream);
      } catch (DocumentException e) {
         throw new RuntimeException(e);
      }
      this.root = capabilitiesDoc.getRootElement();
   }
   public CapabilityParser(Element element){
      this.root = element;
   }

   public List<Capability> parse()  {
      List<Capability> capabilities = new ArrayList<>();
      Element capabilitiesElement = root.element("capabilities");

      for (Element element: capabilitiesElement.elements()) {
         String text = element.getTextTrim();
         if (text.contains("module=")) {
            ModuleSupportCapability capability = new ModuleSupportCapability(URI.create(text));
            capability.parse(text);
            capabilities.add(capability);
         } else {
            Capability capability = new Capability(URI.create(text));
            capabilities.add(capability);
         }
      }

      return capabilities;
   }
   public static ModuleSet toModuleSet(List<Capability> capabilities){
      ModuleSet moduleSet = new ModuleSet();
      Map<String,List<ModuleSupportCapability>> map = new HashMap<>();
      for (Capability capability : capabilities) {
         if (!(capability instanceof ModuleSupportCapability)) {
            continue;
         }
         ModuleSupportCapability moduleSupportCapability = (ModuleSupportCapability) capability;
         List<ModuleSupportCapability> moduleSupportCapabilities = map.get(moduleSupportCapability.getModule());
         if(moduleSupportCapabilities == null){
            moduleSupportCapabilities = new ArrayList<>();
            map.put(moduleSupportCapability.getModule(),moduleSupportCapabilities);
         }
         moduleSupportCapabilities.add(moduleSupportCapability);
      }
      Iterator<Map.Entry<String,List<ModuleSupportCapability>>> iterator = map.entrySet().iterator();
      while (iterator.hasNext()){
         Map.Entry<String,List<ModuleSupportCapability>> entry = iterator.next();

         List<ModuleSupportCapability> list = entry.getValue();
         Collections.sort(list, new Comparator<ModuleSupportCapability>() {
            @Override
            public int compare(ModuleSupportCapability o1, ModuleSupportCapability o2) {
               return o1.getRevision().compareTo(o2.getRevision());
            }
         });
         int size = list.size();
         for(int i = 0; i < size;i++){
            ModuleSupportCapability moduleSupportCapability = list.get(i);
            ModuleId moduleId = new ModuleId(moduleSupportCapability.getModule(), moduleSupportCapability.getRevision());
            YangModuleDescription yangModuleDescription = new YangModuleDescription(moduleId);
            if (!moduleSupportCapability.getFeatures().isEmpty()) {
               for (String feature : moduleSupportCapability.getFeatures()) {
                  yangModuleDescription.addFeature(feature);
               }
            }

            if (!moduleSupportCapability.getDeviations().isEmpty()) {
               for (String deviation : moduleSupportCapability.getDeviations()) {
                  yangModuleDescription.addDeviation(deviation);
               }
            }
            boolean last = false;
            if(i == (size-1)){
               last = true;
            }
            if(!last){
               moduleSet.addImportOnlyModule(yangModuleDescription);
            } else {
               moduleSet.addModule(yangModuleDescription);
            }
         }


      }
      return moduleSet;
   }
}
