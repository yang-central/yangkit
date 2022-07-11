package org.yangcentral.yangkit.model.api.schema;

import org.yangcentral.yangkit.base.YangElement;
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
 * @throws
 * @return
 * @author frank feng
 * @since 7/8/2022
 */
public interface YangSchemaContext extends SchemaNodeContainer {
   List<Module> getModules();

   List<Module> getImportOnlyModules();

   Optional<Module> getModule(String var1, String var2);

   Optional<Module> getModule(ModuleId var1);

   List<Module> getModule(String var1);

   List<Module> getModule(URI var1);

   Optional<? extends SchemaNode> getSchemaNode(SchemaPath.Absolute var1);

   YangSchema getYangSchema();

   void addModule(Module var1);

   void addImportOnlyModule(Module var1);

   boolean isImportOnly(Module var1);

   Module removeModule(ModuleId var1);

   Map<String, List<YangElement>> getParseResult();

   ValidatorResult validate();
}
