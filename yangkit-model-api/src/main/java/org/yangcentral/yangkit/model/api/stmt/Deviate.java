package org.yangcentral.yangkit.model.api.stmt;

import java.util.List;

public interface Deviate extends YangBuiltinStatement, MustSupport {
   DeviateType getDeviateType();

   SchemaNode getTarget();

   void setTarget(SchemaNode var1);

   Config getConfig();

   List<Default> getDefaults();

   Mandatory getMandatory();

   MaxElements getMaxElements();

   MinElements getMinElements();

   Type getType();

   Units getUnits();

   List<Unique> getUniques();
}
