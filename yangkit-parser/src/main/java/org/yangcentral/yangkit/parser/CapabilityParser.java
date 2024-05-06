package org.yangcentral.yangkit.parser;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CapabilityParser {
   private Element root;

   public CapabilityParser(String capabilityFile) {
      SAXReader reader = new SAXReader();
      Document capabilitiesDoc = null;
      try {
         capabilitiesDoc = reader.read(new File(capabilityFile));
      } catch (DocumentException e) {
         throw new RuntimeException(e);
      }
      this.root = capabilitiesDoc.getRootElement();
   }
   public CapabilityParser(InputStream inputStream) {
      SAXReader reader = new SAXReader();
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

   List<Capability> parse()  {
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
}
