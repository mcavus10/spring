package com.example.moodmovies.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for structured error responses, aligned with Python API format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {
    private String detail; // message yerine
    private String errorCode; // errorCode aynı
    private String requestId; // details yerine (Python API'sinde request_id var)
}
