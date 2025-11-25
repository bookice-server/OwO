package com.dgsw.bookice.repository;

import com.dgsw.bookice.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    /**
     * Query Method: 제목으로 검색 (부분 일치)
     */
    List<Book> findByTitleContaining(String title);

    /**
     * Query Method: 저자로 검색 (부분 일치)
     */
    List<Book> findByAuthorContaining(String author);

    /**
     * Query Method: 카테고리로 검색 (정확히 일치)
     */
    List<Book> findByCategory(String category);

    /**
     * Query Method: ISBN으로 검색
     */
    boolean existsByIsbn(String isbn);

    /**
     * JPQL: 가격 범위로 검색
     */
    @Query("SELECT b FROM Book b WHERE b.price BETWEEN :minPrice AND :maxPrice")
    List<Book> findByPriceRange(@Param("minPrice") Integer minPrice,
                                @Param("maxPrice") Integer maxPrice);

    /**
     * JPQL: 재고가 있는 도서만 조회
     */
    @Query("SELECT b FROM Book b WHERE b.stockQuantity > 0")
    List<Book> findBooksInStock();

    /**
     * JPQL: 제목 또는 저자로 검색 (페이징)
     */
    @Query("SELECT b FROM Book b WHERE " +
            "(:keyword IS NULL OR b.title LIKE %:keyword% OR b.author LIKE %:keyword%)")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);

    /**
     * JPQL: 카테고리별 도서 수 조회
     */
    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category")
    List<Object[]> countBooksByCategory();
}
