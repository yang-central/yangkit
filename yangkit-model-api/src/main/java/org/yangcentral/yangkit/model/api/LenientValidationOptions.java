package org.yangcentral.yangkit.model.api;

/** Per-schema validation policy; strict behavior is the default. */
public final class LenientValidationOptions {
    public static final LenientValidationOptions STRICT = new LenientValidationOptions(false);
    public static final LenientValidationOptions LENIENT = new LenientValidationOptions(true);

    private final boolean enabled;

    private LenientValidationOptions(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
