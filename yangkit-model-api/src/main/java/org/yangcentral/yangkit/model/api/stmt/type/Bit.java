package org.yangcentral.yangkit.model.api.stmt.type;

import org.yangcentral.yangkit.model.api.stmt.Entity;
import org.yangcentral.yangkit.model.api.stmt.Identifiable;
import org.yangcentral.yangkit.model.api.stmt.IfFeatureSupport;

public interface Bit extends Entity, Identifiable, IfFeatureSupport {
   Position getPosition();
}
