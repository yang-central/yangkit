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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public class YangYinParser {
   static {
      YangStatementImplRegister.registerImpl();
   }

   /**
    * parse yang modules from a list of yang file
    * @param yangFiles
    * @return yang schema context
    * @throws IOException
    * @throws YangParserException
    * @throws DocumentException
    */
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

   /**
    * parse yang modules from yang directory file
    * @param yangDir yang directory file
    * @return yang schema context
    * @throws IOException
    * @throws YangParserException
    * @throws DocumentException
    */

   public static YangSchemaContext parse(File yangDir) throws IOException, YangParserException, DocumentException {
      List<File> files = getYangFiles(yangDir);
      return parse(files);
   }

   /**
    * parse yang modules from a directory file
    * @param yangDir yang directory file
    * @param context yang schema context
    * @return yang schema context
    * @throws IOException
    * @throws YangParserException
    * @throws DocumentException
    */

   public static YangSchemaContext parse(File yangDir, YangSchemaContext context) throws IOException, YangParserException, DocumentException {
      List<File> files = getYangFiles(yangDir);
      return parse(files, context);
   }

   /**
    * parse yang modules from a directory
    * @param yangDir a directory contains yang/yin modules
    * @return yang schema context
    * @throws IOException
    * @throws YangParserException
    * @throws DocumentException
    */

   public static YangSchemaContext parse(String yangDir) throws IOException, YangParserException, DocumentException {
      File dir = new File(yangDir);
      return parse(dir);
   }

   /**
    * parse yang modules from a directory with a dependency directory and capabilities
    * @param yangDir directory contains yang/yin modules
    * @param dependency directory contains yang/yin modules for dependency
    * @param capabilities capabilities xml like NETCONF capabilities
    * @return yang schema context
    * @throws IOException
    * @throws YangParserException
    * @throws DocumentException
    */
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

   /**
    * parse yang modules from a directory and with capabilities
    * @param yangDir  a directory contains yang/yin modules
    * @param capabilities capabilities xml like NETCONF capabilities
    * @return yang schema context
    * @throws IOException
    * @throws YangParserException
    * @throws DocumentException
    */

   public static YangSchemaContext parse(String yangDir, String capabilities) throws IOException, YangParserException, DocumentException {
      CapabilityParser capabilityParser = new CapabilityParser(capabilities);
      List<Capability> capabilityList = capabilityParser.parse();
      return parse(yangDir,capabilityList);
   }
   /**
    * parse yang modules from a directory and with capabilities
    * @param yangDir  a directory contains yang/yin modules
    * @param capabilityList capabilities
    * @return yang schema context
    * @throws IOException
    * @throws YangParserException
    * @throws DocumentException
    */

   public static YangSchemaContext parse(String yangDir, List<Capability> capabilityList) throws IOException, YangParserException, DocumentException {
      YangSchemaContext schemaContext = parse(yangDir);
      schemaContext.buildDependencies();
      if (capabilityList != null) {
         schemaContext = filter(schemaContext,capabilityList);
      }

      return schemaContext;
   }

   private static Map<ModuleId,Module> searchDependencyChain(YangSchemaContext schemaContext,Module module){
      Map<ModuleId,Module> moduleMap = new HashMap<>();
      List<Module> dependencies = module.getDependencies();
      for(Module dependency: dependencies){
         Optional<Module> moduleOp = schemaContext.getModule(dependency.getModuleId());
         if(!moduleOp.isPresent()){
            //if not found, add
            moduleMap.putIfAbsent(dependency.getModuleId(),dependency);
         }
         moduleMap.putAll(searchDependencyChain(schemaContext,dependency));
      }
      return moduleMap;
   }

   public static YangSchemaContext filter(@Nonnull YangSchemaContext schemaContext, @Nonnull List<Capability> capabilityList){
      List<Module> unMatchedModules = new ArrayList<>();
      ModuleSet moduleSet = CapabilityParser.toModuleSet(capabilityList);

      List<Module> importOnlyModules = new ArrayList<>();
      for (Module module : schemaContext.getModules()) {
         if(moduleSet.getModule(module.getModuleId()) != null){
            continue;
         }
         if(moduleSet.getImportOnlyModule(module.getModuleId()) != null){
            importOnlyModules.add(module);
            continue;
         }

         if (module instanceof SubModule) {
            SubModule subModule = (SubModule) module;
            YangStatement belongsTo = subModule.getSubStatement(YangBuiltinKeyword.BELONGSTO.getQName()).get(0);
            String mainModuleName = belongsTo.getArgStr();
            YangModuleDescription yangModuleDescription = moduleSet.getModule(mainModuleName);
            if (yangModuleDescription != null) {
               yangModuleDescription.addSubModule(subModule.getModuleId());
               continue;
            }
            yangModuleDescription = moduleSet.getImportOnlyModule(mainModuleName);
            if (yangModuleDescription != null) {
               yangModuleDescription.addSubModule(subModule.getModuleId());
               importOnlyModules.add(module);
               continue;
            }
         }

         unMatchedModules.add(module);
      }

      for (Module unMatchedModule : unMatchedModules) {
         schemaContext.removeModule(unMatchedModule.getModuleId());
      }
      for (Module importOnlyModule : importOnlyModules) {
         schemaContext.removeModule(importOnlyModule.getModuleId());
         schemaContext.addImportOnlyModule(importOnlyModule);
      }

      Map<ModuleId,Module> moduleMap = new HashMap<>();
      for (Module module : schemaContext.getModules()){
         moduleMap.putAll(searchDependencyChain(schemaContext,module));
      }

      Iterator<Map.Entry<ModuleId,Module>> it = moduleMap.entrySet().iterator();
      while (it.hasNext()){
         Map.Entry<ModuleId,Module> entry = it.next();
         schemaContext.addImportOnlyModule(entry.getValue());
         moduleSet.addImportOnlyModule(new YangModuleDescription(entry.getValue().getModuleId()));
      }

      YangSchema yangSchema = schemaContext.getYangSchema();
      if (yangSchema == null) {
         yangSchema = new YangSchema();
         schemaContext.setYangSchema(yangSchema);
      }

      yangSchema.addModuleSet(moduleSet);
      return  schemaContext;
   }

   /**
    * parse yang modules from a directory
    * @param yangDir a directory contains yang/yin modules
    * @param context yang schema context
    * @return yang schema context
    * @throws IOException
    * @throws YangParserException
    * @throws DocumentException
    */

   public static YangSchemaContext parse(String yangDir, YangSchemaContext context) throws IOException, YangParserException, DocumentException {
      File dir = new File(yangDir);
      return parse(dir, context);
   }

   private static String readString(InputStream inputStream) throws IOException {
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

      String temp = null;
      StringBuffer sb = new StringBuffer();

      for(temp = br.readLine(); temp != null; temp = br.readLine()) {
         sb.append(temp + "\n");
      }

      br.close();
      return sb.toString();
   }

   /**
    * parse YANG module from inputStream, not import-only
    * @param inputStream
    * @param moduleInfo
    * @param context
    * @return
    * @throws YangParserException
    * @throws IOException
    * @throws DocumentException
    */

   public static YangSchemaContext parse(InputStream inputStream,
                                         String moduleInfo,
                                         @Nullable YangSchemaContext context)
           throws YangParserException, IOException, DocumentException {
      return parse(inputStream,moduleInfo,true,false,context);
   }

   /**
    * parse YANG/YIN module from inputStream
    * @param inputStream  the candidate input stream
    * @param moduleInfo  the description of the module, it often indicates the position information and module information
    *                    for example, "ftp://10.1.1.1/ietf-interfaces@2019-07-31"
    *                    or "C:\documents\yang\ietf-interfaces@2019-07-31.yang"
    * @param isYang
    * @param importOnly
    * @param context
    * @return
    * @throws YangParserException
    * @throws IOException
    * @throws DocumentException
    */
   public static YangSchemaContext parse(InputStream inputStream,
                                         String moduleInfo,
                                         boolean isYang,
                                         boolean importOnly,
                                         @Nullable YangSchemaContext context)
           throws YangParserException, IOException, DocumentException {
      List<YangElement> yangElements;
      if(null == context){
         context = YangStatementRegister.getInstance().getSchemeContextInstance();
      }

      String yangString = "";
      if (isYang) {
         yangString = readString(inputStream);
         YangParser yangParser = new YangParser();
         YangParserEnv env = new YangParserEnv();
         env.setYangStr(yangString);
         env.setFilename(moduleInfo);
         env.setCurPos(0);
         yangElements = yangParser.parseYang(yangString, env);
      } else {
         YinParser yinParser = new YinParser(moduleInfo);
         SAXReader reader = new SAXReader();
         Document document = reader.read(inputStream);
         yangElements = yinParser.parse(document);
      }

      if(yangElements == null || yangElements.isEmpty()){
         return context;
      }
      context.getParseResult().put(moduleInfo, yangElements);
      for(YangElement yangElement: yangElements){
         if(yangElement instanceof Module){
            Module module = (Module) yangElement;
            module.setOriginalString(yangString);
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
            if(importOnly){
               context.addImportOnlyModule(module);
            } else {
               context.addModule(module);
            }

            break;
         }
      }
      return context;
   }

   /**
    * parse yang modules from a list of files
    * @param files a list of files
    * @param context yang schema context
    * @return yang schema context
    * @throws IOException
    * @throws YangParserException
    * @throws DocumentException
    */

   public static YangSchemaContext parse(List<File> files, YangSchemaContext context)
           throws IOException, YangParserException, DocumentException {
      List<File> yangFiles = getYangFiles(files);

      for (File yangFile : yangFiles){
         context = parse(new FileInputStream(yangFile), yangFile.getAbsolutePath(),
                 yangFile.getName().endsWith(Yang.YANG_SUFFIX),false,context);
      }

      return context;
   }
}
