package br.com.srm.creditengine.common.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class ReceivableNotFoundException extends BusinessException {

    public ReceivableNotFoundException(UUID id) {
        super("RECEIVABLE_NOT_FOUND", "Receivable not found: " + id, HttpStatus.NOT_FOUND);
    }
}
