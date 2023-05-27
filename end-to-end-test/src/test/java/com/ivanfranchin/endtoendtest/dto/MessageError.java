package com.ivanfranchin.endtoendtest.dto;

import java.util.List;

public record MessageError(String timestamp, int status, String error, String message, String path,
                           List<ErrorDetail> errors) {
    record ErrorDetail(List<String> codes, String defaultMessage, String objectName, String field,
                       String rejectedValue, boolean bindingFailure, String code) {
    }
}