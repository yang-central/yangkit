package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.exception.Severity;
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
        System.out.println("[DEBUG] LeafDataXmlCodec.buildData called for element: " + element.getName());
        try {
            String yangText = getYangText(element);
            System.out.println("[DEBUG] Yang text value: " + yangText);
            LeafData leafData = (LeafData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), yangText);
            // Trigger validation by getting the string value
            String val = leafData.getStringValue();
            System.out.println("[DEBUG] String value obtained successfully: " + val);
            return leafData;
        } catch (YangDataXmlCodecException e) {
            System.out.println("[DEBUG] Caught YangDataXmlCodecException: " + e.getErrorMsg());
            ValidatorRecordBuilder<String, Element> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setSeverity(Severity.ERROR);
            recordBuilder.setErrorTag(e.getErrorTag());
            recordBuilder.setErrorPath(e.getErrorPath());
            recordBuilder.setBadElement(e.getBadElement());
            recordBuilder.setErrorMessage(e.getErrorMsg());
            validatorResultBuilder.addRecord(recordBuilder.build());
            System.out.println("[DEBUG] Error record added to builder");
        } catch (Exception e) {
            // Catch YangCodecException and other exceptions from codec.deserialize()
            // Convert to validation error record with ERROR severity
            System.out.println("[DEBUG] Caught Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            ValidatorRecordBuilder<String, Element> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setSeverity(Severity.ERROR);
            recordBuilder.setErrorTag(org.yangcentral.yangkit.common.api.exception.ErrorTag.BAD_ELEMENT);
            recordBuilder.setErrorPath(element.getUniquePath());
            recordBuilder.setBadElement(element);
            recordBuilder.setErrorMessage(new org.yangcentral.yangkit.common.api.exception.ErrorMessage(
                "Invalid value: " + e.getMessage()));
            validatorResultBuilder.addRecord(recordBuilder.build());
            System.out.println("[DEBUG] Error record added to builder");
        }
        System.out.println("[DEBUG] Returning null from buildData");
        return null;
    }



}
