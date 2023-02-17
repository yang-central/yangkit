package org.yangcentral.yangkit.model.api.stmt;

import org.yangcentral.yangkit.common.api.exception.Severity;

public class ModelException extends Exception {
   private static final long serialVersionUID = 1L;
   private Severity severity;
   private YangStatement element;
   private String description;

   public ModelException(Severity severity, YangStatement element, String description) {
      this.severity = severity;
      this.element = element;
      this.setDescription(description);
   }

   public YangStatement getElement() {
      return this.element;
   }

   public Severity getSeverity() {
      return this.severity;
   }

   public void setSeverity(Severity severity) {
      this.severity = severity;
   }

   public String getDescription() {
      String traceInfo = null;
      return traceInfo == null ? this.description : this.description + traceInfo;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String toString() {
      return "";
   }
}
