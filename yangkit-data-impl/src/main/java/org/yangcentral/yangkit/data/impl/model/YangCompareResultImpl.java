package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.data.api.model.DifferenceType;
import org.yangcentral.yangkit.data.api.model.YangCompareResult;
import org.yangcentral.yangkit.data.api.model.YangData;

public class YangCompareResultImpl implements YangCompareResult {
    private AbsolutePath path;

    private DifferenceType differenceType;

    private YangData<?> changed;

    public YangCompareResultImpl(AbsolutePath path, DifferenceType differenceType, YangData<?> changed) {
        this.path = path;
        this.differenceType = differenceType;
        this.changed = changed;
    }

    public AbsolutePath getPath() {
        return path;
    }

    public DifferenceType getDifferenceType() {
        return differenceType;
    }

    @Override
    public YangData<?> getChanged() {
        return changed;
    }

}
