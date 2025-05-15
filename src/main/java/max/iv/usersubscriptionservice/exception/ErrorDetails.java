package max.iv.usersubscriptionservice.exception;

import java.time.LocalDateTime;

public record ErrorDetails(

        LocalDateTime timestamp,
        String message,
        String path,
        int status
) {}