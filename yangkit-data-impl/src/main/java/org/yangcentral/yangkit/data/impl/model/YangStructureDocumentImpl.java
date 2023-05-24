package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.YangStructureDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;

public class YangStructureDocumentImpl extends YangDataDocumentImpl implements YangStructureDocument {
    YangDataStructure structure;
    public YangStructureDocumentImpl(YangDataStructure structure, YangSchemaContext yangDataContainer) {
        super(structure.getIdentifier(), yangDataContainer);
        this.structure = structure;
    }

    @Override
    public YangDataStructure getStructure() {
        return structure;
    }
}
