package org.yangcentral.yangkit.model.impl.stmt.ext;

import org.yangcentral.yangkit.base.Cardinality;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangStatementDef;
import org.yangcentral.yangkit.base.YangSubStatementInfo;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.Extension;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.ext.DynamicDefault;
import org.yangcentral.yangkit.model.impl.stmt.YangStatementImpl;
import org.yangcentral.yangkit.register.YangParentStatementInfo;
import org.yangcentral.yangkit.register.YangUnknownParserPolicy;
import org.yangcentral.yangkit.register.YangUnknownRegister;

import java.util.List;

public class DynamicDefaultImpl extends YangStatementImpl implements DynamicDefault {
    private Extension extension;
    public static void register(){
        YangUnknownParserPolicy unknownParserPolicy = new YangUnknownParserPolicy(YANG_KEYWORD, DynamicDefaultImpl.class);
        YangStatementDef yangStatementDef = new YangStatementDef(YANG_KEYWORD,null,false);
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(DynamicDefault.YANG_KEYWORD,new Cardinality()));
        yangStatementDef.addSubStatementInfo(new YangSubStatementInfo(YangBuiltinKeyword.DESCRIPTION.getQName(), new Cardinality(0,1)));
        unknownParserPolicy.setStatementDef(yangStatementDef);
        unknownParserPolicy.addParentStatementInfo(new YangParentStatementInfo(YangBuiltinKeyword.LEAF.getQName(), new Cardinality(0,1)));
        unknownParserPolicy.addParentStatementInfo(new YangParentStatementInfo(YangBuiltinKeyword.LEAFLIST.getQName(), new Cardinality()));
        YangUnknownRegister.getInstance().register(unknownParserPolicy);
    }
    public DynamicDefaultImpl(String argStr) {
        super(argStr);
    }
    public DynamicDefaultImpl(String keyword,String argStr) {
        super(argStr);
    }

    @Override
    public QName getYangKeyword() {
        return YANG_KEYWORD;
    }

    @Override
    public String getKeyword() {
        List<Module> modules = getContext().getSchemaContext().getModule(getYangKeyword().getNamespace());
        if(modules.isEmpty()){
            return null;
        }
        String moduleName = modules.get(0).getArgStr();
        Module curModule =getContext().getCurModule();
        String prefix =null;
        if(curModule.getArgStr().equals(moduleName)){
            prefix = curModule.getSelfPrefix();
        } else {
            for(Import im:curModule.getImports()){
                if(im.getArgStr().equals(moduleName)){
                    prefix = im.getPrefix().getArgStr();
                    break;
                }
            }
        }
        if(prefix == null){
            return null;
        }
        return prefix+":" + getYangKeyword().getLocalName();
    }

    @Override
    public Extension getExtension() {
        if(extension != null){
            return extension;
        }
        List<Module> modules = getContext().getSchemaContext().getModule(getYangKeyword().getNamespace());
        if(modules.isEmpty()){
            return null;
        }
        return modules.get(0).getExtension(getYangKeyword().getLocalName());
    }

    @Override
    public void setExtension(Extension extension) {
        this.extension = extension;
    }
}
