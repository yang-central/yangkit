package org.yangcentral.yangkit.data.codec.json.test;

import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Operation;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.VirtualSchemaNode;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaPathLifecycleTest {
    @Test
    public void schemaTreeBuildsDataOperationAndNotificationPaths() throws Exception {
        YangSchemaContext context = YangYinParser.parse(getClass().getClassLoader()
                .getResource("schema-path/yang").getFile());
        assertTrue(context.validate().isOk());
        for (SchemaNode node : context.getSchemaNodeChildren()) {
            assertPaths(node, new ArrayList<>());
        }
        Module module = context.getModules().get(0);
        Operation empty = (Operation) module.getSchemaNodeChild(new QName("urn:test:schema-path", "empty"));
        assertNotNull(empty);
        SchemaNode input = empty.getSchemaNodeChild(new QName("urn:test:schema-path", "input"));
        SchemaNode output = empty.getSchemaNodeChild(new QName("urn:test:schema-path", "output"));
        assertNotNull(input);
        assertNotNull(output);
        assertEquals("/sp:empty/sp:input", input.getSchemaPath().toString());
        assertEquals("/sp:empty/sp:output", output.getSchemaPath().toString());
    }

    @Test
    public void structureAndAugmentedChildrenHaveSchemaPaths() throws Exception {
        YangSchemaContext context = YangYinParser.parse(getClass().getClassLoader()
                .getResource("structure/yang").getFile());
        assertTrue(context.validate().isOk());
        int structures = 0;
        SchemaNode augmentedLeaf = null;
        for (Module module : context.getModules()) {
            for (YangUnknown unknown : module.getUnknowns()) {
                if (unknown instanceof YangStructure) {
                    structures++;
                    assertPaths((YangStructure) unknown, new ArrayList<>());
                    SchemaNodeContainer payload = (SchemaNodeContainer) ((YangStructure) unknown)
                            .getTreeNodeChild(new QName("urn:example:test-structure", "payload"));
                    if (payload != null) {
                        augmentedLeaf = payload.getTreeNodeChild(new QName("urn:test:schema-path-augment", "extra"));
                    }
                }
            }
        }
        assertTrue(structures > 0);
        assertNotNull(augmentedLeaf);
        assertEquals("/ts:message/ts:payload/spa:extra", augmentedLeaf.getSchemaPath().toString());
    }

    private void assertPaths(SchemaNode node, List<QName> ancestors) {
        List<QName> expected = new ArrayList<>(ancestors);
        if (node instanceof VirtualSchemaNode) {
            assertNull(node.getSchemaPath(), node.getArgStr());
        } else {
            expected.add(node.getIdentifier());
            assertNotNull(node.getSchemaPath(), node.getArgStr());
            assertEquals(expected, node.getSchemaPath().getPath(), node.getArgStr());
        }
        if (node instanceof SchemaNodeContainer) {
            for (SchemaNode child : ((SchemaNodeContainer) node).getSchemaNodeChildren()) {
                assertPaths(child, expected);
            }
        }
    }
}
