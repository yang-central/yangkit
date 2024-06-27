package org.yangcentral.yangkit.data.codec.json;

import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.codec.IdentityRefStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.impl.codec.ComplexStringValueCodecImpl;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public class IdentityRefJsonCodec extends ComplexStringValueCodecImpl<QName> implements IdentityRefStringValueCodec {
    public IdentityRefJsonCodec(TypedDataNode schemaNode) {
        super(schemaNode);
    }


    @Override
    public QName deserialize(Restriction<QName> restriction, String input) throws YangCodecException {
        FName fName = new FName(input);
        String moduleName = fName.getPrefix();
        String localName = fName.getLocalName();
        if(moduleName == null) {
            moduleName = getSchemaNode().getContext().getCurModule().getMainModule().getArgStr();
        }
        Optional<Module> moduleOptional = getSchemaNode().getContext().getSchemaContext()
                .getLatestModule(moduleName);
        if(!moduleOptional.isPresent()) {
            throw new YangCodecException("the module name:" + moduleName + " is not found.");
        }
        URI ns = moduleOptional.get().getMainModule().getNamespace().getUri();
        QName qName = new QName(ns,localName);
        if(!restriction.evaluate(qName)){
            throw new YangCodecException("invalid value:" + input);
        }
        return qName;
    }

    @Override
    public String serialize(Restriction<QName> restriction, QName output) throws YangCodecException {
        if(!restriction.evaluate(output)) {
            throw new YangCodecException("invalid value:" + output);
        }
        URI ns = output.getNamespace();
        List<Module> modules = getSchemaNode().getContext().getSchemaContext().getModule(ns);
        if(modules.isEmpty()) {
            throw new YangCodecException(" can not find the corresponding module for namespace:"+ns);
        }

        return modules.get(0).getMainModule().getArgStr() + ":" + output.getLocalName();
    }
}
