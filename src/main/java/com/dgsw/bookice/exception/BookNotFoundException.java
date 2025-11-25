package com.dgsw.bookice.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("도서를 찾을 수 없습니다. ID: " + id);
    }

    public BookNotFoundException(String message) {
        super(message);
    }
}
