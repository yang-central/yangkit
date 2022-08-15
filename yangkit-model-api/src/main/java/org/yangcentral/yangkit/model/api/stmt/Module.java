package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.model.api.schema.ModuleId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Module extends YangBuiltinStatement, MetaDef, SchemaNodeContainer, DataDefContainer, TypedefContainer, GroupingDefContainer, NotificationContainer, Identifiable {
   YangVersion getYangVersion();

   String getEffectiveYangVersion();

   List<Include> getIncludes();

   List<Import> getImports();

   Organization getOrganization();

   Contact getContact();

   List<Revision> getRevisions();

   Optional<Revision> getCurRevision();

   Optional<String> getCurRevisionDate();

   List<Extension> getExtensions();

   Extension getExtension(String name);

   List<Feature> getFeatures();

   Feature getFeature(String name);

   List<Identity> getIdentities();

   Identity getIdentity(String name);

   List<Augment> getAugments();

   List<Rpc> getRpcs();

   Rpc getRpc(String name);

   List<Deviation> getDeviations();

   Optional<ModuleId> findModuleByPrefix(String prefix);

   Import getImportByPrefix(String prefix);

   Map<String, ModuleId> getPrefixes();

   boolean isSelfPrefix(String prefix);

   String getSelfPrefix();

   default boolean isSchemaTreeRoot() {
      return true;
   }

   MainModule getMainModule();

   ModuleId getModuleId();

   List<YangStatement> getEffectiveMetaStatements();

   List<YangStatement> getEffectiveLinkageStatement();

   List<YangStatement> getEffectiveDefinitionStatement();

   List<Module> getDependentBys();
   void addDependentBy(Module module);
   void removeDependentBy(Module module);
}
