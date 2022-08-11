package org.yangcentral.yangkit.parser;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.model.api.schema.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.register.YangStatementImplRegister;
import org.yangcentral.yangkit.register.YangStatementRegister;
import org.yangcentral.yangkit.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class YangYinParser {
   static {
      YangStatementImplRegister.registerImpl();
   }
   public static YangSchemaContext parse(List<File> yangFiles) throws IOException, YangParserException, DocumentException {
      if (null == yangFiles) {
         return null;
      } else {
         YangSchemaContext schemaContext = YangStatementRegister.getInstance().getSchemeContextInstance();
         return parse(yangFiles, schemaContext);
      }
   }

   private static List<File> getYangFiles(List<File> files) {
      List<File> yangFiles = new ArrayList();
      if (null == files) {
         return yangFiles;
      } else {
         Iterator fileIterator = files.iterator();

         while(fileIterator.hasNext()) {
            File file = (File)fileIterator.next();
            yangFiles.addAll(getYangFiles(file));
         }

         return yangFiles;
      }
   }

   private static List<File> getYangFiles(File dir) {
      List<File> yangFiles = new ArrayList();
      if (dir.isFile()) {
         if (dir.getName().endsWith(".yang") || dir.getName().endsWith(".yin")) {
            yangFiles.add(dir);
         }

         return yangFiles;
      } else {
         File[] files = dir.listFiles();
         if (null != files && files.length != 0) {
            File[] var3 = files;
            int var4 = files.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               File file = var3[var5];
               yangFiles.addAll(getYangFiles(file));
            }

            return yangFiles;
         } else {
            return yangFiles;
         }
      }
   }

   public static YangSchemaContext parse(File yangDir) throws IOException, YangParserException, DocumentException {
      List<File> files = getYangFiles(yangDir);
      return parse(files);
   }

   public static YangSchemaContext parse(File yangDir, YangSchemaContext context) throws IOException, YangParserException, DocumentException {
      List<File> files = getYangFiles(yangDir);
      return parse(files, context);
   }

   public static YangSchemaContext parse(String yangDir) throws IOException, YangParserException, DocumentException {
      File dir = new File(yangDir);
      return parse(dir);
   }

   public static YangSchemaContext parse(String yangDir, String dependency, String capabilities) throws IOException, YangParserException, DocumentException {
      YangSchemaContext schemaContext = parse(yangDir, capabilities);
      if (dependency != null) {
         YangSchemaContext dependencyContext = parse(dependency);
         Iterator it = dependencyContext.getModules().iterator();

         while(it.hasNext()) {
            Module depModule = (Module)it.next();
            schemaContext.addImportOnlyModule(depModule);
         }

         it = dependencyContext.getParseResult().entrySet().iterator();

         while(it.hasNext()) {
            Map.Entry<String, List<YangElement>> entry = (Map.Entry)it.next();
            if (!schemaContext.getParseResult().containsKey(entry.getKey())) {
               schemaContext.getParseResult().put((String)entry.getKey(), (List)entry.getValue());
            }
         }
      }

      return schemaContext;
   }

   public static YangSchemaContext parse(String yangDir, String capabilities) throws IOException, YangParserException, DocumentException {
      YangSchemaContext schemaContext = parse(yangDir);
      if (capabilities != null) {
         CapabilityParser capabilityParser = new CapabilityParser(capabilities);
         List<Capability> capabilityList = capabilityParser.parse();
         List<Module> unMatchedModules = new ArrayList();
         ModuleSet moduleSet = new ModuleSet();
         Iterator var7 = schemaContext.getModules().iterator();

         Module module;
         while(var7.hasNext()) {
            module = (Module)var7.next();
            boolean match = false;
            Iterator var10 = capabilityList.iterator();

            label76:
            while(var10.hasNext()) {
               Capability capability = (Capability)var10.next();
               if (capability instanceof ModuleSupportCapability) {
                  ModuleSupportCapability moduleSupportCapability = (ModuleSupportCapability)capability;
                  String deviation;
                  if (moduleSupportCapability.match(module.getModuleId())) {
                     match = true;
                     YangModuleDescription moduleDescription = moduleSet.getModule(module.getModuleId());
                     if (null == moduleDescription) {
                        moduleDescription = new YangModuleDescription(module.getModuleId());
                        moduleSet.addModule(moduleDescription);
                     }

                     Iterator var20;
                     if (!moduleSupportCapability.getFeatures().isEmpty()) {
                        var20 = moduleSupportCapability.getFeatures().iterator();

                        while(var20.hasNext()) {
                           deviation = (String)var20.next();
                           moduleDescription.addFeature(deviation);
                        }
                     }

                     if (moduleSupportCapability.getDeviations().isEmpty()) {
                        break;
                     }

                     var20 = moduleSupportCapability.getDeviations().iterator();

                     while(true) {
                        if (!var20.hasNext()) {
                           break label76;
                        }

                        deviation = (String)var20.next();
                        moduleDescription.addDeviation(deviation);
                     }
                  }

                  if (module instanceof SubModule) {
                     SubModule subModule = (SubModule)module;
                     YangStatement belongsTo = (YangStatement)subModule.getSubStatement(YangBuiltinKeyword.BELONGSTO.getQName()).get(0);
                     deviation = belongsTo.getArgStr();
                     if (moduleSupportCapability.getModule().equals(deviation)) {
                        match = true;
                        ModuleId mainModuleId = new ModuleId(moduleSupportCapability.getModule(), moduleSupportCapability.getRevision());
                        YangModuleDescription moduleDescription = moduleSet.getModule(mainModuleId);
                        if (moduleDescription == null) {
                           moduleDescription = new YangModuleDescription(mainModuleId);
                           moduleSet.addModule(moduleDescription);
                        }

                        moduleDescription.addSubModule(subModule.getModuleId());
                        break;
                     }
                  }
               }
            }

            if (!match) {
               unMatchedModules.add(module);
            }
         }

         var7 = unMatchedModules.iterator();

         while(var7.hasNext()) {
            module = (Module)var7.next();
            schemaContext.removeModule(module.getModuleId());
            schemaContext.addImportOnlyModule(module);
         }

         YangSchema yangSchema = schemaContext.getYangSchema();
         if (yangSchema == null) {
            yangSchema = new YangSchema();
         }

         yangSchema.addModuleSet(moduleSet);
      }

      return schemaContext;
   }

   public static YangSchemaContext parse(String yangDir, YangSchemaContext context) throws IOException, YangParserException, DocumentException {
      File dir = new File(yangDir);
      return parse(dir, context);
   }

   public static YangSchemaContext parse(List<File> files, YangSchemaContext context) throws IOException, YangParserException, DocumentException {
      List<File> yangFiles = getYangFiles(files);
      Iterator fileIterator = yangFiles.iterator();

      while(true) {
         YangElement element;
         label48:
         while(true) {
            File yangFile;
            List elements;
            do {
               do {
                  if (!fileIterator.hasNext()) {
                     return context;
                  }

                  yangFile = (File)fileIterator.next();
                  if (yangFile.getName().endsWith(".yang")) {
                     YangParser yangParser = new YangParser();
                     String yangStr = FileUtil.readFile2String(yangFile);
                     YangParserEnv env = new YangParserEnv();
                     env.setYangStr(yangStr);
                     env.setFilename(yangFile.getAbsolutePath());
                     env.setCurPos(0);
                     elements = yangParser.parseYang(yangStr, env);
                  } else {
                     YinParser yinParser = new YinParser(yangFile.getAbsolutePath());
                     SAXReader reader = new SAXReader();
                     Document document = reader.read(yangFile);
                     elements = yinParser.parse(document);
                  }
               } while(elements == null);
            } while(elements.isEmpty());

            context.getParseResult().put(yangFile.getAbsolutePath(), elements);
            Iterator iterator = elements.iterator();

            while(iterator.hasNext()) {
               element = (YangElement)iterator.next();
               if (element instanceof YangStatement) {
                  Module module = (Module)element;
                  if (context.getYangSchema() == null) {
                     break label48;
                  }

                  YangSchema yangSchema = context.getYangSchema();
                  if (yangSchema.match(new ModuleId(module.getArgStr(), module.getCurRevisionDate().isPresent() ? (String)module.getCurRevisionDate().get() : null))) {
                     break label48;
                  }
               }
            }
         }

         context.addModule((Module)element);
      }
   }
}
