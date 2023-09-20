package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.restriction.IdentityRef;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.dom4j.Element;
import org.dom4j.Namespace;
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
            FName fName = new FName(value);
            if(getSchemaNode().getContext().getCurModule().isSelfPrefix(fName.getPrefix())){
                value = fName.getLocalName();
            }
            else {
                Optional<ModuleId> moduleIdOptional = getSchemaNode().getContext().getCurModule()
                        .findModuleByPrefix(fName.getPrefix());
                if(!moduleIdOptional.isPresent()){
                    return ;
                }
                Optional<Module> moduleOptional = getSchemaNode().getContext().getSchemaContext()
                        .getModule(moduleIdOptional.get());
                URI uri = moduleOptional.get().getMainModule().getNamespace().getUri();
                element.addNamespace(fName.getPrefix(), uri.toString());
            }
        }
        element.setText(value);
    }


}

		