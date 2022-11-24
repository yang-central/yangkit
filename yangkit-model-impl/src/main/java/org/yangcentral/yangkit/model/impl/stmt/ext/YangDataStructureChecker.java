package org.yangcentral.yangkit.model.impl.stmt.ext;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;
import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;
import org.yangcentral.yangkit.base.YangStatementChecker;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-11-22
 */
public class YangDataStructureChecker implements YangStatementChecker {
    @Override
    public ValidatorResult buildChecker(YangStatement parent, YangStatement child,BuildPhase buildPhase) {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        if(buildPhase != BuildPhase.SCHEMA_BUILD){
            return validatorResultBuilder.build();
        }
        if(!(child instanceof YangDataStructure)){
            validatorResultBuilder.addRecord(ModelUtil.reportError(parent,
                    ErrorCode.COMMON_ERROR.getFieldName()));
            return validatorResultBuilder.build();
        }
        if(!(parent instanceof Module)){
            validatorResultBuilder.addRecord(ModelUtil.reportError(parent,
                    ErrorCode.INVALID_SUBSTATEMENT.getFieldName()+ " sx:structure"));
            return validatorResultBuilder.build();
        }
        Module module = (Module) parent;
        YangDataStructure structure = (YangDataStructure) child;
        if(module.getContext().getSchemaNodeIdentifierCache().containsKey(structure.getArgStr())){
            validatorResultBuilder.addRecord(ModelUtil.reportDuplicateError(module.getContext().getSchemaNodeIdentifierCache()
                    .get(structure.getArgStr()),structure));
        } else {
            module.getContext().getSchemaNodeIdentifierCache().put(structure.getArgStr(), structure);
            module.addSchemaNodeChild(structure);
        }
        return validatorResultBuilder.build();
    }
}

