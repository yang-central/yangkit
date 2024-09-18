package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.data.api.model.DifferenceType;
import org.yangcentral.yangkit.data.api.model.YangDataCompareResult;
import org.yangcentral.yangkit.data.api.model.YangData;

public class YangCompareResultImpl implements YangDataCompareResult {
    private AbsolutePath path;

    private DifferenceType differenceType;

    private YangData<?> changed;

    private YangData<?> previous;

    public YangCompareResultImpl(AbsolutePath path, DifferenceType differenceType, YangData<?> changed,YangData<?> previous) {
        this.path = path;
        this.differenceType = differenceType;
        this.changed = changed;
        this.previous = previous;
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

    @Override
    public YangData<?> getPrevious() {
        return previous;
    }

}
