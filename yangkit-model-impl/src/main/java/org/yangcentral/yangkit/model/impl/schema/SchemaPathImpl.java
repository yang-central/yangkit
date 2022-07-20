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
      Iterator iterator = steps.iterator();

      while(iterator.hasNext()) {
         QName step = (QName)iterator.next();
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
      for(Iterator iterator = this.steps.iterator(); iterator.hasNext(); sb.append(step.getQualifiedName())) {
         step = (QName)iterator.next();
         if (!first) {
            sb.append("/");
         } else {
            first = false;
         }
      }

      return sb.toString();
   }

   public static SchemaPath from(Module module, SchemaNodeContainer contextNode, YangStatement yangStatement,String path) throws ModelException {
      boolean isAbsolute = false;
      if (path.startsWith("/")) {
         isAbsolute = true;
      }

      String[] steps = path.split("/");
      List<QName> stepList = new ArrayList();
      int length = steps.length;

      for(int i = 0; i < length; ++i) {
         String step = steps[i];
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
                  im.addReference(yangStatement);
               }

               Optional<ModuleId> moduleIdOp = module.findModuleByPrefix(prefix);
               if (!moduleIdOp.isPresent()) {
                  throw new ModelException(Severity.ERROR, (isAbsolute ? module : (YangStatement)contextNode), "can't find the module which prefix:" + prefix + " points to");
               }

               Optional<Module> moduleOp = module.getContext().getSchemaContext().getModule((ModuleId)moduleIdOp.get());
               if (!moduleIdOp.isPresent()) {
                  throw new ModelException(Severity.ERROR, (isAbsolute ? module : (YangStatement)contextNode), "can't find the module which prefix:" + prefix + " points to");
               }

               sourceModule = moduleOp.get();
            }

            if (sourceModule instanceof MainModule) {
               namespace = ((MainModule)sourceModule).getNamespace().getUri();
               if (null == prefix) {
                  prefix = ((MainModule)sourceModule).getPrefix().getArgStr();
               }
            } else {
               SubModule sb = (SubModule)sourceModule;
               MainModule mainModule = sb.getBelongsto().getMainModules().get(0);
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
      Iterator iterator = candidate.iterator();

      while(iterator.hasNext()) {
         SchemaNode schemaNode = (SchemaNode)iterator.next();
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
      Iterator iterator = pathElements.iterator();

      while(iterator.hasNext()) {
         QName pathElement = (QName)iterator.next();
         matched = this.match(candidate, pathElement);
         if (matched.size() == 0) {
            return null;
         }

         candidate.clear();
         Iterator matchedIt = matched.iterator();

         while(matchedIt.hasNext()) {
            SchemaNode matchedNode = (SchemaNode)matchedIt.next();
            if (matchedNode instanceof SchemaNodeContainer) {
               candidate.addAll(((SchemaNodeContainer)matchedNode).getSchemaNodeChildren());
            }
         }
      }

      if (null == matched) {
         return null;
      } else {
         assert matched.size() == 1;

         return matched.get(0);
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
