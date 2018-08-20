package org.groundwork.cloudhub.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoApiResultResponse {

    private String error;
    private Boolean success = true;
    private String result;

    public DtoApiResultResponse() {
        this.success = true;
    }

    public DtoApiResultResponse(String error) {
        if (error.isEmpty()) {
            this.success = true;
        }
        else {
            this.success = false;
            this.error = error;
        }
    }

    public String getError() {
        return error;
    }

    public DtoApiResultResponse setError(String error) {
        this.error = error;
        return this;
    }

    public Boolean getSuccess() {
        return success;
    }

    public DtoApiResultResponse setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public String getResult() {
        return result;
    }

    public DtoApiResultResponse setResult(String result) {
        this.result = result;
        return this;
    }

}
