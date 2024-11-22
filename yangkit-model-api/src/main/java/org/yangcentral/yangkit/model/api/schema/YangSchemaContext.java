package org.yangcentral.yangkit.model.api.schema;

import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/**
 * the interface of yang schema context. a schema context contains all yang modules including import-only yang modules.
 * when a series of yang files are parsed to yang modules,these yang modules will be added to a yang schema context. user can
 * validate the yang schema context by calling validate method.
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface YangSchemaContext extends SchemaNodeContainer {
   List<Module> getModules();

   List<Module> getImportOnlyModules();

   Optional<Module> getModule(String name, String revision);

   Optional<Module> getModule(ModuleId moduleId);

   List<Module> getModule(String name);

   Optional<Module> getLatestModule(String name);

   List<Module> getModule(URI uri);

   Optional<? extends SchemaNode> getSchemaNode(SchemaPath.Absolute path);

   SchemaNode getSchemaNode(AbsolutePath absolutePath);

   YangSchema getYangSchema();

   void setYangSchema(YangSchema yangSchema);

   void addModule(Module module);

   void addImportOnlyModule(Module module);

   boolean isImportOnly(Module module);

   Module removeModule(ModuleId moduleId);

   Map<String, List<YangElement>> getParseResult();

   ValidatorResult validate();

   ValidatorResult getValidateResult();

   void clearValidateResult();

   void buildDependencies();

   List<List<Module>> resolvesImportOrder();
}
