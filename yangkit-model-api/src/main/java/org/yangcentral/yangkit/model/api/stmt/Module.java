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

   Extension getExtension(String var1);

   List<Feature> getFeatures();

   Feature getFeature(String var1);

   List<Identity> getIdentities();

   Identity getIdentity(String var1);

   List<Augment> getAugments();

   List<Rpc> getRpcs();

   Rpc getRpc(String var1);

   List<Deviation> getDeviations();

   Optional<ModuleId> findModuleByPrefix(String var1);

   Import getImportByPrefix(String var1);

   Map<String, ModuleId> getPrefixes();

   boolean isSelfPrefix(String var1);

   String getSelfPrefix();

   default boolean isSchemaTreeRoot() {
      return true;
   }

   MainModule getMainModule();

   ModuleId getModuleId();

   List<YangStatement> getEffectiveMetaStatements();

   List<YangStatement> getEffectiveLinkageStatement();

   List<YangStatement> getEffectiveDefinitionStatement();
}
