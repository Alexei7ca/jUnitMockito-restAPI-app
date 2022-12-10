package com.example.demo.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


//@ControllerAdvice is a specialization of the @Component annotation which allows to handle exceptions  (контроллер для ошибок)
// across the whole application in one global handling component.
// It can be viewed as an interceptor of exceptions thrown by methods annotated with @RequestMapping and similar.
@RestControllerAdvice
// this is a combination of @ControllerAdvice + @ResponseBody  // without this we would need to add the @ExceptionHandler to all our Controller classes
public class BookRecordExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(BookRecordExceptionHandler.class);


    @ExceptionHandler(BookRecordNotFoundException.class)
    //Annotation for handling exceptions in specific handler classes and/or handler methods
    @ResponseStatus(HttpStatus.NOT_FOUND)
    // the status could be OK(200) although the data corresponds to exception signal (404 – Not Found for example). @ResponseStatus help set the HTTP status code for the response
    public String throwResourceNotFoundException(BookRecordNotFoundException resourceNotFoundException) {
        logger.error("BookRecordNotFoundException Thrown ", resourceNotFoundException);
        return resourceNotFoundException.getMessage();

    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // did not pass validation (age 18+ for example or name is null)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String throwMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException) {
        logger.error("MethodArgumentNotValidException Thrown ", methodArgumentNotValidException);
        return methodArgumentNotValidException.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)  //this is the ones for the ids that do not match
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String throwIllegalArgumentException(IllegalArgumentException illegalArgumentException) {
        logger.error("IllegalArgumentException Thrown ", illegalArgumentException);
        return illegalArgumentException.getMessage();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)  //when an int is required (like id) but user writes somth else
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String throwMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
        logger.error("IllegalArgumentException Thrown ", methodArgumentTypeMismatchException);
        return methodArgumentTypeMismatchException.getMessage();
    }

    //this is the handler for all other errors
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public String catchAllOtherExceptions(Exception otherException) {
        logger.error("Internal Server Error ", otherException);
//        return otherException.getMessage();
        //    private final String GLOBAL_EXCEPTION_MESSAGE = "Internal Server Error";
        String GLOBAL_EXCEPTION_MESSAGE = "Something went wrong.";
        return GLOBAL_EXCEPTION_MESSAGE;
    }

}
//@ResponseStatus
//Marks a method or exception class with the status code and reason that should be returned.
//The status code is applied to the HTTP response when the handler method is invoked and overrides status information set by other means