package org.yangcentral.yangkit.data.api.codec;

@FunctionalInterface
public interface AnydataValidationContextResolver {
   AnydataValidationContext resolve(AnydataValidationRequest request);
}

