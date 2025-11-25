package com.dgsw.bookice.repository;

import com.dgsw.bookice.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepositoryCustom {

    /**
     * 동적 검색 쿼리 (제목, 저자, 카테고리)
     */
    Page<Book> searchByConditions(String title, String author, String category, Pageable pageable);
}
