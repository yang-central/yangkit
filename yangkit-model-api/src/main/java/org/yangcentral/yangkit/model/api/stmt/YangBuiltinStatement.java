package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.base.Cardinality;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.base.YangSpecification;
import org.yangcentral.yangkit.base.YangStatementDef;
import org.yangcentral.yangkit.base.YangSubStatementInfo;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.register.YangUnknownParserPolicy;
import org.yangcentral.yangkit.register.YangUnknownRegister;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.List;
import java.util.Map;

/**
 * Marker interface for YANG built-in keyword statements.
 * Provides default substatement validation that all builtin
 * statements must perform regardless of their inheritance chain.
 */
public interface YangBuiltinStatement extends YangStatement {

    /**
     * Validate that all child substatements of this statement
     * are legal per the YANG specification (YangStatementDef).
     * Also checks cardinality of each defined substatement.
     *
     * @param baseResult the result from parent initSelf() chain
     * @return merged ValidatorResult including substatement validation
     */
    default ValidatorResult validateSubStatements(ValidatorResult baseResult) {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(baseResult);

        YangSpecification yangSpecification = this.getContext().getYangSpecification();
        YangStatementDef statementDef = yangSpecification.getStatementDef(this.getYangKeyword());
        if (null == statementDef) {
            validatorResultBuilder.addRecord(ModelUtil.reportError((YangStatement) this,
                    ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()
                            + " keyword:" + this.getYangKeyword().getLocalName()));
            return validatorResultBuilder.build();
        }

        Map<QName, YangSubStatementInfo> subStatementInfos = statementDef.getSubStatementInfos();

        // check cardinality
        for (QName key : subStatementInfos.keySet()) {
            List<YangStatement> filteredStatements =
                    ((YangStatement) this).getSubStatement(key);
            Cardinality cardinality = subStatementInfos.get(key).getCardinality();
            if (!cardinality.isValid(filteredStatements.size())) {
                validatorResultBuilder.addRecord(ModelUtil.reportError(
                        (YangStatement) this,
                        ErrorCode.CARDINALITY_BROKEN.getFieldName()
                                + " sub-statement:" + key.getLocalName()
                                + "'s cardinality is:" + cardinality));
            }
        }

        // check invalid substatements (including YangUnknown parent checks)
        for (YangElement subElement : ((YangStatement) this).getSubElements()) {
            if (!(subElement instanceof YangStatement)) {
                continue;
            }
            YangStatement yangStatement = (YangStatement) subElement;
            if (!subStatementInfos.containsKey(yangStatement.getYangKeyword())) {
                if (!(yangStatement instanceof YangUnknown)) {
                    validatorResultBuilder.addRecord(ModelUtil.reportError(
                            yangStatement,
                            ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                } else {
                    YangUnknownParserPolicy unknownParserPolicy =
                            YangUnknownRegister.getInstance()
                                    .getUnknownInfo(((YangUnknown) subElement).getYangKeyword());
                    if (unknownParserPolicy == null
                            || unknownParserPolicy.getParentStatements().size() <= 0) {
                        continue;
                    }
                    if (unknownParserPolicy.getParentStatement(
                            this.getYangKeyword()) == null) {
                        validatorResultBuilder.addRecord(ModelUtil.reportError(
                                (YangStatement) subElement,
                                ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
                    }
                }
            }
        }
        return validatorResultBuilder.build();
    }
}
