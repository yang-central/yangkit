package org.yangcentral.yangkit.model.api.stmt;

public interface Deviationable {
   boolean processDeviation(Deviation deviation) throws ModelException;
}
