package org.yangcentral.yangkit.util;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.Namespace;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.*;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelUtil {
   public static boolean isNullString(String str) {
      if (null == str) {
         return true;
      } else {
         return 0 == str.length();
      }
   }

   public static boolean isAlpha(char c) {
      return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
   }

   public static boolean isDigit(char c) {
      return c >= '0' && c <= '9';
   }

   public static boolean isDigit(String s) {
      if (null == s) {
         return false;
      } else {
         for(int i = 0; i < s.length(); ++i) {
            if (!isDigit(s.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isNodeIdentifier(String ref) {
      return isIdentifierRef(ref);
   }

   public static boolean isIdentifierRef(String ref) {
      if (null == ref) {
         return false;
      } else {
         FName fName = new FName(ref);
         if (fName.getPrefix() != null && !isIdentifier(fName.getPrefix())) {
            return false;
         } else {
            return isIdentifier(fName.getLocalName());
         }
      }
   }

   public static boolean isIdentifier(String id) {
      if (null == id) {
         return false;
      } else {
         String str = id.toLowerCase();
         if (str.startsWith("xml")) {
            return false;
         } else {
            int size = id.length();
            if (0 == size) {
               return false;
            } else {
               for(int i = 0; i < size; ++i) {
                  char c = id.charAt(i);
                  if (0 == i) {
                     if (!isAlpha(c) && c != '_') {
                        return false;
                     }
                  } else if (!isAlpha(c) && !isDigit(c) && c != '_' && c != '-' && c != '.') {
                     return false;
                  }
               }

               return true;
            }
         }
      }
   }

   public static boolean isDescendantSchemaNodeIdentifier(String path) {
      if (null == path) {
         return false;
      } else if (path.startsWith("/")) {
         return false;
      } else {
         boolean containAbsolutePath = false;
         String absoultePath = null;
         String startNode = null;
         if (path.contains("/")) {
            containAbsolutePath = true;
            int index = path.indexOf("/");
            startNode = path.substring(0, index);
            absoultePath = path.substring(index);
         } else {
            startNode = path;
         }

         if (!isNodeIdentifier(startNode)) {
            return false;
         } else {
            return containAbsolutePath ? isAbsoluteSchemaNodeIdentifier(absoultePath) : true;
         }
      }
   }

   public static boolean isAbsoluteSchemaNodeIdentifier(String path) {
      if (null == path) {
         return false;
      } else if (!path.startsWith("/")) {
         return false;
      } else if (path.length() <= 1) {
         return false;
      } else {
         String[] nodeIdentifiers = path.substring(1).split("/");
         if (null == nodeIdentifiers) {
            return isNodeIdentifier(path.substring(1));
         } else {
            int size = nodeIdentifiers.length;

            for(int i = 0; i < size; ++i) {
               String nodeName = nodeIdentifiers[i];
               if (null == nodeName) {
                  return false;
               }

               if (!isNodeIdentifier(nodeName)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public static String[] parseAbsoluteSchemaNodeIdentifier(String path) {
      if (!isAbsoluteSchemaNodeIdentifier(path)) {
         return null;
      } else {
         String[] nodeIdentifiers = path.substring(1).split("/");
         if (null == nodeIdentifiers) {
            String[] nodes = new String[]{path.substring(1)};
            return nodes;
         } else {
            return nodeIdentifiers;
         }
      }
   }

   public static String[] parseDescendantSchemaNodeIdentifier(String path) {
      if (!isDescendantSchemaNodeIdentifier(path)) {
         return null;
      } else {
         String[] nodeIdentifiers = path.split("/");
         if (null == nodeIdentifiers) {
            String[] nodes = new String[]{path};
            return nodes;
         } else {
            return nodeIdentifiers;
         }
      }
   }

   public static boolean isExtensionKeyword(String keyword) {
      if (null == keyword) {
         return false;
      } else if (!keyword.contains(":")) {
         return false;
      } else {
         String[] strs = keyword.split(":");
         if (2 != strs.length) {
            return false;
         } else {
            String identifier = strs[1];
            return isIdentifier(identifier);
         }
      }
   }

   public static boolean isYYYY_MM_DD(String sDate) {
      if (null == sDate) {
         return false;
      } else {
         String datePattern1 = "\\d{4}-\\d{2}-\\d{2}";
         String datePattern2 = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
         Pattern pattern = Pattern.compile(datePattern1);
         Matcher match = pattern.matcher(sDate);
         if (match.matches()) {
            pattern = Pattern.compile(datePattern2);
            match = pattern.matcher(sDate);
            return match.matches();
         } else {
            return false;
         }
      }
   }

   public static ValidatorRecord<Position, YangStatement> reportDuplicateError(YangStatement original, YangStatement duplicated, Severity severity) {
      ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
      validatorRecordBuilder.setBadElement(duplicated);
      validatorRecordBuilder.setSeverity(severity);
      validatorRecordBuilder.setErrorPath(duplicated.getElementPosition());
      validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
      validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName() + " in " + original.getElementPosition()));
      return validatorRecordBuilder.build();
   }

   public static ValidatorRecord<Position, YangStatement> reportDuplicateError(YangStatement original, YangStatement duplicated) {
      return reportDuplicateError(original, duplicated, Severity.ERROR);
   }

   public static <T extends YangStatement> T checkConflict(T candidate, List<T> original) {
      for(T t: original){
         if(t.getArgStr().equals(candidate.getArgStr())){
            return t;
         }
      }
      return null;
   }

   public static Module findModuleByPrefix(YangContext context, String prefix) throws ModelException {
      if (prefix != null && prefix.length() != 0) {
         if (context.getCurModule().isSelfPrefix(prefix)) {
            return context.getCurModule();
         } else {
            Module curModule = context.getCurModule();
            Optional<ModuleId> moduleIdOp = curModule.findModuleByPrefix(prefix);
            if (!moduleIdOp.isPresent()) {
               throw new ModelException(Severity.ERROR, context.getSelf(), ErrorCode.INVALID_PREFIX.toString(new String[]{"name=" + prefix}));
            } else {
               Optional<Module> moduleOptional = context.getSchemaContext().getModule((ModuleId)moduleIdOp.get());
               if (!moduleOptional.isPresent()) {
                  throw new ModelException(Severity.ERROR, context.getSelf(), ErrorCode.MISSING_DEPENDENCE_MODULE.toString(new String[]{"name=" + ((ModuleId)moduleIdOp.get()).getModuleName()}));
               } else {
                  Module targetModule = (Module)moduleOptional.get();
                  return targetModule;
               }
            }
         }
      } else {
         return context.getCurModule();
      }
   }

   public static Namespace getNamespace(Module module) {
      URI namespace = null;
      String prefix = null;
      if (module instanceof MainModule) {
         namespace = ((MainModule)module).getNamespace().getUri();
         prefix = ((MainModule)module).getPrefix().getArgStr();
      } else {
         SubModule sb = (SubModule)module;
         MainModule mainModule = (MainModule)sb.getBelongsto().getMainModules().get(0);
         namespace = mainModule.getNamespace().getUri();
         prefix = mainModule.getPrefix().getArgStr();
      }

      return new Namespace(namespace, prefix);
   }
}
