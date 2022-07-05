package org.yangcentral.yangkit.parser;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CapabilityParser {
   private String capabilityFile;

   public CapabilityParser(String capabilityFile) {
      this.capabilityFile = capabilityFile;
   }

   List<Capability> parse() throws DocumentException {
      List<Capability> capabilities = new ArrayList();
      SAXReader reader = new SAXReader();
      Document capabilitiesDoc = reader.read(new File(this.capabilityFile));
      Element root = capabilitiesDoc.getRootElement();
      Element capabilitiesElement = root.element("capabilities");
      Iterator var6 = capabilitiesElement.elements().iterator();

      while(var6.hasNext()) {
         Element capabilityElement = (Element)var6.next();
         String text = capabilityElement.getTextTrim();
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
}
