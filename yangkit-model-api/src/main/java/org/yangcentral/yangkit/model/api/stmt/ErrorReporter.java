package org.yangcentral.yangkit.model.api.stmt;

public interface ErrorReporter {
   ErrorAppTagStmt getErrorAppTag();

   ErrorMessageStmt getErrorMessage();
}
