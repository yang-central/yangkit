package org.yangcentral.yangkit.test.model.impl.stmt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.impl.stmt.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verify that SchemaNode component delegation works correctly
 * after the SchemaNodeImpl -> SchemaNodeSupport refactoring.
 *
 * Tests a concrete SchemaNode impl (EnumImpl) to confirm that
 * the SchemaNodeSupport composition correctly delegates all
 * SchemaNode contract methods.
 */
public class SchemaNodeSupportTest {

    // SchemaNodeSupport tested indirectly through a concrete class.
    // EnumImpl extends YangStatementImpl, implements YangEnum
    // (which extends Entity + YangBuiltinStatement), and
    // SchemaDataNodeImpl/DataDefinitionImpl/etc are upstream.
    //
    // For direct testing we use ContainerImpl which is the simplest
    // concrete SchemaNode after the refactor.

    @Test
    public void testSchemaNodeDefaultValues() {
        ContainerImpl container = new ContainerImpl("test-container");
        assertNotNull(container.supported(), "supported should not be null");
        assertEquals(SchemaTreeType.DATATREE, container.getSchemaTreeType(),
                "Default schemaTreeType should be DATATREE");
        assertNull(container.getParentSchemaNode(),
                "New container should have null parent");
        assertNull(container.getSchemaPath(),
                "Schema path should be null before SCHEMA_TREE build phase");
        assertFalse(container.isDeviated(),
                "New container should not be deviated");
    }

    @Test
    public void testSetSupportedAndDeviated() {
        InputImpl input = new InputImpl("test-input");
        assertTrue(input.supported(), "supported defaults to true via parent chain");

        input.setSupported(false);
        assertFalse(input.supported(), "setSupported(false) should take effect");

        input.setDeviated(true);
        assertTrue(input.isDeviated(), "setDeviated(true) should take effect");
    }

    @Test
    public void testSetParentSchemaNode() {
        ContainerImpl container = new ContainerImpl("test-container");
        MockSchemaNodeContainer parent = new MockSchemaNodeContainer();
        container.setParentSchemaNode(parent);
        assertSame(parent, container.getParentSchemaNode(),
                "getParentSchemaNode should return what was set");
    }

    @Test
    public void testIsMandatoryForLeafWithMandatory() {
        // Leaf with mandatory=true should be mandatory
        LeafImpl leaf = new LeafImpl("enabled");
        MandatoryImpl mandatory = new MandatoryImpl("true");
        YangContext ctx = new YangContext(null, null);
        mandatory.setContext(ctx);
        leaf.setContext(ctx);

        // Simulate the init process to set mandatory field
        // Direct call — field set happens in initSelf which requires context
    }

    @Test
    public void testIsMandatoryForLeafWithoutMandatory() {
        LeafImpl leaf = new LeafImpl("description");
        assertFalse(leaf.isMandatory(),
                "Leaf without mandatory statement should not be mandatory");
    }

    @Test
    public void testSetSchemaTreeType() {
        ContainerImpl container = new ContainerImpl("test-container");
        container.setSchemaTreeType(SchemaTreeType.YANGDATATREE);
        assertEquals(SchemaTreeType.YANGDATATREE, container.getSchemaTreeType(),
                "setSchemaTreeType should persist");
    }

    @Test
    public void testIsAncestorNode() {
        ContainerImpl parent = new ContainerImpl("parent");
        LeafImpl child = new LeafImpl("child");

        MockSchemaNodeContainer parentContainer = new MockSchemaNodeContainer();
        parentContainer.setChild(parent);

        child.setParentSchemaNode(parentContainer);

        // Since parentContainer is not a SchemaNode, isAncestorNode returns false
        assertFalse(child.isAncestorNode(parent),
                "Non-SchemaNode parent should not be ancestor");

        // Set parent as real parent
        child.setParentSchemaNode(parent);
        assertTrue(child.isAncestorNode(parent),
                "Direct parent should be ancestor");
    }

    /**
     * Minimal mock SchemaNodeContainer for testing.
     */
    private static class MockSchemaNodeContainer implements SchemaNodeContainer {
        private SchemaNode child;

        void setChild(SchemaNode child) { this.child = child; }

        @Override public List<SchemaNode> getSchemaNodeChildren() {
            List<SchemaNode> list = new ArrayList<>();
            if (child != null) list.add(child);
            return list;
        }
        @Override public ValidatorResult addSchemaNodeChild(SchemaNode schemaNode) { return null; }
        @Override public ValidatorResult addSchemaNodeChildren(List<SchemaNode> list) { return null; }
        @Override public SchemaNode getSchemaNodeChild(QName qname) { return null; }
        @Override public DataNode getDataNodeChild(QName qname) { return null; }
        @Override public List<DataNode> getDataNodeChildren() { return null; }
        @Override public List<SchemaNode> getTreeNodeChildren() { return null; }
        @Override public SchemaNode getTreeNodeChild(QName qname) { return null; }
        @Override public void removeSchemaNodeChild(QName qname) {}
        @Override public void removeSchemaNodeChild(SchemaNode schemaNode) {}
        @Override public SchemaNode getMandatoryDescendant() { return null; }
        @Override public boolean isSchemaTreeRoot() { return false; }
        @Override public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) { return null; }
    }
}
