package org.yangcentral.yangkit.data.api.codec;

@FunctionalInterface
public interface AnydataValidationContextResolver {
   AnydataValidationContext resolve(AnydataValidationRequest request);

   /**
    * Returns whether resolving a payload schema is mandatory when no context is returned.
    */
   default boolean isRequirePayloadSchema() {
      return false;
   }
}
