package org.yangcentral.yangkit.data.api.model;

import java.util.List;

public interface ListIdentifier extends DataIdentifier {
   List<LeafData<?>> getKeys();
}
