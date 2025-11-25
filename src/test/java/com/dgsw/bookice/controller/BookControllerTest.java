package com.dgsw.bookice.controller;

import com.dgsw.bookice.dto.request.BookCreateRequest;
import com.dgsw.bookice.dto.request.BookUpdateRequest;
import com.dgsw.bookice.dto.response.BookResponse;
import com.dgsw.bookice.exception.BookNotFoundException;
import com.dgsw.bookice.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private BookResponse bookResponse;
    private BookCreateRequest createRequest;
    private BookUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        bookResponse = BookResponse.builder()
                .id(1L)
                .title("클린 코드")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .publisher("인사이트")
                .isbn("9788966260959")
                .price(33000)
                .stockQuantity(100)
                .description("애자일 소프트웨어 장인 정신")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
    @DisplayName("POST /api/books - 도서 등록 성공")
    void createBook_Success() throws Exception {
        // given
        given(bookService.createBook(any(BookCreateRequest.class))).willReturn(bookResponse);

        // when & then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("도서가 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data.title").value("클린 코드"))
                .andExpect(jsonPath("$.data.author").value("로버트 C. 마틴"));
    }

    @Test
    @DisplayName("POST /api/books - 도서 등록 실패 (유효성 검증)")
    void createBook_Fail_Validation() throws Exception {
        // given
        BookCreateRequest invalidRequest = BookCreateRequest.builder()
                .title("")  // 빈 제목
                .author("")  // 빈 저자
                .category("프로그래밍")
                .price(-1000)  // 음수 가격
                .stockQuantity(-10)  // 음수 재고
                .build();

        // when & then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("GET /api/books/{id} - 도서 단건 조회 성공")
    void getBook_Success() throws Exception {
        // given
        given(bookService.getBook(anyLong())).willReturn(bookResponse);

        // when & then
        mockMvc.perform(get("/api/books/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("클린 코드"));
    }

    @Test
    @DisplayName("GET /api/books/{id} - 도서 단건 조회 실패 (존재하지 않음)")
    void getBook_Fail_NotFound() throws Exception {
        // given
        given(bookService.getBook(anyLong())).willThrow(new BookNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/books/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/books - 전체 도서 목록 조회")
    void getAllBooks_Success() throws Exception {
        // given
        List<BookResponse> bookList = Arrays.asList(bookResponse, bookResponse);
        given(bookService.getAllBooks()).willReturn(bookList);

        // when & then
        mockMvc.perform(get("/api/books"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/books/search - 도서 검색 (페이징)")
    void searchBooks_Success() throws Exception {
        // given
        Page<BookResponse> bookPage = new PageImpl<>(
                Arrays.asList(bookResponse),
                PageRequest.of(0, 10),
                1
        );
        given(bookService.searchBooks(anyString(), any())).willReturn(bookPage);

        // when & then
        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "클린")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/books/search/title - 제목으로 검색")
    void searchByTitle_Success() throws Exception {
        // given
        List<BookResponse> bookList = Arrays.asList(bookResponse);
        given(bookService.searchByTitle(anyString())).willReturn(bookList);

        // when & then
        mockMvc.perform(get("/api/books/search/title")
                        .param("title", "클린"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("PUT /api/books/{id} - 도서 수정 성공")
    void updateBook_Success() throws Exception {
        // given
        BookResponse updatedResponse = BookResponse.builder()
                .id(1L)
                .title("클린 코드 (개정판)")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .publisher("인사이트")
                .isbn("9788966260959")
                .price(35000)
                .stockQuantity(100)
                .description("애자일 소프트웨어 장인 정신 개정판")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(bookService.updateBook(anyLong(), any(BookUpdateRequest.class)))
                .willReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("클린 코드 (개정판)"))
                .andExpect(jsonPath("$.data.price").value(35000));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - 도서 삭제 성공")
    void deleteBook_Success() throws Exception {
        // given
        doNothing().when(bookService).deleteBook(anyLong());

        // when & then
        mockMvc.perform(delete("/api/books/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("도서가 성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - 도서 삭제 실패 (존재하지 않음)")
    void deleteBook_Fail_NotFound() throws Exception {
        // given
        doThrow(new BookNotFoundException(999L)).when(bookService).deleteBook(anyLong());

        // when & then
        mockMvc.perform(delete("/api/books/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/books/{id}/stock/increase - 재고 증가")
    void increaseStock_Success() throws Exception {
        // given
        BookResponse increasedStockResponse = BookResponse.builder()
                .id(1L)
                .title("클린 코드")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .publisher("인사이트")
                .isbn("9788966260959")
                .price(33000)
                .stockQuantity(150)  // 증가된 재고
                .description("애자일 소프트웨어 장인 정신")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(bookService.increaseStock(anyLong(), anyInt())).willReturn(increasedStockResponse);

        // when & then
        mockMvc.perform(post("/api/books/1/stock/increase")
                        .param("quantity", "50"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stockQuantity").value(150));
    }

    @Test
    @DisplayName("POST /api/books/{id}/stock/decrease - 재고 감소")
    void decreaseStock_Success() throws Exception {
        // given
        BookResponse decreasedStockResponse = BookResponse.builder()
                .id(1L)
                .title("클린 코드")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .publisher("인사이트")
                .isbn("9788966260959")
                .price(33000)
                .stockQuantity(70)  // 감소된 재고
                .description("애자일 소프트웨어 장인 정신")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(bookService.decreaseStock(anyLong(), anyInt())).willReturn(decreasedStockResponse);

        // when & then
        mockMvc.perform(post("/api/books/1/stock/decrease")
                        .param("quantity", "30"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stockQuantity").value(70));
    }
}