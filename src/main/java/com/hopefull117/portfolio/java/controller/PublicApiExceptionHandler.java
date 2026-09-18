package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.exception.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {PublicArticleController.class, PublicProjectController.class})
public class PublicApiExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound() {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Resource not found");
        problem.setDetail("The requested public resource was not found.");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedFailure() {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Public API failure");
        problem.setDetail("The public request could not be completed.");
        return problem;
    }
}
