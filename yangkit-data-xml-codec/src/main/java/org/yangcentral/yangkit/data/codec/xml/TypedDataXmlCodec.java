package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.IdentityRef;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.yangcentral.yangkit.util.ModelUtil;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.yangcentral.yangkit.data.codec.xml.Constants.OPERATION;


abstract class TypedDataXmlCodec<S extends TypedDataNode,D extends TypedData<?,S>> extends YangDataXmlCodec<S,D> {
    protected TypedDataXmlCodec( S schemaNode) {
        super(schemaNode);
    }

    protected String translateStringValueForIdentityRef(Element element,String value) throws YangDataXmlCodecException {
        FName fName = new FName(value);

        String originalPrefix = fName.getPrefix();
        String yangPrefix = null;
        if(originalPrefix == null){
            yangPrefix = getSchemaNode().getContext().getCurModule().getSelfPrefix();
        }
        else {
            Namespace namespace = element.getNamespaceForPrefix(originalPrefix);
            if(element.getNamespace().equals(namespace)){
                //same module
                yangPrefix = getSchemaNode().getContext().getCurModule().getSelfPrefix();
            }
            else {
                //import module
                List<Module> moduleList = getSchemaNode().getContext().getSchemaContext().getModule(URI.create(namespace.getURI()));
                if(null == moduleList || moduleList.size() == 0){
                    throw new YangDataXmlCodecException(element.getUniquePath(),element,
                            ErrorTag.BAD_ELEMENT,"can not find a module with namespace:" + namespace.getURI());
                }
                Iterator<Map.Entry<String,ModuleId>> entrys = getSchemaNode().getContext().getCurModule().getPrefixes().entrySet().iterator();
                while (entrys.hasNext()){
                    Map.Entry<String,ModuleId> entry = entrys.next();
                    for(Module module:moduleList){
                        if(entry.getValue().equals(module.getModuleId())){
                            yangPrefix = entry.getKey();
                            break;
                        }
                    }
                    if(yangPrefix != null){
                        break;
                    }

                }
                if(yangPrefix == null){
                    yangPrefix = moduleList.get(0).getSelfPrefix();
                    // throw new YangDataXmlCodecException(element.getUniquePath(),element,
                    //     ErrorTag.BAD_ELEMENT,"can not find prefix for namespace:" + namespace.getURI());
                }

            }
        }
        return yangPrefix+":" +fName.getLocalName();
    }

    protected String getYangText(Element element) throws YangDataXmlCodecException {
        String operationType = element.attributeValue(OPERATION);
        String text = element.getTextTrim();
        
        // For leaf-list, check for value attribute as alternative encoding
        if (getSchemaNode() instanceof LeafList) {
            String valueAttr = element.attributeValue("value");
            if (valueAttr != null && !valueAttr.trim().isEmpty()) {
                text = valueAttr.trim();
            }
        }
        
        if(text== null || text.length() == 0){
            if(operationType == null || !(operationType.equals("delete") || operationType.equals("remove"))){
                // ValidatorRecordBuilder<String,Element> validatorRecordBuilder = new ValidatorRecordBuilder<>();
                // validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                // validatorRecordBuilder.setErrorPath(element.getUniquePath());
                // validatorRecordBuilder.setBadElement(element);
                // validatorRecordBuilder.setErrorMessage(new ErrorMessage("leaf data's value MUST NOT be empty."));
                // return null;
                return "";
            }
            text = null;
        } else {
            if(getSchemaNode().getType().getRestriction() instanceof IdentityRef){
                text = translateStringValueForIdentityRef(element,text);
            }
        }
        return text;
    }

    @Override
    protected void buildElement(Element element, YangData<?> yangData) {
        String value = ((TypedData) yangData).getStringValue();
        if(value == null){
            return ;
        }
        TypedData leafData = (TypedData) yangData;
        if(((TypedDataNode)(leafData.getSchemaNode())).getType().getRestriction() instanceof IdentityRef){
            try {
                QName qName = (QName) ((TypedData<?, ?>) yangData).getValue().getValue();
                element.addNamespace(qName.getPrefix(), qName.getNamespace().toString());
            } catch (YangCodecException e) {
                throw new RuntimeException(e);
            }

        }
        element.setText(value);
    }


}
