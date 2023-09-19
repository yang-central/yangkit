package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.ListData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.YangList;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import java.util.ArrayList;
import java.util.List;


public class ListDataXmlCodec extends YangDataXmlCodec<YangList, ListData> {
    protected ListDataXmlCodec(YangList schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ListData buildData(Element element, ValidatorResultBuilder validatorResultBuilder) {
        //key
        List<LeafData> keyDataList = new ArrayList<>();
        List<Leaf> keys = getSchemaNode().getKey().getkeyNodes();
        for(Leaf key:keys){
            Element keyElement = element.element(QName.get(key.getIdentifier().getLocalName(),
                    Namespace.get(key.getIdentifier().getNamespace().toString())));
            if(keyElement == null){
                ValidatorRecordBuilder<String, Element> recordBuilder = new ValidatorRecordBuilder<>();
                recordBuilder.setErrorTag(ErrorTag.MISSING_ELEMENT);
                recordBuilder.setErrorPath(element.getUniquePath());
                recordBuilder.setBadElement(element);
                recordBuilder.setErrorMessage(new ErrorMessage("missing key:" + key.getIdentifier().getLocalName()));
                validatorResultBuilder.addRecord(recordBuilder.build());
                return null;
            }
            YangDataXmlCodec xmlCodec = YangDataXmlCodec.getInstance(key);
            LeafData keyData = (LeafData) xmlCodec.deserialize(keyElement,validatorResultBuilder);
            keyDataList.add(keyData);
        }
        ListData listData = (ListData) YangDataBuilderFactory.getBuilder().getYangData(getSchemaNode(), keyDataList);
        return listData;
    }

    @Override
    protected void buildElement(Element element, YangData<?> yangData) {

    }

    @Override
    protected void serializeChildren(Element element, YangDataContainer yangDataContainer) {
        ListData listData = (ListData) yangDataContainer;
        YangList yangList = listData.getSchemaNode();
        //serialize key firstly
        for(LeafData key:listData.getKeys()){
            YangDataXmlCodec xmlCodec = getInstance(key.getSchemaNode());
            Element childElement = xmlCodec.serialize(key);
            element.add(childElement);
        }
        List<SchemaNode> schemaChildren = yangList.getTreeNodeChildren();
        for(SchemaNode dataChild:schemaChildren){
            if(dataChild instanceof Leaf){
                Leaf leaf = (Leaf) dataChild;
                if(leaf.isKey()){
                    continue;
                }
            }
            List<YangData<?>> childData= yangDataContainer
                    .getDataChildren(dataChild.getIdentifier());
            for(YangData<?> childDatum:childData){
                if(childDatum.isDummyNode()){
                    continue;
                }
                YangDataXmlCodec xmlCodec = getInstance(childDatum.getSchemaNode());
                Element childElement = xmlCodec.serialize(childDatum);
                element.add(childElement);
            }
        }
    }
}

