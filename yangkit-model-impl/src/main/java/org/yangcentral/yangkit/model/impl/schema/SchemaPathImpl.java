package org.yangcentral.yangkit.model.impl.schema;

import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
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
   private Stack<QName> steps = new Stack<>();

   public SchemaPathImpl(List<QName> steps) {
      for (QName step : steps) {
         this.steps.push(step);
      }
   }

   public SchemaPathImpl() {
   }

   public void addStep(QName step) {
      this.steps.push(step);
   }

   public QName getLast() {
      return this.steps.peek();
   }

   public List<QName> getPath() {
      return new ArrayList<>(this.steps);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      if (this.isAbsolute()) {
         sb.append("/");
      }

      boolean first = true;

      QName step;
      for(Iterator<QName> iterator = this.steps.iterator(); iterator.hasNext(); sb.append(step.getQualifiedName())) {
         step = iterator.next();
         if (!first) {
            sb.append("/");
         } else {
            first = false;
         }
      }

      return sb.toString();
   }


   public static SchemaPath from(SchemaNodeContainer contextNode, YangStatement yangStatement,String path) throws ModelException {
      boolean isAbsolute = path.startsWith("/");

      String[] steps = path.split("/");
      List<QName> stepList = new ArrayList<>();
      int length = steps.length;

      for(int i = 0; i < length; ++i) {
         String step = steps[i];
         step = step.trim();
         if (step.length() != 0) {
            FName fName = new FName(step);
            String prefix = fName.getPrefix();
            String localName = fName.getLocalName();
            URI namespace = null;
            if (prefix == null) {
               if(contextNode instanceof SchemaNode){
                  SchemaNode schemaNode = (SchemaNode) contextNode;
                  namespace = schemaNode.getContext().getNamespace().getUri();
               } else {
                  Module curModule = (Module) contextNode;
                  namespace = curModule.getMainModule().getNamespace().getUri();
               }
            } else {
               Module module = null;
               if(contextNode instanceof SchemaNode){
                  SchemaNode schemaNode = (SchemaNode) contextNode;
                  module = schemaNode.getContext().getCurModule();
               } else {
                  module = (Module) contextNode;
               }
               Import im = module.getImportByPrefix(prefix);
               if (im != null) {
                  im.addReference(yangStatement);
               }

               Optional<ModuleId> moduleIdOp = module.findModuleByPrefix(prefix);
               if (!moduleIdOp.isPresent()) {
                  throw new ModelException(Severity.ERROR, (isAbsolute ? module : (YangStatement)contextNode), "can't find the module which prefix:" + prefix + " points to");
               }

               Optional<Module> moduleOp = module.getContext().getSchemaContext().getModule(moduleIdOp.get());
               if (!moduleIdOp.isPresent()) {
                  throw new ModelException(Severity.ERROR, (isAbsolute ? module : (YangStatement)contextNode), "can't find the module which prefix:" + prefix + " points to");
               }

               Module sourceModule = moduleOp.get();
               namespace = sourceModule.getMainModule().getNamespace().getUri();
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
      List<SchemaNode> matched = new ArrayList<>();

      for (SchemaNode schemaNode : candidate) {
         if (schemaNode instanceof VirtualSchemaNode) {
            VirtualSchemaNode virtualSchemaNode = (VirtualSchemaNode) schemaNode;
            matched.addAll(this.match(virtualSchemaNode.getSchemaNodeChildren(), qName));
         } else if (schemaNode.getIdentifier().equals(qName)) {
            matched.add(schemaNode);
         }
      }

      return matched;
   }

   public SchemaNode getSchemaNode(YangSchemaContext schemaContext) {
      List<QName> pathElements = this.getPath();
      List<SchemaNode> candidate = new ArrayList<>();
      if (this.isAbsolute()) {
         candidate.addAll(schemaContext.getSchemaNodeChildren());
      } else {
         SchemaPath.Descendant descendant = (SchemaPath.Descendant)this;
         candidate.addAll(descendant.getContext().getSchemaNodeChildren());
      }

      List<SchemaNode> matched = null;

      for (QName pathElement : pathElements) {
         matched = this.match(candidate, pathElement);
         if (matched.size() == 0) {
            return null;
         }

         candidate.clear();

         for (SchemaNode matchedNode : matched) {
            if (matchedNode instanceof SchemaNodeContainer) {
               candidate.addAll(((SchemaNodeContainer) matchedNode).getSchemaNodeChildren());
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
