package org.yangcentral.yangkit.model.api.stmt.type;

import org.yangcentral.yangkit.model.api.restriction.Section;
import org.yangcentral.yangkit.model.api.stmt.ErrorReporter;
import org.yangcentral.yangkit.model.api.stmt.MetaDef;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;

import java.util.List;

public interface SectionExpression extends YangBuiltinStatement, MetaDef, ErrorReporter {
   boolean isSubSet(SectionExpression var1);

   boolean evaluate(Comparable var1);

   List<Section> getSections();

   void setBound(Comparable var1, Comparable var2);

   Comparable getMax();

   Comparable getMin();
}
