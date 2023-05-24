package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.NamespaceContextDom4j;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataCompareResult;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.operation.DataChangeNotifier;
import org.yangcentral.yangkit.data.api.operation.YangDataDocumentOperator;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

import java.util.List;

public class YangDataDocumentImpl extends YangAbstractDataContainer implements YangDataDocument {
    private QName qName;
    private YangSchemaContext schemaContext;
    public YangDataDocumentImpl(QName qName,YangSchemaContext yangDataContainer) {
        super(yangDataContainer);
        this.qName = qName;
        this.schemaContext = yangDataContainer;
        setSelf(this);
    }

    @Override
    public QName getQName() {
        return qName;
    }

    @Override
    public YangSchemaContext getSchemaContext() {
        return schemaContext;
    }



    @Override
    public ValidatorResult validate() {

        return null;
    }



    @Override
    public void addAttribute(Attribute var1) {

    }

    @Override
    public Attribute getAttribute(QName var1) {
        return null;
    }

    @Override
    public List<Attribute> getAttributes() {
        return null;
    }

    @Override
    public void setAttributes(List<Attribute> var1) {

    }

    @Override
    public List<YangDataCompareResult> compare(YangDataDocument var1) {
        return null;
    }


    @Override
    public ValidatorResult processWhen() {
        return null;
    }

    @Override
    public YangDataDocument clone() throws CloneNotSupportedException {
        return null;
    }

    @Override
    public void update() {

    }
}
