package org.yangcentral.yangkit.model.impl.schema;

import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.VirtualSchemaNode;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

public abstract class SchemaPathImpl implements SchemaPath {
   private Stack<QName> steps = new Stack();

   public SchemaPathImpl(List<QName> steps) {
      Iterator var2 = steps.iterator();

      while(var2.hasNext()) {
         QName step = (QName)var2.next();
         this.steps.push(step);
      }

   }

   public SchemaPathImpl() {
   }

   public void addStep(QName step) {
      this.steps.push(step);
   }

   public QName getLast() {
      return (QName)this.steps.peek();
   }

   public List<QName> getPath() {
      return new ArrayList(this.steps);
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      if (this.isAbsolute()) {
         sb.append("/");
      }

      boolean first = true;

      QName step;
      for(Iterator var3 = this.steps.iterator(); var3.hasNext(); sb.append(step.getQualifiedName())) {
         step = (QName)var3.next();
         if (!first) {
            sb.append("/");
         } else {
            first = false;
         }
      }

      return sb.toString();
   }

   public static SchemaPath from(Module module, SchemaNodeContainer contextNode, String path) throws ModelException {
      boolean isAbsolute = false;
      if (path.startsWith("/")) {
         isAbsolute = true;
      }

      String[] steps = path.split("/");
      List<QName> stepList = new ArrayList();
      String[] var6 = steps;
      int var7 = steps.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         String step = var6[var8];
         step = step.trim();
         if (step.length() != 0) {
            FName fName = new FName(step);
            String prefix = fName.getPrefix();
            String localName = fName.getLocalName();
            URI namespace = null;
            Module sourceModule = null;
            if (prefix == null) {
               sourceModule = module;
            } else {
               Import im = module.getImportByPrefix(prefix);
               if (im != null) {
                  im.setReferenced(true);
               }

               Optional<ModuleId> moduleIdOp = module.findModuleByPrefix(prefix);
               if (!moduleIdOp.isPresent()) {
                  throw new ModelException(Severity.ERROR, (YangStatement)(isAbsolute ? module : (YangStatement)contextNode), "can't find the module which prefix:" + prefix + " points to");
               }

               Optional<Module> moduleOp = module.getContext().getSchemaContext().getModule((ModuleId)moduleIdOp.get());
               if (!moduleIdOp.isPresent()) {
                  throw new ModelException(Severity.ERROR, (YangStatement)(isAbsolute ? module : (YangStatement)contextNode), "can't find the module which prefix:" + prefix + " points to");
               }

               sourceModule = (Module)moduleOp.get();
            }

            if (sourceModule instanceof MainModule) {
               namespace = ((MainModule)sourceModule).getNamespace().getUri();
               if (null == prefix) {
                  prefix = ((MainModule)sourceModule).getPrefix().getArgStr();
               }
            } else {
               SubModule sb = (SubModule)sourceModule;
               MainModule mainModule = (MainModule)sb.getBelongsto().getMainModules().get(0);
               namespace = mainModule.getNamespace().getUri();
               if (null == prefix) {
                  prefix = sb.getBelongsto().getPrefix().getArgStr();
               }
            }

            QName qName = new QName(namespace, prefix, localName);
            stepList.add(qName);
         }
      }

      if (isAbsolute) {
         return new AbsoluteSchemaPath(stepList);
      } else {
         return new DescendantSchemaPath(stepList, contextNode);
      }
   }

   private List<SchemaNode> match(List<SchemaNode> candidate, QName qName) {
      List<SchemaNode> matched = new ArrayList();
      Iterator var4 = candidate.iterator();

      while(var4.hasNext()) {
         SchemaNode schemaNode = (SchemaNode)var4.next();
         if (schemaNode instanceof VirtualSchemaNode) {
            VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode)schemaNode;
            matched.addAll(this.match(virtualSchemaNode.getSchemaNodeChildren(), qName));
         } else if (schemaNode.getIdentifier().equals(qName)) {
            matched.add(schemaNode);
         }
      }

      return matched;
   }

   public SchemaNode getSchemaNode(YangSchemaContext schemaContext) {
      List<QName> pathElements = this.getPath();
      List<SchemaNode> candidate = new ArrayList();
      if (this.isAbsolute()) {
         candidate.addAll(schemaContext.getSchemaNodeChildren());
      } else {
         SchemaPath.Descendant descendant = (SchemaPath.Descendant)this;
         candidate.addAll(descendant.getContext().getSchemaNodeChildren());
      }

      List<SchemaNode> matched = null;
      Iterator var5 = pathElements.iterator();

      while(var5.hasNext()) {
         QName pathElement = (QName)var5.next();
         matched = this.match(candidate, pathElement);
         if (matched.size() == 0) {
            return null;
         }

         candidate.clear();
         Iterator var7 = matched.iterator();

         while(var7.hasNext()) {
            SchemaNode matchedNode = (SchemaNode)var7.next();
            if (matchedNode instanceof SchemaNodeContainer) {
               candidate.addAll(((SchemaNodeContainer)matchedNode).getSchemaNodeChildren());
            }
         }
      }

      if (null == matched) {
         return null;
      } else {
         assert matched.size() == 1;

         return (SchemaNode)matched.get(0);
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof SchemaPathImpl)) {
         return false;
      } else {
         SchemaPathImpl that = (SchemaPathImpl)o;
         return Objects.equals(this.steps, that.steps);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.steps});
   }
}
