package com.acme.salarymgmt.exception;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response body returned by all error handlers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    /**
     * Field-level validation errors (field name → error message).
     * Only populated for validation failures.
     */
    private Map<String, String> validationErrors;
}
