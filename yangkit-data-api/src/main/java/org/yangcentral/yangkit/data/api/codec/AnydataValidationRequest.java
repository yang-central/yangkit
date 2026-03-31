package org.yangcentral.yangkit.data.api.codec;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Anydata;

public class AnydataValidationRequest {
   private final Anydata schemaNode;
   private final QName schemaNodeIdentifier;
   private final String sourcePath;
   private final YangSchemaContext documentSchemaContext;

   public AnydataValidationRequest(Anydata schemaNode, String sourcePath, YangSchemaContext documentSchemaContext) {
      this.schemaNode = schemaNode;
      this.schemaNodeIdentifier = schemaNode == null ? null : schemaNode.getIdentifier();
      this.sourcePath = sourcePath;
      this.documentSchemaContext = documentSchemaContext;
   }

   public Anydata getSchemaNode() {
      return schemaNode;
   }

   public QName getSchemaNodeIdentifier() {
      return schemaNodeIdentifier;
   }

   public String getSourcePath() {
      return sourcePath;
   }

   public YangSchemaContext getDocumentSchemaContext() {
      return documentSchemaContext;
   }
}

