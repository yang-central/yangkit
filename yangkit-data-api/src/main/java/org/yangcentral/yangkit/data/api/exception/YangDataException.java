package org.yangcentral.yangkit.data.api.exception;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.data.api.model.YangData;

public class YangDataException extends Exception implements ValidatorRecord<AbsolutePath, YangData> {
   private static final long serialVersionUID = -4815971601385477372L;
   private ErrorTag errorTag;
   private String errorAppTag;
   private AbsolutePath errorPath;
   private YangData badElement;
   private ErrorMessage errorMessage;

   public YangDataException(ErrorTag errorTag, String errorAppTag, AbsolutePath errorPath, YangData badElement, ErrorMessage errorMessage) {
      this.errorTag = errorTag;
      this.errorAppTag = errorAppTag;
      this.errorPath = errorPath;
      this.badElement = badElement;
      this.errorMessage = errorMessage;
   }

   public YangDataException(ErrorTag errorTag, AbsolutePath errorPath, ErrorMessage errorMessage) {
      this.errorTag = errorTag;
      this.errorPath = errorPath;
      this.errorMessage = errorMessage;
   }

   public Severity getSeverity() {
      return null;
   }

   public ErrorTag getErrorTag() {
      return this.errorTag;
   }

   public String getErrorAppTag() {
      return this.errorAppTag;
   }

   public AbsolutePath getErrorPath() {
      return this.errorPath;
   }

   public YangData getBadElement() {
      return this.badElement;
   }

   public ErrorMessage getErrorMsg() {
      return this.errorMessage;
   }
}
