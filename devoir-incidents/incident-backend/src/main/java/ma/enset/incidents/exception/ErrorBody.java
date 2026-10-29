package ma.enset.incidents.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorBody(int status, String message, Map<String, String> errors, LocalDateTime timestamp) {
    public static ErrorBody of(int status, String message) {
        return new ErrorBody(status, message, null, LocalDateTime.now());
    }
    public static ErrorBody of(int status, String message, Map<String, String> errors) {
        return new ErrorBody(status, message, errors, LocalDateTime.now());
    }
}
