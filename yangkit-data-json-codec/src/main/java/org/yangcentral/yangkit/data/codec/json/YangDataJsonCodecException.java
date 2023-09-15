package org.yangcentral.yangkit.data.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;

import java.util.Objects;

public class YangDataJsonCodecException extends Exception implements ValidatorRecord<String, JsonNode> {
    private String errorPath;
    private JsonNode badElement;
    private ErrorTag errorTag;
    private ErrorMessage errorMessage;
    private String errorAppTag;

    public YangDataJsonCodecException(String errorPath, JsonNode badElement, ErrorTag errorTag, String errorAppTag,
                                      ErrorMessage errorMessage) {
        this.errorPath = errorPath;
        this.badElement = badElement;
        this.errorTag = errorTag;
        this.errorAppTag = errorAppTag;
        this.errorMessage = errorMessage;
    }
    public YangDataJsonCodecException(String errorPath, JsonNode badElement, ErrorTag errorTag, String errorMessage) {
        this.errorPath = errorPath;
        this.badElement = badElement;
        this.errorTag = errorTag;
        this.errorMessage = new ErrorMessage(errorMessage);
    }
    public YangDataJsonCodecException(String errorPath, JsonNode badElement, ErrorTag errorTag) {
        this.errorPath = errorPath;
        this.badElement = badElement;
        this.errorTag = errorTag;
    }

    public String getErrorPath() {
        return errorPath;
    }

    public JsonNode getBadElement() {
        return badElement;
    }

    @Override
    public ErrorMessage getErrorMsg() {
        return errorMessage;
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    public ErrorTag getErrorTag() {
        return errorTag;
    }

    @Override
    public String getErrorAppTag() {
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YangDataJsonCodecException)) return false;
        YangDataJsonCodecException that = (YangDataJsonCodecException) o;
        return getErrorPath().equals(that.getErrorPath()) &&
                getBadElement().equals(that.getBadElement()) &&
                getErrorTag() == that.getErrorTag() &&
                Objects.equals(getErrorMsg(), that.getErrorMsg());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getErrorPath(), getBadElement(), getErrorTag(), getErrorMsg());
    }

    @Override
    public String toString() {
        return "YangDataJsonCodecException{" +
                "errorPath=" + errorPath +
                ", badElement=" + badElement +
                ", errorTag=" + errorTag +
                ", errorMessage=" + errorMessage.getMessage() +
                '}';
    }

    @Override
    public int compareTo(ValidatorRecord o) {
        if(this.getErrorPath().hashCode() != o.getErrorPath().hashCode()){
            return this.getErrorPath().hashCode() - o.getErrorPath().hashCode();
        }
        return this.hashCode() - o.hashCode();
    }
}