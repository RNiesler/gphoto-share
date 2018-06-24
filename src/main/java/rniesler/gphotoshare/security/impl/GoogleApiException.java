package rniesler.gphotoshare.security.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class GoogleApiException extends RuntimeException {
    private HttpStatus status;
}
