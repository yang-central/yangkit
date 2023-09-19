package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.dom4j.Element;

public class ContainerDataXmlCodec extends YangDataXmlCodec<Container, ContainerData> {
    public ContainerDataXmlCodec(Container schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ContainerData buildData(Element element, ValidatorResultBuilder validatorResultBuilder) {
        ContainerData containerData = (ContainerData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(),null);
        return containerData;
    }


    @Override
    protected void buildElement(Element element, YangData<?> yangData) {
    }

}


