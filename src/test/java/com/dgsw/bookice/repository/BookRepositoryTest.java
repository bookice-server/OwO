package com.dgsw.bookice.repository;

import com.dgsw.bookice.config.QueryDslConfig;
import com.dgsw.bookice.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    private Book book1;
    private Book book2;
    private Book book3;

    @BeforeEach
    void setUp() {
        book1 = Book.builder()
                .title("클린 코드")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .publisher("인사이트")
                .isbn("9788966260959")
                .price(33000)
                .stockQuantity(100)
                .description("애자일 소프트웨어 장인 정신")
                .build();

        book2 = Book.builder()
                .title("이펙티브 자바")
                .author("조슈아 블로크")
                .category("프로그래밍")
                .publisher("인사이트")
                .isbn("9788966262281")
                .price(36000)
                .stockQuantity(0)
                .description("자바 플랫폼 Best Practice")
                .build();

        book3 = Book.builder()
                .title("혼자 공부하는 머신러닝")
                .author("박해선")
                .category("AI")
                .publisher("한빛미디어")
                .isbn("9791162243664")
                .price(28000)
                .stockQuantity(70)
                .description("머신러닝과 딥러닝 입문서")
                .build();

        bookRepository.saveAll(List.of(book1, book2, book3));
    }

    @Test
    @DisplayName("도서 저장 및 조회")
    void saveAndFindById() {
        // given
        Book newBook = Book.builder()
                .title("테스트 주도 개발")
                .author("켄트 벡")
                .category("프로그래밍")
                .price(25000)
                .stockQuantity(50)
                .build();

        // when
        Book saved = bookRepository.save(newBook);
        Optional<Book> found = bookRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("테스트 주도 개발");
        assertThat(found.get().getAuthor()).isEqualTo("켄트 벡");
    }

    @Test
    @DisplayName("제목으로 검색 (부분 일치)")
    void findByTitleContaining() {
        // when
        List<Book> books = bookRepository.findByTitleContaining("클린");

        // then
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("클린 코드");
    }

    @Test
    @DisplayName("저자로 검색 (부분 일치)")
    void findByAuthorContaining() {
        // when
        List<Book> books = bookRepository.findByAuthorContaining("마틴");

        // then
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getAuthor()).contains("마틴");
    }

    @Test
    @DisplayName("카테고리로 검색 (정확히 일치)")
    void findByCategory() {
        // when
        List<Book> books = bookRepository.findByCategory("프로그래밍");

        // then
        assertThat(books).hasSize(2);
        assertThat(books).extracting(Book::getCategory)
                .containsOnly("프로그래밍");
    }

    @Test
    @DisplayName("ISBN 존재 여부 확인")
    void existsByIsbn() {
        // when
        boolean exists = bookRepository.existsByIsbn("9788966260959");
        boolean notExists = bookRepository.existsByIsbn("0000000000000");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("가격 범위로 검색 (JPQL)")
    void findByPriceRange() {
        // when
        List<Book> books = bookRepository.findByPriceRange(30000, 35000);

        // then
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getPrice()).isBetween(30000, 35000);
    }

    @Test
    @DisplayName("재고가 있는 도서 조회 (JPQL)")
    void findBooksInStock() {
        // when
        List<Book> books = bookRepository.findBooksInStock();

        // then
        assertThat(books).hasSize(2);
        assertThat(books).allMatch(book -> book.getStockQuantity() > 0);
    }

    @Test
    @DisplayName("키워드로 검색 (페이징, JPQL)")
    void searchBooks() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Book> bookPage = bookRepository.searchBooks("자바", pageable);

        // then
        assertThat(bookPage.getContent()).hasSize(1);
        assertThat(bookPage.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("동적 검색 - 제목만 (QueryDSL)")
    void searchByConditions_OnlyTitle() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Book> bookPage = bookRepository.searchByConditions("클린", null, null, pageable);

        // then
        assertThat(bookPage.getContent()).hasSize(1);
        assertThat(bookPage.getContent().get(0).getTitle()).contains("클린");
    }

    @Test
    @DisplayName("동적 검색 - 저자만 (QueryDSL)")
    void searchByConditions_OnlyAuthor() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Book> bookPage = bookRepository.searchByConditions(null, "박해선", null, pageable);

        // then
        assertThat(bookPage.getContent()).hasSize(1);
        assertThat(bookPage.getContent().get(0).getAuthor()).isEqualTo("박해선");
    }

    @Test
    @DisplayName("동적 검색 - 카테고리만 (QueryDSL)")
    void searchByConditions_OnlyCategory() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Book> bookPage = bookRepository.searchByConditions(null, null, "프로그래밍", pageable);

        // then
        assertThat(bookPage.getContent()).hasSize(2);
        assertThat(bookPage.getContent()).allMatch(book ->
                book.getCategory().equals("프로그래밍"));
    }

    @Test
    @DisplayName("동적 검색 - 모든 조건 (QueryDSL)")
    void searchByConditions_AllConditions() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Book> bookPage = bookRepository.searchByConditions(
                "클린", "마틴", "프로그래밍", pageable);

        // then
        assertThat(bookPage.getContent()).hasSize(1);
        assertThat(bookPage.getContent().get(0).getTitle()).contains("클린");
        assertThat(bookPage.getContent().get(0).getAuthor()).contains("마틴");
        assertThat(bookPage.getContent().get(0).getCategory()).isEqualTo("프로그래밍");
    }

    @Test
    @DisplayName("동적 검색 - 조건 없음 (QueryDSL)")
    void searchByConditions_NoConditions() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Book> bookPage = bookRepository.searchByConditions(null, null, null, pageable);

        // then
        assertThat(bookPage.getContent()).hasSize(3);
        assertThat(bookPage.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("도서 수정")
    void updateBook() {
        // given
        Book book = bookRepository.findById(book1.getId()).orElseThrow();

        // when
        book.update("클린 코드 개정판", "로버트 C. 마틴", "프로그래밍",
                "인사이트", 35000, "개정판 설명");
        bookRepository.flush();

        // then
        Book updated = bookRepository.findById(book1.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("클린 코드 개정판");
        assertThat(updated.getPrice()).isEqualTo(35000);
    }

    @Test
    @DisplayName("도서 삭제")
    void deleteBook() {
        // given
        Long bookId = book1.getId();

        // when
        bookRepository.delete(book1);
        bookRepository.flush();

        // then
        Optional<Book> deleted = bookRepository.findById(bookId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("재고 증가")
    void increaseStock() {
        // given
        Book book = bookRepository.findById(book1.getId()).orElseThrow();
        int initialStock = book.getStockQuantity();

        // when
        book.increaseStock(50);
        bookRepository.flush();

        // then
        Book updated = bookRepository.findById(book1.getId()).orElseThrow();
        assertThat(updated.getStockQuantity()).isEqualTo(initialStock + 50);
    }

    @Test
    @DisplayName("재고 감소")
    void decreaseStock() {
        // given
        Book book = bookRepository.findById(book1.getId()).orElseThrow();
        int initialStock = book.getStockQuantity();

        // when
        book.decreaseStock(30);
        bookRepository.flush();

        // then
        Book updated = bookRepository.findById(book1.getId()).orElseThrow();
        assertThat(updated.getStockQuantity()).isEqualTo(initialStock - 30);
    }

    @Test
    @DisplayName("전체 도서 개수 조회")
    void countAll() {
        // when
        long count = bookRepository.count();

        // then
        assertThat(count).isEqualTo(3);
    }
}