package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.codec.IdentityRefStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class IdentityRefStringValueCodecImpl extends ComplexStringValueCodecImpl<QName> implements IdentityRefStringValueCodec {
   private static final Logger LOGGER = Logger.getLogger(IdentityRefStringValueCodecImpl.class.getName());
   public IdentityRefStringValueCodecImpl(TypedDataNode schemaNode) {
      super(schemaNode);
   }

   public QName deserialize(Restriction<QName> restriction, String input) throws YangCodecException {
      FName fName = new FName(input);
      Module curModule = this.getSchemaNode().getContext().getCurModule();
      String prefix = null;
      URI namespace = null;
      List mainModuleList;
      if (fName.getPrefix() == null) {
         if (curModule instanceof MainModule) {
            namespace = ((MainModule)curModule).getNamespace().getUri();
            prefix = ((MainModule)curModule).getPrefix().getArgStr();
         } else {
            SubModule sb = (SubModule)curModule;
            mainModuleList = sb.getBelongsto().getMainModules();
            if (mainModuleList.size() == 0) {
               LOGGER.warning("[IdentityRef] SubModule has no main modules, treating as opaque: " + input);
               return buildFallbackQName(prefix, fName.getLocalName());
            }

            namespace = ((MainModule)mainModuleList.get(0)).getNamespace().getUri();
            prefix = sb.getBelongsto().getPrefix().getArgStr();
         }
      } else {
         prefix = fName.getPrefix();
         Optional<ModuleId> moduleIdOp = curModule.findModuleByPrefix(fName.getPrefix());
         Module module;
         if (moduleIdOp.isPresent()) {
            ModuleId moduleId = (ModuleId)moduleIdOp.get();
            Optional<Module> moduleOp = this.getSchemaNode().getContext().getSchemaContext().getModule(moduleId);
            if (!moduleOp.isPresent()) {
               LOGGER.warning("[IdentityRef] Module not present in schema context for prefix: " + fName.getPrefix() + ", treating as opaque: " + input);
               return buildFallbackQName(prefix, fName.getLocalName());
            }

            module = (Module)moduleOp.get();

            assert module instanceof MainModule;

            namespace = ((MainModule)module).getNamespace().getUri();
         } else {
            mainModuleList = curModule.getContext().getSchemaContext().getModules();
            Iterator iterator = mainModuleList.iterator();

            while(iterator.hasNext()) {
               module = (Module)iterator.next();
               if (module.getSelfPrefix().equals(prefix)) {
                  namespace = module.getMainModule().getNamespace().getUri();
               }
            }

            if (namespace == null) {
               LOGGER.warning("[IdentityRef] No module found for prefix: " + prefix + ", treating as opaque: " + input);
               return buildFallbackQName(prefix, fName.getLocalName());
            }
         }
      }

      QName qName = new QName(namespace, prefix, fName.getLocalName());
      try {
         if (!restriction.evaluate(qName)) {
            LOGGER.warning("[IdentityRef] Identity not found in restriction, treating as opaque: " + input);
         }
      } catch (Exception e) {
         LOGGER.warning("[IdentityRef] Restriction evaluation error for: " + input + " - " + e.getMessage());
      }
      return qName;
   }

   public String serialize(Restriction<QName> restriction, QName output) throws YangCodecException {
      try {
         if (!restriction.evaluate(output)) {
            LOGGER.warning("[IdentityRef] Serialize: identity not found in restriction, returning qualified name anyway.");
         }
      } catch (Exception e) {
         LOGGER.warning("[IdentityRef] Serialize: restriction evaluation error - " + e.getMessage());
      }
      return output.getQualifiedName();
   }

   private QName buildFallbackQName(String prefix, String localName) {
      try {
         URI fallbackNamespace = new URI("urn:unknown-module:" + (prefix != null ? prefix : "unknown"));
         return new QName(fallbackNamespace, prefix != null ? prefix : "", localName);
      } catch (Exception e) {
         return new QName((URI) null, prefix != null ? prefix : "", localName);
      }
   }
}
