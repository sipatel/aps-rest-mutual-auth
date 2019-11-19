/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * From http://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
 * 
 * @author Joram Barrez
 */
@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)  // 404
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ErrorInfo handleNotFound(Exception e) {
    	return new ErrorInfo(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ErrorInfo handleBadRequest(Exception e) {
    	return new ErrorInfo(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseBody
    public ErrorInfo handleInternalServerError(Exception e) {
    	return new ErrorInfo(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.UNAUTHORIZED)  // 401
    @ExceptionHandler(NotPermittedException.class)
    @ResponseBody
    public ErrorInfo handleNotPermittedException(Exception e) {
    	return new ErrorInfo(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.CONFLICT)  // 409
    @ExceptionHandler(ConflictException.class)
    @ResponseBody
    public ErrorInfo handleConflict(Exception e) {
    	return new ErrorInfo(e.getMessage());
    }
    
}
