package org.yangcentral.yangkit.model.api.stmt.ext;

import org.yangcentral.yangkit.common.api.validate.ValidatorResult;

import java.util.List;

public interface AugmentStructureSupport {
    List<AugmentStructure> getAugmentStructures();
    ValidatorResult addAugmentStructure(AugmentStructure augmentStructure);
}
