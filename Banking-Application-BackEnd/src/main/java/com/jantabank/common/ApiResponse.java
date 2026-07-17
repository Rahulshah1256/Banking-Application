package com.jantabank.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard envelope for every REST response so clients get a consistent shape
 * for both success and error cases. Null fields are omitted from the JSON.
 *
 * @param <T> type of the {@code data} payload
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private List<String> errors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String path;
    private String traceId;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static ApiResponse<Object> error(String message, String errorCode, List<String> errors, String path) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errors(errors)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
