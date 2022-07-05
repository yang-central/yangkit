package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.common.api.QName;

public interface DataIdentifier extends Comparable<DataIdentifier> {
   QName getQName();

   default int compareTo(DataIdentifier o) {
      return this.getQName().compareTo(o.getQName());
   }
}
