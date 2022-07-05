package org.yangcentral.yangkit.parser;

import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.common.api.exception.Severity;

public class YangParserException extends Exception {
   private static final long serialVersionUID = 1L;
   private Severity severity;
   private String description;
   private Position position;

   public YangParserException(Severity severity, Position position, String description) {
      this.severity = severity;
      this.position = position;
      this.description = description;
   }

   public Severity getSeverity() {
      return this.severity;
   }

   public String getDescription() {
      return this.description;
   }

   public Position getPosition() {
      return this.position;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("@" + this.position.toString());
      switch (this.severity) {
         case ERROR:
            sb.append("Error:");
            break;
         case WARNING:
            sb.append("Warning:");
            break;
         case INFO:
            sb.append("Info:");
            break;
         case DEBUG:
            sb.append("Debug:");
            break;
         default:
            return null;
      }

      if (null != this.description) {
         sb.append(" ");
         sb.append(this.description);
      }

      return sb.toString();
   }
}
