package org.yangcentral.yangkit.model.api.stmt.ext;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;

import java.util.List;

public interface StructureSupport {
    List<YangStructure> getStructures();
    YangStructure getStructure(String name);
    ValidatorResult addStructure(YangStructure structure);

}
