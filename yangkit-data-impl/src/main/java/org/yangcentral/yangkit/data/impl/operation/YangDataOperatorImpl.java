package org.yangcentral.yangkit.data.impl.operation;

import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.DataIdentifier;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.operation.YangDataOperator;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

public class YangDataOperatorImpl implements YangDataOperator {
    private YangDataContainer operatedData;

    public YangDataOperatorImpl(YangDataContainer operatedData) {
        this.operatedData = operatedData;
    }

    @Override
    public YangDataContainer getOperatedData() {
        return null;
    }

    @Override
    public void create(YangData<?> node, boolean autoDelete) throws YangDataException {

    }

    @Override
    public void merge(YangData<?> node, boolean autoDelete) throws YangDataException {

    }

    @Override
    public void replace(YangData<?> node, boolean autoDelete) throws YangDataException {

    }

    @Override
    public void delete(DataIdentifier identifier) {

    }
}
