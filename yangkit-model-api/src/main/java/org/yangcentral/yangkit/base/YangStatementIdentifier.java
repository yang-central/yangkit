package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.model.api.stmt.YangStatement;

/**
 * @author : frank feng
 * @since  : 7/15/2022 2:49 PM
 */
public class YangStatementIdentifier {
    private YangStatement yangStatement;

    public YangStatementIdentifier(YangStatement yangStatement) {
        this.yangStatement = yangStatement;
    }
    public String getIdentifier(){
        StringBuilder sb = new StringBuilder();
        String keyword;
        if(yangStatement.getYangKeyword().getNamespace().equals(Yang.NAMESPACE.getUri())){
            keyword = yangStatement.getYangKeyword().getLocalName();
        } else {
            keyword = yangStatement.getYangKeyword().getQualifiedName();
        }
        sb.append("[" + keyword + "]");
        YangStatement parentStmt = yangStatement.getParentStatement();
        if( parentStmt == null){
            //module or submodule
            sb.append(yangStatement.getArgStr());//add argument
            return sb.toString();
        }

        YangSpecification yangSpecification = yangStatement.getContext().getYangSpecification();
        YangStatementDef parentStatementDef = yangSpecification.getStatementDef(parentStmt.getYangKeyword());
        if(parentStatementDef == null){
            sb.append(yangStatement.getArgStr());//add argument
            return sb.toString();
        }
        Cardinality cardinality = parentStatementDef.getSubStatementCardinality(yangStatement.getYangKeyword());
        if(cardinality == null || cardinality.isUnbounded() || cardinality.getMaxElements() > 1){
            sb.append(yangStatement.getArgStr());//add argument
        }
        return sb.toString();
    }
}
