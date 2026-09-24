package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.LenientValidationOptions;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentityRefStringValueCodecImpl extends ComplexStringValueCodecImpl<QName> implements IdentityRefStringValueCodec {
   private static final Logger LOGGER = LoggerFactory.getLogger(IdentityRefStringValueCodecImpl.class);
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
               if (LenientValidationOptions.isEnabled()) {
                  LOGGER.warn("[IdentityRef] SubModule has no main modules: " + input);
                  return buildFallbackQName(prefix, fName.getLocalName());
               }
               throw new IllegalArgumentException(ErrorCode.INVALID_VALUE.getFieldName());
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
                if (LenientValidationOptions.isEnabled()) {
                   LOGGER.warn("[IdentityRef] Module not present in schema context for prefix: " + fName.getPrefix() + ": " + input);
                   return buildFallbackQName(prefix, fName.getLocalName());
                }
                throw new IllegalArgumentException(ErrorCode.INVALID_VALUE.getFieldName());
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
                if (LenientValidationOptions.isEnabled()) {
                   LOGGER.warn("[IdentityRef] No module found for prefix: " + prefix + ": " + input);
                   return buildFallbackQName(prefix, fName.getLocalName());
                }
                throw new IllegalArgumentException(ErrorCode.INVALID_VALUE.getFieldName());
            }
         }
      }

       QName qName = new QName(namespace, prefix, fName.getLocalName());
       // An identityref value that is not a valid identity of the declared base is an
       // error, regardless of lenient mode. Lenient handling is limited to genuinely
       // missing modules (see above) so an invalid value is never accepted.
       if (!restriction.evaluate(qName)) {
          throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
       }
       return qName;
    }

    public String serialize(Restriction<QName> restriction, QName output) throws YangCodecException {
       if (!restriction.evaluate(output)) {
          throw new YangCodecException(ErrorCode.INVALID_VALUE.getFieldName());
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
