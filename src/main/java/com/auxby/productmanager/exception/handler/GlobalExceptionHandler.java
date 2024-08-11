package com.auxby.productmanager.exception.handler;

import com.auxby.productmanager.api.v1.bid.model.PlaceBidResponse;
import com.auxby.productmanager.exception.*;
import com.auxby.productmanager.exception.response.ExceptionResponse;
import com.auxby.productmanager.utils.constant.CustomHttpStatus;
import org.apache.coyote.BadRequestException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        Map<String, List<String>> body = new HashMap<>();
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    protected ResponseEntity<ExceptionResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = PhotoUploadException.class)
    protected ResponseEntity<ExceptionResponse> handleUserEmailNotValidated(PhotoUploadException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = ActionNotAllowException.class)
    protected ResponseEntity<ExceptionResponse> handleEditNotAllow(ActionNotAllowException ex) {
        return ResponseEntity.status(CustomHttpStatus.ACTION_NOT_ALLOW.getValue())
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = InsufficientCoinsException.class)
    protected ResponseEntity<ExceptionResponse> handleNotEnoughCoins(InsufficientCoinsException ex) {
        return ResponseEntity.status(CustomHttpStatus.INSUFFICIENT_COINS.getValue())
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = BidDeclinedException.class)
    protected ResponseEntity<PlaceBidResponse> handleEditNotAllow(BidDeclinedException ex) {
        return ResponseEntity.status(CustomHttpStatus.BID_NOT_ACCEPTED.getValue())
                .body(new PlaceBidResponse(false, ex.getOfferBidsInPlace()));
    }

    @ExceptionHandler(value = AuxbyAuthenticationException.class)
    protected ResponseEntity<ExceptionResponse> handleEditNotAllow(AuxbyAuthenticationException ex) {
        return ResponseEntity.status(CustomHttpStatus.BID_NOT_ACCEPTED.getValue())
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = BadRequestException.class)
    protected ResponseEntity<ExceptionResponse> handleEditNotAllow(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = DeepLinkGenerationException.class)
    protected ResponseEntity<ExceptionResponse> handleEditNotAllow(DeepLinkGenerationException ex) {
        return ResponseEntity.status(CustomHttpStatus.FAIL_TO_GENERATE_LINK.getValue())
                .body(new ExceptionResponse(ex.getMessage()));
    }
}
