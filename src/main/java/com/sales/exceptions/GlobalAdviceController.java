package com.sales.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.sales.request.ErrorDto;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartException;

import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalAdviceController {

    
    private static final Logger logger = LoggerFactory.getLogger(GlobalAdviceController.class);


    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ErrorDto handleAccessDenied(AuthorizationDeniedException ex) {
        logger.error("AuthorizationDeniedException: {}", ex.getMessage(),ex);
        return new ErrorDto(ex.getMessage(), 403);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ErrorDto handleAccessDenied(AccessDeniedException ex) {
        logger.error("AccessDeniedException: {}", ex.getMessage(),ex);
        return new ErrorDto(ex.getMessage(), 403);
    }

    @Transactional
    @ExceptionHandler(value = PermissionDeniedDataAccessException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ErrorDto permissionDeniedDataAccessException (PermissionDeniedDataAccessException ex , WebRequest request){
        logger.error("PermissionDeniedDataAccessException: {}", ex.getMessage(),ex);
        return new ErrorDto(ex.getMessage(), 403);
    }

    @Transactional
    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto notFoundException (NotFoundException ex , WebRequest request){
        logger.error("NotFoundException: {}", ex.getMessage(),ex);
        return new ErrorDto(ex.getMessage(), 400);
    }

    @Transactional
    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public ErrorDto illegalArgumentException (IllegalArgumentException ex , WebRequest request){
        logger.error("IllegalArgumentException: {}", ex.getMessage(),ex);
        return new ErrorDto(ex.getMessage(), 406);
    }

    @Transactional
    @ExceptionHandler(value = JsonMappingException.class)
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public ErrorDto jsonMappingExceptionHandle (JsonMappingException ex , WebRequest request){
        logger.error("JsonMappingException: {}", ex.getMessage(),ex);
        return new ErrorDto(ex.getMessage(), 406);
    }

    @Transactional
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        logger.error("MethodArgumentNotValidException: {}", ex.getMessage(), ex);
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(fe -> {
            sb.append(fe.getField()).append(" ").append(fe.getDefaultMessage()).append("; ");
        });
        String msg = sb.length() == 0 ? "Validation failed" : sb.toString().trim();
        return new ErrorDto(msg, 400);
    }

    @Transactional
    @ExceptionHandler(value = {MultipartException.class})
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public ErrorDto noMultipartException (MultipartException ex , WebRequest request){
        logger.error("MultipartException: {}", ex.getMessage(),ex);
        return new ErrorDto("This is not a multipart request", 400);
    }

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorDto fileNotFound(FileNotFoundException e,WebRequest request) {
        logger.error("FileNotFoundException: {}", e.getMessage());
        ErrorDto errorDto = new ErrorDto(e.getMessage(),400);
        return errorDto;
    }

    @Transactional
    @ExceptionHandler(value = {ObjectNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorDto resourceNotFoundException(ObjectNotFoundException ex, WebRequest request) {
        logger.error("ObjectNotFoundException: {}", ex.getMessage(),ex);
        String errorMessage = ex.getMessage();
        errorMessage = errorMessage.contains(";") ? errorMessage.substring(0, errorMessage.indexOf(";")) : errorMessage;
        ErrorDto message = new ErrorDto(errorMessage,404);
        return message;
    }

    @Transactional
    @ExceptionHandler(value = {SQLIntegrityConstraintViolationException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto resourceNotFoundException(SQLIntegrityConstraintViolationException ex, WebRequest request) {
        logger.error("SQLIntegrityConstraintViolationException: {}", ex.getMessage(),ex);
        String errorMessage = ex.getMessage();
        errorMessage = errorMessage.contains(";") ? errorMessage.substring(0, errorMessage.indexOf(";")) : errorMessage;
        ErrorDto message = new ErrorDto(errorMessage,500);
        return message;
    }

    @Transactional
    @ExceptionHandler(value = {NullPointerException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto resourceNotFoundException(NullPointerException ex, WebRequest request) {
        logger.error("NullPointerException: {}", ex.getMessage(),ex);
        ErrorDto message = new ErrorDto("Something went wrong there is a null pointer exception.",400);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return message;
    }

    @Transactional
    @ExceptionHandler(value = {DataIntegrityViolationException.class})
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorDto dataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        logger.error("DataIntegrityViolationException: {}", ex.getMessage(),ex);
        String errorMessage = getCauseMessage(ex);
        errorMessage = errorMessage.contains(";") ? errorMessage.substring(0, errorMessage.indexOf(";")) : errorMessage;
        ErrorDto err = new ErrorDto(errorMessage,409);
        return err;
    }

    @Transactional
    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorDto sqlExceptionHelper(ConstraintViolationException ex, WebRequest request) {
        logger.error("ConstraintViolationException : {}", ex.getMessage(),ex);
        String errorMessage = extractDuplicateEntryMessage(ex.getMessage());
        errorMessage = errorMessage.contains(";") ? errorMessage.substring(0, errorMessage.indexOf(";")) : errorMessage;
        return new ErrorDto(errorMessage,409);
    }

    @Transactional
    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public ErrorDto httpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        logger.error("HttpMessageNotReadableException: {}", ex.getMessage(),ex);
        ErrorDto message = new ErrorDto(ex.getMessage(),406);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return message;
    }

    @Transactional
    @ExceptionHandler(value = {MyException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto myException(MyException ex, WebRequest request) {
        logger.error("MyException: {}", ex.getMessage(),ex);
        String errorMessage = ex.getMessage();
        errorMessage = errorMessage.contains(";") ? errorMessage.substring(0, errorMessage.indexOf(";")) : errorMessage;
        ErrorDto message = new ErrorDto(errorMessage,500);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return message;
    }

    @Transactional
    @ExceptionHandler(value = {UserException.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorDto userException(UserException ex, WebRequest request) {
        logger.error("UserException: {}", ex.getMessage(),ex);
        String errorMessage = ex.getMessage();
        errorMessage = errorMessage.contains(";") ? errorMessage.substring(0, errorMessage.indexOf(";")) : errorMessage;
        ErrorDto message = new ErrorDto(errorMessage,500);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return message;
    }

    @Transactional
    @ExceptionHandler(value = {UnexpectedRollbackException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto unexpectedRollbackException(UnexpectedRollbackException ex, WebRequest request) {
        logger.error("UnexpectedRollbackException: {}", ex.getMessage(),ex);
        String errorMessage = ex.getLocalizedMessage();
        errorMessage = errorMessage.contains(";") ? errorMessage.substring(0, errorMessage.indexOf(";")) : errorMessage;
        ErrorDto message = new ErrorDto(errorMessage,500);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return message;
    }

    @Transactional
    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto resourceNotFoundException(Exception ex, WebRequest request) {
        logger.error("Exception: {}", ex.getMessage(),ex);
        ErrorDto message = null;
        try{
            String errorMessage = getCauseMessage(ex);
            errorMessage = errorMessage.contains(";") ? errorMessage.substring(0, errorMessage.indexOf(";")) : errorMessage;
            message =new ErrorDto(errorMessage,500);
        }catch (Exception e) {
            String errorMessage = ex.getMessage();
            errorMessage = errorMessage.contains(";") ? errorMessage.substring(0,errorMessage.indexOf(";")) : errorMessage;
            message = new ErrorDto(errorMessage, 500);
        }
        return message;
    }

    private String getCauseMessage(Throwable t){
        return t.getCause().getCause().getLocalizedMessage();
    }

    private String extractDuplicateEntryMessage(String errorMessage) {
        // The regex looks for "Duplicate entry '...' for a key '...'"
        // It captures the entire string matching this pattern.
        Pattern pattern = Pattern.compile("Duplicate entry '[^']+' for key '[^']+?'");
        Matcher matcher = pattern.matcher(errorMessage);

        if (matcher.find()) {
            return matcher.group(0); // group(0) returns the entire matched sequence
        } else {
            return  errorMessage;
        }
    }
}
