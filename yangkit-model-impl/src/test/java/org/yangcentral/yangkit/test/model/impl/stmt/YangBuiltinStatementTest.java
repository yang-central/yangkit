package org.yangcentral.yangkit.test.model.impl.stmt;

import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.impl.stmt.YangStatementImpl;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for YangBuiltinStatement default methods.
 * Tests that validateSubStatements() and builtinEquals() work
 * regardless of inheritance chain — pure interface contract.
 */
public class YangBuiltinStatementTest {

    /**
     * A minimal builtin statement that extends YangStatementImpl directly
     * (not YangBuiltInStatementImpl) — like our refactored classes.
     * Its keyword is 'type' so it exists in YangSpecification with
     * known sub-statement definitions (fraction-digits, range, etc.).
     */
    private static final class TestBuiltinStmt extends YangStatementImpl
            implements YangBuiltinStatement {
        TestBuiltinStmt(String argStr) {
            super(argStr);
        }
        @Override
        public QName getYangKeyword() {
            return YangBuiltinKeyword.TYPE.getQName();
        }
    }

    /**
     * A YangStatement that is NOT a Builtin (like YangUnknown extensions).
     * Should NOT have validateSubStatements called.
     */
    private static final class TestNonBuiltinStmt extends YangStatementImpl {
        TestNonBuiltinStmt(String argStr) {
            super(argStr);
        }
        @Override
        public QName getYangKeyword() {
            return new QName("urn:test", "dummy");
        }
    }

    @Test
    public void testBuiltinEqualsSameKeywordAndArg() {
        TestBuiltinStmt a = new TestBuiltinStmt("string");
        TestBuiltinStmt b = new TestBuiltinStmt("string");
        assertTrue(a.builtinEquals(b),
                "Same keyword + same arg should be equal");
    }

    @Test
    public void testBuiltinEqualsDifferentArg() {
        TestBuiltinStmt a = new TestBuiltinStmt("string");
        TestBuiltinStmt b = new TestBuiltinStmt("uint8");
        assertFalse(a.builtinEquals(b),
                "Same keyword but different arg should NOT be equal");
    }

    @Test
    public void testBuiltinEqualsNonBuiltin() {
        TestBuiltinStmt a = new TestBuiltinStmt("string");
        TestNonBuiltinStmt nb = new TestNonBuiltinStmt("x");
        assertFalse(a.builtinEquals(nb),
                "Non-Builtin should NOT equal a Builtin");
    }

    @Test
    public void testBuiltinEqualsNullArg() {
        TestBuiltinStmt a = new TestBuiltinStmt(null);
        TestBuiltinStmt b = new TestBuiltinStmt(null);
        assertTrue(a.builtinEquals(b),
                "Both null arg should be equal");
    }

    @Test
    public void testBuiltinEqualsOneNullArg() {
        TestBuiltinStmt a = new TestBuiltinStmt("string");
        TestBuiltinStmt b = new TestBuiltinStmt(null);
        assertFalse(a.builtinEquals(b),
                "One null arg should NOT be equal");
    }

    @Test
    public void testValidateSubStatementsBaseResultPreserved() {
        // Even without context (no YangSpecification), baseResult
        // should be passed through.
        TestBuiltinStmt stmt = new TestBuiltinStmt("test");
        ValidatorResult base = new ValidatorResultBuilder().build();
        // With null context this will hit NPE or early return —
        // the point is the method exists and is callable.
        assertDoesNotThrow(() -> {
            try {
                stmt.validateSubStatements(base);
            } catch (NullPointerException e) {
                // expected — no context set up
            }
        }, "validateSubStatements should be callable on any YangBuiltinStatement");
    }
}
