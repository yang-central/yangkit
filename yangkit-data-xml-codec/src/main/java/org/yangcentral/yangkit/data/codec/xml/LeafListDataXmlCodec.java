package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.dom4j.Element;

public class LeafListDataXmlCodec extends TypedDataXmlCodec<LeafList, LeafListData<?>> {
    protected LeafListDataXmlCodec(LeafList schemaNode) {
        super(schemaNode);
    }

    @Override
    protected LeafListData buildData(Element element, ValidatorResultBuilder validatorResultBuilder) {
        try {
            String yangText = getYangText(element);
            LeafListData leafListData = (LeafListData) YangDataBuilderFactory.getBuilder()
                    .getYangData(getSchemaNode(),yangText);
            return leafListData;
        } catch (YangDataXmlCodecException e) {
            ValidatorRecordBuilder<String, Element> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(e.getErrorTag());
            recordBuilder.setErrorPath(e.getErrorPath());
            recordBuilder.setBadElement(e.getBadElement());
            recordBuilder.setErrorMessage(e.getErrorMsg());
            validatorResultBuilder.addRecord(recordBuilder.build());
        }
        return null;
    }
}

