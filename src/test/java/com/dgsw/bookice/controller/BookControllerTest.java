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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

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
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("클린 코드"))
                .andExpect(jsonPath("$.data.author").value("로버트 C. 마틴"))
                .andExpect(jsonPath("$.data.price").value(33000))
                .andExpect(jsonPath("$.data.stockQuantity").value(100));
    }

    @Test
    @DisplayName("POST /api/books - 도서 등록 실패 (제목 누락)")
    void createBook_Fail_NoTitle() throws Exception {
        // given
        BookCreateRequest invalidRequest = BookCreateRequest.builder()
                .title("")  // 빈 제목
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .price(33000)
                .stockQuantity(100)
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
    @DisplayName("POST /api/books - 도서 등록 실패 (음수 가격)")
    void createBook_Fail_NegativePrice() throws Exception {
        // given
        BookCreateRequest invalidRequest = BookCreateRequest.builder()
                .title("클린 코드")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .price(-1000)  // 음수 가격
                .stockQuantity(100)
                .build();

        // when & then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/books/{id} - 도서 단건 조회 성공")
    void getBook_Success() throws Exception {
        // given
        given(bookService.getBook(1L)).willReturn(bookResponse);

        // when & then
        mockMvc.perform(get("/api/books/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("도서 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("클린 코드"))
                .andExpect(jsonPath("$.data.author").value("로버트 C. 마틴"));
    }

    @Test
    @DisplayName("GET /api/books/{id} - 도서 단건 조회 실패 (존재하지 않음)")
    void getBook_Fail_NotFound() throws Exception {
        // given
        given(bookService.getBook(999L)).willThrow(new BookNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/books/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/books - 전체 도서 목록 조회")
    void getAllBooks_Success() throws Exception {
        // given
        BookResponse book2 = BookResponse.builder()
                .id(2L)
                .title("이펙티브 자바")
                .author("조슈아 블로크")
                .category("프로그래밍")
                .price(36000)
                .stockQuantity(80)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<BookResponse> bookList = Arrays.asList(bookResponse, book2);
        given(bookService.getAllBooks()).willReturn(bookList);

        // when & then
        mockMvc.perform(get("/api/books"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("클린 코드"))
                .andExpect(jsonPath("$.data[1].title").value("이펙티브 자바"));
    }

    @Test
    @DisplayName("GET /api/books/search - 키워드 검색 (페이징)")
    void searchBooks_Success() throws Exception {
        // given
        Page<BookResponse> bookPage = new PageImpl<>(
                Arrays.asList(bookResponse),
                PageRequest.of(0, 10),
                1
        );
        given(bookService.searchBooks(eq("클린"), any())).willReturn(bookPage);

        // when & then
        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "클린")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("클린 코드"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /api/books/search/advanced - 고급 검색 (QueryDSL)")
    void searchBooksAdvanced_Success() throws Exception {
        // given
        Page<BookResponse> bookPage = new PageImpl<>(
                Arrays.asList(bookResponse),
                PageRequest.of(0, 10),
                1
        );
        given(bookService.searchBooksByConditions(
                eq("클린"), eq("마틴"), eq("프로그래밍"), any()
        )).willReturn(bookPage);

        // when & then
        mockMvc.perform(get("/api/books/search/advanced")
                        .param("title", "클린")
                        .param("author", "마틴")
                        .param("category", "프로그래밍")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("클린 코드"));
    }

    @Test
    @DisplayName("GET /api/books/search/title - 제목으로 검색")
    void searchByTitle_Success() throws Exception {
        // given
        List<BookResponse> bookList = Arrays.asList(bookResponse);
        given(bookService.searchByTitle("클린")).willReturn(bookList);

        // when & then
        mockMvc.perform(get("/api/books/search/title")
                        .param("title", "클린"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("클린 코드"));
    }

    @Test
    @DisplayName("GET /api/books/search/author - 저자로 검색")
    void searchByAuthor_Success() throws Exception {
        // given
        List<BookResponse> bookList = Arrays.asList(bookResponse);
        given(bookService.searchByAuthor("마틴")).willReturn(bookList);

        // when & then
        mockMvc.perform(get("/api/books/search/author")
                        .param("author", "마틴"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].author").value("로버트 C. 마틴"));
    }

    @Test
    @DisplayName("GET /api/books/search/category - 카테고리로 검색")
    void searchByCategory_Success() throws Exception {
        // given
        List<BookResponse> bookList = Arrays.asList(bookResponse);
        given(bookService.searchByCategory("프로그래밍")).willReturn(bookList);

        // when & then
        mockMvc.perform(get("/api/books/search/category")
                        .param("category", "프로그래밍"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].category").value("프로그래밍"));
    }

    @Test
    @DisplayName("GET /api/books/search/price - 가격 범위로 검색")
    void searchByPriceRange_Success() throws Exception {
        // given
        List<BookResponse> bookList = Arrays.asList(bookResponse);
        given(bookService.searchByPriceRange(30000, 35000)).willReturn(bookList);

        // when & then
        mockMvc.perform(get("/api/books/search/price")
                        .param("minPrice", "30000")
                        .param("maxPrice", "35000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].price").value(33000));
    }

    @Test
    @DisplayName("GET /api/books/in-stock - 재고 있는 도서 조회")
    void getBooksInStock_Success() throws Exception {
        // given
        List<BookResponse> bookList = Arrays.asList(bookResponse);
        given(bookService.getBooksInStock()).willReturn(bookList);

        // when & then
        mockMvc.perform(get("/api/books/in-stock"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].stockQuantity").value(100));
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

        given(bookService.updateBook(eq(1L), any(BookUpdateRequest.class)))
                .willReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("도서가 성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.data.title").value("클린 코드 (개정판)"))
                .andExpect(jsonPath("$.data.price").value(35000));
    }

    @Test
    @DisplayName("PUT /api/books/{id} - 도서 수정 실패 (존재하지 않음)")
    void updateBook_Fail_NotFound() throws Exception {
        // given
        given(bookService.updateBook(eq(999L), any(BookUpdateRequest.class)))
                .willThrow(new BookNotFoundException(999L));

        // when & then
        mockMvc.perform(put("/api/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/books/{id} - 도서 수정 실패 (유효성 검증)")
    void updateBook_Fail_Validation() throws Exception {
        // given
        BookUpdateRequest invalidRequest = BookUpdateRequest.builder()
                .title("")  // 빈 제목
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .price(-1000)  // 음수 가격
                .build();

        // when & then
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - 도서 삭제 성공")
    void deleteBook_Success() throws Exception {
        // given
        doNothing().when(bookService).deleteBook(1L);

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
        doThrow(new BookNotFoundException(999L)).when(bookService).deleteBook(999L);

        // when & then
        mockMvc.perform(delete("/api/books/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("POST /api/books/{id}/stock/increase - 재고 증가 성공")
    void increaseStock_Success() throws Exception {
        // given
        BookResponse increasedResponse = BookResponse.builder()
                .id(1L)
                .title("클린 코드")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .price(33000)
                .stockQuantity(150)  // 100 + 50
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(bookService.increaseStock(1L, 50)).willReturn(increasedResponse);

        // when & then
        mockMvc.perform(post("/api/books/1/stock/increase")
                        .param("quantity", "50"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("재고가 증가되었습니다."))
                .andExpect(jsonPath("$.data.stockQuantity").value(150));
    }

    @Test
    @DisplayName("POST /api/books/{id}/stock/increase - 재고 증가 실패 (존재하지 않음)")
    void increaseStock_Fail_NotFound() throws Exception {
        // given
        given(bookService.increaseStock(999L, 50))
                .willThrow(new BookNotFoundException(999L));

        // when & then
        mockMvc.perform(post("/api/books/999/stock/increase")
                        .param("quantity", "50"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/books/{id}/stock/decrease - 재고 감소 성공")
    void decreaseStock_Success() throws Exception {
        // given
        BookResponse decreasedResponse = BookResponse.builder()
                .id(1L)
                .title("클린 코드")
                .author("로버트 C. 마틴")
                .category("프로그래밍")
                .price(33000)
                .stockQuantity(70)  // 100 - 30
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(bookService.decreaseStock(1L, 30)).willReturn(decreasedResponse);

        // when & then
        mockMvc.perform(post("/api/books/1/stock/decrease")
                        .param("quantity", "30"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("재고가 감소되었습니다."))
                .andExpect(jsonPath("$.data.stockQuantity").value(70));
    }

    @Test
    @DisplayName("POST /api/books/{id}/stock/decrease - 재고 감소 실패 (재고 부족)")
    void decreaseStock_Fail_InsufficientStock() throws Exception {
        // given
        given(bookService.decreaseStock(1L, 200))
                .willThrow(new IllegalStateException("재고가 부족합니다. 현재 재고: 100"));

        // when & then
        mockMvc.perform(post("/api/books/1/stock/decrease")
                        .param("quantity", "200"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/books/{id}/stock/decrease - 재고 감소 실패 (존재하지 않음)")
    void decreaseStock_Fail_NotFound() throws Exception {
        // given
        given(bookService.decreaseStock(999L, 30))
                .willThrow(new BookNotFoundException(999L));

        // when & then
        mockMvc.perform(post("/api/books/999/stock/decrease")
                        .param("quantity", "30"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}