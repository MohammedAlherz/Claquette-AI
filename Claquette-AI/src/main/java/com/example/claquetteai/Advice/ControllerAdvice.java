package com.example.claquetteai.Advice;

import com.example.claquetteai.Api.ApiException;

import com.example.claquetteai.Api.ApiResponse;
import jakarta.validation.UnexpectedTypeException;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.core.io.JsonEOFException;
import jakarta.validation.UnexpectedTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;

@org.springframework.web.bind.annotation.ControllerAdvice

public class ControllerAdvice {

    @ExceptionHandler(value =  ApiException.class )
    public ResponseEntity<ApiResponse> ApiException(ApiException ex){
        return ResponseEntity.status(400).body(new ApiResponse(ex.getMessage()));
    }

    // Server Validation Exception
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getFieldError().getDefaultMessage();
        return ResponseEntity.status(400).body(new ApiResponse(msg));
    }

    // Server Validation Exception
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> ConstraintViolationException(ConstraintViolationException e) {
        String msg =e.getMessage();
        return ResponseEntity.status(400).body(new ApiResponse(msg));
    }


    // SQL Constraint Ex:(Duplicate) Exception
    @ExceptionHandler(value = SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ApiResponse> SQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e){
        String msg=e.getMessage();
        return ResponseEntity.status(400).body(new ApiResponse(msg));
    }

    // wrong write SQL in @column Exception
    @ExceptionHandler(value = InvalidDataAccessResourceUsageException.class )
    public ResponseEntity<ApiResponse> InvalidDataAccessResourceUsageException(InvalidDataAccessResourceUsageException e){
        String msg=e.getMessage();
        return ResponseEntity.status(400).body(new ApiResponse(msg));
    }

    // Database Constraint Exception
    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> DataIntegrityViolationException(DataIntegrityViolationException e){
        String msg=e.getMessage();
        return ResponseEntity.status(400).body(new ApiResponse(msg));
    }

    // Method not allowed Exception
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String msg = e.getMessage();
        return ResponseEntity.status(400).body(new ApiResponse(msg));
    }

    //     Json parse Exception
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String msg = e.getMessage();
        return ResponseEntity.status(400).body(new ApiResponse(msg));
    }

    // TypesMisMatch Exception
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> MethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String msg = e.getMessage();
        return ResponseEntity.status(400).body(new ApiResponse(msg));
    }

    @ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse> HttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e){
        return ResponseEntity.status(400).body(new ApiResponse(e.getMessage()));
    }

    @ExceptionHandler(value = JsonEOFException.class)
    public ResponseEntity<ApiResponse> JsonEOFException(JsonEOFException e){
        return ResponseEntity.status(400).body(new ApiResponse(e.getMessage()));
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public ResponseEntity<ApiResponse> HttpClientErrorException(HttpClientErrorException HttpClientErrorException){
        return ResponseEntity.status(400).body(new ApiResponse(HttpClientErrorException.getMessage()));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> IllegalArgumentException (IllegalArgumentException IllegalArgumentException){
        return ResponseEntity.status(400).body(new ApiResponse(IllegalArgumentException.getMessage()));
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<ApiResponse>NoResourceFoundException (NoResourceFoundException NoResourceFoundException){
        return ResponseEntity.status(400).body(new ApiResponse(NoResourceFoundException.getMessage()));
    }

    @ExceptionHandler(value = TransactionSystemException.class)
    public ResponseEntity<ApiResponse> TransactionSystemException (TransactionSystemException transactionSystemException){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(transactionSystemException.getMessage()));
    }

    @ExceptionHandler(value = UnexpectedTypeException.class)
    public ResponseEntity<ApiResponse> UnexpectedTypeException (UnexpectedTypeException unexpectedTypeException){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(unexpectedTypeException.getMessage()));
    }

    @ExceptionHandler(value = JpaSystemException.class)
    public ResponseEntity<ApiResponse> JpaSystemException(JpaSystemException jpaSystemException){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(jpaSystemException.getMessage()));
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<ApiResponse> NullPointerException (NullPointerException nullPointerException){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(nullPointerException.getMessage()));
    }
}
