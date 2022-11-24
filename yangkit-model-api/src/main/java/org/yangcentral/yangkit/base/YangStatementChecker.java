package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

/**
 * 功能描述
 *
 * @author frank
 * @since 2022-11-22
 */
public interface YangStatementChecker {

    default ValidatorResult initChecker(YangStatement parent,YangStatement child){
        return new ValidatorResultBuilder().build();
    }
    default ValidatorResult buildChecker(YangStatement parent,YangStatement child, BuildPhase buildPhase){
        return new ValidatorResultBuilder().build();
    }
    default ValidatorResult validateChecker(YangStatement parent,YangStatement child){
        return new ValidatorResultBuilder().build();
    }
    default boolean check(YangStatement parent,YangStatement child){
        return true;
    }

}
