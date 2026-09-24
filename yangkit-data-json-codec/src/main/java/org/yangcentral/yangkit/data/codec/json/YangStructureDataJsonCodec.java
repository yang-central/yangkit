package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.YangStructureData;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.ext.YangStructure;

import java.util.Iterator;
import java.util.Map;

public class YangStructureDataJsonCodec extends YangDataJsonCodec<YangStructure, YangStructureData> {
    protected YangStructureDataJsonCodec(YangStructure schemaNode) {
        super(schemaNode);
    }

    @Override
    protected YangStructureData buildData(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        YangStructureData yangStructureData =
                (YangStructureData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), null);
        // Build only the fields that are part of this structure. Fields that do not resolve here
        // (e.g. the notification payload next to "eventTime" in a notification message) are left
        // for the caller (e.g. NotificationMessageJsonCodec) and must not be reported as unknown.
        Iterator<Map.Entry<String, JsonNode>> fields = element.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            QName qName = JsonCodecUtil.getQNameFromJsonField(field.getKey(), yangStructureData);
            if (qName == null) {
                continue;
            }
            SchemaNode childSchemaNode = getSchemaNode().getTreeNodeChild(qName);
            if (childSchemaNode != null) {
                validatorResultBuilder.merge(JsonCodecUtil.buildChildData(yangStructureData, field.getValue(), childSchemaNode));
            }
        }
        return yangStructureData;
    }
}
