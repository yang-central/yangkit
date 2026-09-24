package org.yangcentral.yangkit.data.api.codec;

import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Anydata;

/**
 * Resolves the schema used to parse and validate an {@code anydata} payload.
 */
public final class AnydataValidationSupport {
    private AnydataValidationSupport() {
    }

    /**
     * Resolves a payload schema and records a validation error when none is configured.
     *
     * @param schemaNode enclosing anydata schema node
     * @param sourcePath source path of the payload
     * @param documentSchemaContext schema context of the enclosing document
     * @param resolver configured payload-schema resolver
     * @param badElement source payload used by codec error handling
     * @param validatorResultBuilder destination for validation errors
     * @return resolved payload schema, or {@code null} when resolution fails
     */
    public static YangSchemaContext resolveSchemaContext(
            Anydata schemaNode,
            String sourcePath,
            YangSchemaContext documentSchemaContext,
            AnydataValidationContextResolver resolver,
            Object badElement,
            ValidatorResultBuilder validatorResultBuilder) {
        AnydataValidationRequest request =
                new AnydataValidationRequest(schemaNode, sourcePath, documentSchemaContext);
        AnydataValidationContext context = resolver == null ? null : resolver.resolve(request);
        if (context != null && context.getSchemaContext() != null) {
            return context.getSchemaContext();
        }

        ValidatorRecordBuilder<String, Object> recordBuilder = new ValidatorRecordBuilder<>();
        recordBuilder.setErrorTag(ErrorTag.OPERATION_FAILED);
        recordBuilder.setErrorPath(sourcePath);
        recordBuilder.setBadElement(badElement);
        recordBuilder.setErrorMessage(new ErrorMessage(
                "No payload schema is registered for anydata node: "
                        + schemaNode.getIdentifier().getQualifiedName()));
        validatorResultBuilder.addRecord(recordBuilder.build());
        return null;
    }

    /**
     * Records invalid structured content for an {@code anydata} node.
     *
     * @param schemaNode enclosing anydata schema node
     * @param sourcePath source path of the payload
     * @param badElement invalid payload value
     * @param message validation message
     * @param validatorResultBuilder destination for validation errors
     */
    public static void recordInvalidContent(
            Anydata schemaNode,
            String sourcePath,
            Object badElement,
            String message,
            ValidatorResultBuilder validatorResultBuilder) {
        ValidatorRecordBuilder<String, Object> recordBuilder = new ValidatorRecordBuilder<>();
        recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
        recordBuilder.setErrorPath(sourcePath);
        recordBuilder.setBadElement(badElement);
        recordBuilder.setErrorMessage(new ErrorMessage(
                "Invalid content for anydata node "
                        + schemaNode.getIdentifier().getQualifiedName() + ": " + message));
        validatorResultBuilder.addRecord(recordBuilder.build());
    }
}
