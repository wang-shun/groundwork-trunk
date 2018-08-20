package org.groundwork.cloudhub.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoApiSaveResultResponse {

    private String error;
    private Boolean success = true;
    private String result;
    private ConnectionConfiguration configuration;

    public DtoApiSaveResultResponse(ConnectionConfiguration configuration) {
        this.success = true;
        this.configuration = configuration;
    }

    public DtoApiSaveResultResponse(String error) {
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

    public DtoApiSaveResultResponse setError(String error) {
        this.error = error;
        return this;
    }

    public Boolean getSuccess() {
        return success;
    }

    public DtoApiSaveResultResponse setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public String getResult() {
        return result;
    }

    public DtoApiSaveResultResponse setResult(String result) {
        this.result = result;
        return this;
    }

    public ConnectionConfiguration getConfiguration() {
        return configuration;
    }

}
