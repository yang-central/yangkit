package org.yangcentral.yangkit.parser;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.yangcentral.yangkit.base.Yang;
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
      List<File> yangFiles = new ArrayList<>();
      if (null == files) {
         return yangFiles;
      }

      for (File file : files) {
         yangFiles.addAll(getYangFiles(file));
      }

      return yangFiles;
   }

   private static List<File> getYangFiles(File dir) {
      List<File> yangFiles = new ArrayList<>();
      if (dir.isFile()) {
         if (dir.getName().endsWith(".yang") || dir.getName().endsWith(".yin")) {
            yangFiles.add(dir);
         }

         return yangFiles;
      }
      File[] files = dir.listFiles();
      if (null != files && files.length != 0) {
         for(File file :files) {
            yangFiles.addAll(getYangFiles(file));
         }
      }
      return yangFiles;
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

         for (Module depModule : dependencyContext.getModules()) {
            schemaContext.addImportOnlyModule(depModule);
         }

         for (Map.Entry<String, List<YangElement>> entry: dependencyContext.getParseResult().entrySet()) {
            if (!schemaContext.getParseResult().containsKey(entry.getKey())) {
               schemaContext.getParseResult().put(entry.getKey(),entry.getValue());
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
         List<Module> unMatchedModules = new ArrayList<>();
         ModuleSet moduleSet = new ModuleSet();

         for (Module module : schemaContext.getModules()) {
            boolean match = false;

            for (Capability capability : capabilityList) {
               if (!(capability instanceof ModuleSupportCapability)) {
                  continue;
               }
               ModuleSupportCapability moduleSupportCapability = (ModuleSupportCapability) capability;
               if (moduleSupportCapability.match(module.getModuleId())) {
                  match = true;
                  YangModuleDescription moduleDescription = moduleSet.getModule(module.getModuleId());
                  if (null == moduleDescription) {
                     moduleDescription = new YangModuleDescription(module.getModuleId());
                     moduleSet.addModule(moduleDescription);
                  }
                  if (!moduleSupportCapability.getFeatures().isEmpty()) {
                     for (String feature : moduleSupportCapability.getFeatures()) {
                        moduleDescription.addFeature(feature);
                     }
                  }

                  if (!moduleSupportCapability.getDeviations().isEmpty()) {
                     for (String deviation : moduleSupportCapability.getDeviations()) {
                        moduleDescription.addDeviation(deviation);
                     }
                  }
                  break;
               }


               if (module instanceof SubModule) {
                  SubModule subModule = (SubModule) module;
                  YangStatement belongsTo = subModule.getSubStatement(YangBuiltinKeyword.BELONGSTO.getQName()).get(0);
                  String mainModuleName = belongsTo.getArgStr();
                  if (moduleSupportCapability.getModule().equals(mainModuleName)) {
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
            if (!match) {
               unMatchedModules.add(module);
            }
         }

         for (Module unMatchedModule : unMatchedModules) {
            schemaContext.removeModule(unMatchedModule.getModuleId());
            schemaContext.addImportOnlyModule(unMatchedModule);
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

      for (File yangFile : yangFiles){
         List<YangElement> yangElements;
         if (yangFile.getName().endsWith(Yang.YANG_SUFFIX)) {
            YangParser yangParser = new YangParser();
            String yangStr = FileUtil.readFile2String(yangFile);
            YangParserEnv env = new YangParserEnv();
            env.setYangStr(yangStr);
            env.setFilename(yangFile.getAbsolutePath());
            env.setCurPos(0);
            yangElements = yangParser.parseYang(yangStr, env);
         } else {
            YinParser yinParser = new YinParser(yangFile.getAbsolutePath());
            SAXReader reader = new SAXReader();
            Document document = reader.read(yangFile);
            yangElements = yinParser.parse(document);
         }

         if(yangElements == null || yangElements.isEmpty()){
            continue;
         }
         context.getParseResult().put(yangFile.getAbsolutePath(), yangElements);
         for(YangElement yangElement: yangElements){
            if(yangElement instanceof Module){
               Module module = (Module) yangElement;
               if(context.getYangSchema() != null){
                  YangSchema yangSchema = context.getYangSchema();
                  String revision = null;
                  List<YangStatement> revisions = module.getSubStatement(YangBuiltinKeyword.REVISION.getQName());
                  if(!revisions.isEmpty()){
                     revision = revisions.get(0).getArgStr();
                  }
                  if (!yangSchema.match(new ModuleId(module.getArgStr(),revision))){
                     continue;
                  }
               }
               context.addModule(module);
               break;
            }
         }
      }

      return context;
   }
}
