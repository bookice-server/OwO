package com.dgsw.bookice.service;

import com.dgsw.bookice.dto.request.BookCreateRequest;
import com.dgsw.bookice.dto.request.BookUpdateRequest;
import com.dgsw.bookice.dto.response.BookResponse;
import com.dgsw.bookice.entity.Book;
import com.dgsw.bookice.exception.BookNotFoundException;
import com.dgsw.bookice.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookCreateRequest createRequest;
    private BookUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .title("클린 코드")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .publisher("인사이트")
                .isbn("9788966260959")
                .price(33000)
                .stockQuantity(100)
                .description("애자일 소프트웨어 장인 정신")
                .build();

        createRequest = BookCreateRequest.builder()
                .title("클린 코드")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .publisher("인사이트")
                .isbn("9788966260959")
                .price(33000)
                .stockQuantity(100)
                .description("애자일 소프트웨어 장인 정신")
                .build();

        updateRequest = BookUpdateRequest.builder()
                .title("클린 코드 (개정판)")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .publisher("인사이트")
                .price(35000)
                .description("애자일 소프트웨어 장인 정신 개정판")
                .build();
    }

    @Test
    @DisplayName("도서 등록 성공")
    void createBook_Success() {
        // given
        given(bookRepository.existsByIsbn(anyString())).willReturn(false);
        given(bookRepository.save(any(Book.class))).willReturn(book);

        // when
        BookResponse response = bookService.createBook(createRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("클린 코드");
        assertThat(response.getAuthor()).isEqualTo("로버트 C. 마틴");
        verify(bookRepository, times(1)).existsByIsbn(anyString());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("도서 등록 실패 - ISBN 중복")
    void createBook_Fail_DuplicateIsbn() {
        // given
        given(bookRepository.existsByIsbn(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> bookService.createBook(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 존재하는 ISBN입니다");

        verify(bookRepository, times(1)).existsByIsbn(anyString());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("도서 단건 조회 성공")
    void getBook_Success() {
        // given
        given(bookRepository.findById(anyLong())).willReturn(Optional.of(book));

        // when
        BookResponse response = bookService.getBook(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("클린 코드");
        verify(bookRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("도서 단건 조회 실패 - 존재하지 않는 도서")
    void getBook_Fail_NotFound() {
        // given
        given(bookRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookService.getBook(999L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("도서를 찾을 수 없습니다");

        verify(bookRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("전체 도서 목록 조회 성공")
    void getAllBooks_Success() {
        // given
        List<Book> books = Arrays.asList(book, book);
        given(bookRepository.findAll()).willReturn(books);

        // when
        List<BookResponse> responses = bookService.getAllBooks();

        // then
        assertThat(responses).hasSize(2);
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("도서 검색 성공 (페이징)")
    void searchBooks_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(book), pageable, 1);
        given(bookRepository.searchBooks(anyString(), any(Pageable.class))).willReturn(bookPage);

        // when
        Page<BookResponse> responses = bookService.searchBooks("클린", pageable);

        // then
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getTotalElements()).isEqualTo(1);
        verify(bookRepository, times(1)).searchBooks(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("도서 수정 성공")
    void updateBook_Success() {
        // given
        given(bookRepository.findById(anyLong())).willReturn(Optional.of(book));

        // when
        BookResponse response = bookService.updateBook(1L, updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("클린 코드 (개정판)");
        assertThat(response.getPrice()).isEqualTo(35000);
        verify(bookRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("도서 삭제 성공")
    void deleteBook_Success() {
        // given
        given(bookRepository.findById(anyLong())).willReturn(Optional.of(book));
        doNothing().when(bookRepository).delete(any(Book.class));

        // when
        bookService.deleteBook(1L);

        // then
        verify(bookRepository, times(1)).findById(anyLong());
        verify(bookRepository, times(1)).delete(any(Book.class));
    }

    @Test
    @DisplayName("재고 증가 성공")
    void increaseStock_Success() {
        // given
        given(bookRepository.findById(anyLong())).willReturn(Optional.of(book));
        int initialStock = book.getStockQuantity();

        // when
        BookResponse response = bookService.increaseStock(1L, 50);

        // then
        assertThat(response.getStockQuantity()).isEqualTo(initialStock + 50);
        verify(bookRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("재고 감소 성공")
    void decreaseStock_Success() {
        // given
        given(bookRepository.findById(anyLong())).willReturn(Optional.of(book));
        int initialStock = book.getStockQuantity();

        // when
        BookResponse response = bookService.decreaseStock(1L, 30);

        // then
        assertThat(response.getStockQuantity()).isEqualTo(initialStock - 30);
        verify(bookRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("재고 감소 실패 - 재고 부족")
    void decreaseStock_Fail_InsufficientStock() {
        // given
        given(bookRepository.findById(anyLong())).willReturn(Optional.of(book));

        // when & then
        assertThatThrownBy(() -> bookService.decreaseStock(1L, 200))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");

        verify(bookRepository, times(1)).findById(anyLong());
    }
}