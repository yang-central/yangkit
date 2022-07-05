package org.yangcentral.yangkit.model.api.restriction;

import org.yangcentral.yangkit.model.api.stmt.Type;

import java.util.List;

public interface Union extends Restriction<Object> {
   List<Type> getTypes();
}
