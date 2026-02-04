package real.talk.controller.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import real.talk.service.access.AccessControlService;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
    ;

    @ExceptionHandler(AccessControlService.AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessControlService.AccessDeniedException e) {
        log.warn("Access Denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error on server side");
    }
}
