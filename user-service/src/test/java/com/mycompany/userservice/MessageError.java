package com.mycompany.userservice;

import lombok.Data;

import java.util.List;

@Data
public class MessageError {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<ErrorDetail> errors;

    @Data
    static class ErrorDetail {
        private List<String> codes;
        private String defaultMessage;
        private String objectName;
        private String field;
        private String rejectedValue;
        private boolean bindingFailure;
        private String code;
    }

}
