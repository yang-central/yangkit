package org.yangcentral.yangkit.model.api.schema;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;

import java.util.List;
/**
 * the interface for schema path, like ‘if:interfaces/if:interface/if:name’
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface SchemaPath {
   QName getLast();

   String toString();

   List<QName> getPath();

   void addStep(QName var1);

   boolean isAbsolute();

   SchemaNode getSchemaNode(YangSchemaContext schemaContext);
   /**
    * interface for descendant schema path, like ipv4/ip-address
    * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-6.5"></a>
    * @version 1.0.0
    * @author frank feng
    * @since 7/8/2022
    */
   public interface Descendant extends SchemaPath {
      SchemaNodeContainer getContext();
   }
   /**
    * intface for absolute schema path like "/if:interfaces/if:interface"
    * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-6.5"/>
    * @version 1.0.0
    * @author frank feng
    * @since 7/8/2022
    */
   public interface Absolute extends SchemaPath {
      boolean contains(Absolute path);

      List<QName> getRelativeSchemaPath(Absolute path);
   }
}
