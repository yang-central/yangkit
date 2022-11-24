package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.common.api.QName;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-11-22
 */
public class YangSubStatementInfo {
    private QName keyword;
    private Cardinality cardinality;
    private Class<? extends YangStatementChecker> checker;

    public YangSubStatementInfo(QName keyword, Cardinality cardinality) {
        this.keyword = keyword;
        this.cardinality = cardinality;
    }

    public YangSubStatementInfo(QName keyword, Cardinality cardinality, Class<? extends YangStatementChecker> checker) {
        this.keyword = keyword;
        this.cardinality = cardinality;
        this.checker = checker;
    }

    public QName getKeyword() {
        return keyword;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public Class<? extends YangStatementChecker> getChecker() {
        return checker;
    }

    public void setChecker(Class<? extends YangStatementChecker> checker) {
        this.checker = checker;
    }
}
