package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.DynamicMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;

import java.util.Objects;

/**
 * Exception for Protocol Buffers codec operations.
 */
public class YangDataProtoCodecException extends Exception implements ValidatorRecord<String, DynamicMessage> {

    private ErrorTag errorTag = ErrorTag.OPERATION_FAILED;
    private String errorPath;
    private DynamicMessage badElement;
    private ErrorMessage errorMessage;

    public YangDataProtoCodecException(String message) {
        super(message);
    }

    public YangDataProtoCodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public YangDataProtoCodecException(ErrorTag errorTag, String message) {
        super(message);
        this.errorTag = errorTag;
        this.errorMessage = new ErrorMessage(message);
    }

    public ErrorTag getErrorTag() {
        return errorTag;
    }

    public void setErrorTag(ErrorTag errorTag) {
        this.errorTag = errorTag;
    }

    public String getErrorPath() {
        return errorPath != null ? errorPath : "";
    }

    public void setErrorPath(String errorPath) {
        this.errorPath = errorPath;
    }

    public DynamicMessage getBadElement() {
        return badElement;
    }

    public void setBadElement(DynamicMessage badElement) {
        this.badElement = badElement;
    }

    @Override
    public ErrorMessage getErrorMsg() {
        return errorMessage != null ? errorMessage : new ErrorMessage(getMessage());
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public String getErrorAppTag() {
        return null;
    }

    @Override
    public int compareTo(ValidatorRecord o) {
        if(this.getErrorPath().hashCode() != o.getErrorPath().hashCode()){
            return this.getErrorPath().hashCode() - o.getErrorPath().hashCode();
        }
        return this.hashCode() - o.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YangDataProtoCodecException)) return false;
        YangDataProtoCodecException that = (YangDataProtoCodecException) o;
        return getErrorPath().equals(that.getErrorPath()) &&
                Objects.equals(getErrorMsg(), that.getErrorMsg());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getErrorPath(), getErrorMsg());
    }
}
