package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.Namespace;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.codec.YangDataDocumentCodec;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.model.YangDataEntity;
import org.yangcentral.yangkit.data.api.operation.YangDataOperator;
import org.yangcentral.yangkit.data.impl.model.YangDataDocumentImpl;
import org.yangcentral.yangkit.data.impl.operation.YangDataOperatorImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.*;

public class YangDataDocumentJsonCodec implements YangDataDocumentCodec<JsonNode> {

    private YangSchemaContext yangSchemaContext;

    public YangDataDocumentJsonCodec(YangSchemaContext yangSchemaContext) {
        this.yangSchemaContext = yangSchemaContext;
    }

    @Override
    public YangSchemaContext getSchemaContext() {
        return yangSchemaContext;
    }


    @Override
    public YangDataDocument deserialize(JsonNode element, ValidatorResultBuilder validatorResultBuilder) {
        if (element.isNull()) {
            return null;
        }
        int size = 0;
        Iterator<String> fields = element.fieldNames();
        String field = null;
        while (fields.hasNext()) {
            field = fields.next();
            size++;
        }
        if(size == 0) {
            return null;
        }
        if(size > 1) {
            ValidatorRecordBuilder<String, JsonNode> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            recordBuilder.setErrorPath(JsonCodecUtil.getJsonPath(element));
            recordBuilder.setBadElement(element);
            recordBuilder.setErrorMessage(new ErrorMessage("wrong format, only one root node can be allowed."));
            validatorResultBuilder.addRecord(recordBuilder.build());
            return null;
        }
        QName qName = JsonCodecUtil.getQNameFromJsonField(field,yangSchemaContext);
        JsonNode rootElement = element;
        if(qName == null){
            qName = new QName("urn:ietf:params:xml:ns:yang:ietf-restconf","ietf-restconf","data");
            rootElement = element.get(field);
        }
        YangDataDocument yangDataDocument = new YangDataDocumentImpl(qName, yangSchemaContext, element.toString());

        validatorResultBuilder.merge(JsonCodecUtil.buildChildrenData(yangDataDocument, rootElement));
        return yangDataDocument;
    }

    @Override
    public JsonNode serialize(YangDataDocument yangDataDocument) {
        if (null == yangDataDocument) {
            return null;
        }
        List<Attribute> list = yangDataDocument.getAttributes();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ObjectNode dataNode = mapper.createObjectNode();
        QName qName = yangDataDocument.getQName();
        if(qName == null){
            qName = new QName("urn:ietf:params:xml:ns:yang:ietf-restconf","ietf-restconf","data");
        }
        root.put(JsonCodecUtil.getJsonFieldFromQName(qName,yangDataDocument),dataNode);
        JsonCodecUtil.serializeChildren(dataNode,yangDataDocument);
        return root;
    }


}