package org.yangcentral.yangkit.data.api.codec;

import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public class DefaultAnydataValidationContext implements AnydataValidationContext {
   private final YangSchemaContext schemaContext;

   public DefaultAnydataValidationContext(YangSchemaContext schemaContext) {
      this.schemaContext = schemaContext;
   }

   @Override
   public YangSchemaContext getSchemaContext() {
      return schemaContext;
   }
}

