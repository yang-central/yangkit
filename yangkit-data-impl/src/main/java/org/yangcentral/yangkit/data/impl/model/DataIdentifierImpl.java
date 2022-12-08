package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.DataIdentifier;

import java.util.Objects;

public abstract class DataIdentifierImpl implements DataIdentifier {
    private QName qName;
    public DataIdentifierImpl(QName qName){
        this.qName = qName;
    }

    public QName getQName(){
        return qName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataIdentifierImpl)) {
            return false;
        }
        DataIdentifierImpl that = (DataIdentifierImpl) o;
        return qName.equals(that.qName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qName);
    }
}
