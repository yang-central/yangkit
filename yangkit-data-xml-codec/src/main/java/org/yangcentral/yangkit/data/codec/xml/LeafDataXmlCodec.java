package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.dom4j.Element;

public class LeafDataXmlCodec extends TypedDataXmlCodec<Leaf, LeafData<?>> {
    protected LeafDataXmlCodec(Leaf schemaNode) {
        super(schemaNode);
    }

    @Override
    protected LeafData buildData(Element element, ValidatorResultBuilder validatorResultBuilder) {
        try {
            String yangText = getYangText(element);
            LeafData leafData = (LeafData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), yangText);
            return leafData;
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
