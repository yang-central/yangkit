package org.yangcentral.yangkit.model.api;

/**
 * Thread-local opt-in switch for lenient (lax) parsing and validation.
 *
 * <p><b>Strict is the default.</b> By default all yangkit codecs and validators report
 * unknown elements, inactive (if-feature / deviated) nodes, unresolved identityref/leafref
 * targets and missing list keys as errors.
 *
 * <p>Consumers that legitimately operate on partial schemas — for example a YANG Library
 * (RFC 8525) based deserializer that only has a subset of the device's modules — may
 * enable lenient mode for the duration of a parse/validate call:
 *
 * <pre>
 * try {
 *     LenientValidationOptions.enable();
 *     YangDataDocument doc = parser.parse(jsonNode, resultBuilder);
 *     doc.validate();
 * } finally {
 *     LenientValidationOptions.disable();
 * }
 * </pre>
 *
 * <p>In lenient mode the same situations are downgraded to warnings (or silently
 * accepted) so that the rest of the document is still processed.
 *
 * <p>The flag is thread-confined: enabling it in one thread never affects other threads.
 */
public final class LenientValidationOptions {

    /** Strict is the default behavior. */
    private static final ThreadLocal<Boolean> LENIENT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private LenientValidationOptions() {
    }

    /** Enables lenient behavior on the current thread (default is strict). */
    public static void enable() {
        LENIENT.set(Boolean.TRUE);
    }

    /** Restores strict behavior on the current thread. */
    public static void disable() {
        LENIENT.set(Boolean.FALSE);
    }

    /** Alias for {@link #disable()} — resets the thread to the default (strict) behavior. */
    public static void clear() {
        LENIENT.remove();
    }

    /** @return true when lenient behavior is active on the current thread. */
    public static boolean isEnabled() {
        return Boolean.TRUE.equals(LENIENT.get());
    }
}
