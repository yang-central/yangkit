package org.yangcentral.yangkit.data.codec.json;

/**
 * Thread-local context manager for ExtraValidationDataJsonCodec.
 * This allows getJsonPath() to access parent-child relationships
 * without explicitly passing the context around.
 */
public class ExtraValidationDataContext {
    
    private static final ThreadLocal<ExtraValidationDataJsonCodec> currentContext = 
        new ThreadLocal<>();
    
    /**
     * Set the current validation context for this thread.
     * Should be called at the beginning of deserialization.
     * 
     * @param context the validation context to use
     */
    public static void setCurrentContext(ExtraValidationDataJsonCodec context) {
        currentContext.set(context);
    }
    
    /**
     * Get the current validation context for this thread.
     * Returns null if no context is set.
     * 
     * @return the current context, or null if not available
     */
    public static ExtraValidationDataJsonCodec getCurrentContext() {
        return currentContext.get();
    }
    
    /**
     * Clear the current context.
     * Should be called after deserialization is complete.
     */
    public static void clearContext() {
        currentContext.remove();
    }
}
