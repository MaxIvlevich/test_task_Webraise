package max.iv.usersubscriptionservice.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorDetailsValidation(

        LocalDateTime timestamp,
        String message,
        String path,
        int status,
        Map<String, String> errors // Поле для деталей ошибок валидации
) {}
