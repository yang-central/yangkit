package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.common.api.Namespace;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Extension;
import org.yangcentral.yangkit.model.api.stmt.Feature;
import org.yangcentral.yangkit.model.api.stmt.Grouping;
import org.yangcentral.yangkit.model.api.stmt.Identity;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangVersion;
import org.yangcentral.yangkit.model.api.stmt.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YangContext {
   private YangSchemaContext schemaContext;
   private Module curModule;
   private Grouping curGrouping;
   private Namespace curNamespace;
   private List<YangContext> mergedContexts = new ArrayList();
   private YangStatement self;
   private Map<String, SchemaNode> SchemaNodeIdentifierCache = new ConcurrentHashMap();
   private Map<String, Grouping> groupingIdentifierCache = new ConcurrentHashMap();
   private Map<String, Typedef> typedefIdentifierCache = new ConcurrentHashMap();
   private Map<String, Extension> extensionCache = new ConcurrentHashMap();
   private Map<String, Feature> featureCache = new ConcurrentHashMap();
   private Map<String, Identity> identityCache = new ConcurrentHashMap();

   public YangContext(YangSchemaContext schemaContext, Module curModule) {
      this.schemaContext = schemaContext;
      this.curModule = curModule;
   }

   public YangContext(YangContext parent) {
      this.schemaContext = parent.getSchemaContext();
      this.curModule = parent.getCurModule();
      this.curNamespace = parent.getNamespace();
      this.curGrouping = parent.getCurGrouping();
      this.merge(parent);
   }

   public YangSchemaContext getSchemaContext() {
      return this.schemaContext;
   }

   public void setSchemaContext(YangSchemaContext schemaContext) {
      this.schemaContext = schemaContext;
   }

   public Module getCurModule() {
      return this.curModule;
   }

   public void setCurModule(Module curModule) {
      this.curModule = curModule;
   }

   public Grouping getCurGrouping() {
      return this.curGrouping;
   }

   public void setCurGrouping(Grouping curGrouping) {
      this.curGrouping = curGrouping;
   }

   public YangSpecification getYangSpecification() {
      YangSpecification yangSpecification = null;
      Module module = this.getCurModule();
      YangVersion yangVersion = module.getYangVersion();
      if (yangVersion != null && !yangVersion.getArgStr().equals("1")) {
         yangSpecification = YangSpecification.getVersion11Spec();
      } else {
         yangSpecification = YangSpecification.getVersion1Spec();
      }

      return yangSpecification;
   }

   public void merge(YangContext other) {
      this.mergedContexts.add(other);
   }

   public Typedef getTypedef(String name) {
      if (this.typedefIdentifierCache.containsKey(name)) {
         return (Typedef)this.typedefIdentifierCache.get(name);
      } else {
         Iterator var2 = this.mergedContexts.iterator();

         Typedef typedef;
         do {
            if (!var2.hasNext()) {
               return null;
            }

            YangContext mergedContext = (YangContext)var2.next();
            typedef = mergedContext.getTypedef(name);
         } while(typedef == null);

         return typedef;
      }
   }

   public Grouping getGrouping(String name) {
      if (this.groupingIdentifierCache.containsKey(name)) {
         return (Grouping)this.groupingIdentifierCache.get(name);
      } else {
         Iterator var2 = this.mergedContexts.iterator();

         Grouping grouping;
         do {
            if (!var2.hasNext()) {
               return null;
            }

            YangContext mergedContext = (YangContext)var2.next();
            grouping = mergedContext.getGrouping(name);
         } while(grouping == null);

         return grouping;
      }
   }

   public Map<String, SchemaNode> getSchemaNodeIdentifierCache() {
      return this.SchemaNodeIdentifierCache;
   }

   public Map<String, Grouping> getGroupingIdentifierCache() {
      return this.groupingIdentifierCache;
   }

   public Map<String, Typedef> getTypedefIdentifierCache() {
      return this.typedefIdentifierCache;
   }

   public Map<String, Extension> getExtensionCache() {
      return this.extensionCache;
   }

   public Map<String, Feature> getFeatureCache() {
      return this.featureCache;
   }

   public Map<String, Identity> getIdentityCache() {
      return this.identityCache;
   }

   public Namespace getNamespace() {
      return this.curNamespace;
   }

   public void setNamespace(Namespace curNamespace) {
      this.curNamespace = curNamespace;
   }

   public YangStatement getSelf() {
      return this.self;
   }

   public void setSelf(YangStatement self) {
      this.self = self;
   }
}
