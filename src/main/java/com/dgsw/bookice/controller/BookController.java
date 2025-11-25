package com.dgsw.bookice.controller;

import com.dgsw.bookice.dto.request.BookCreateRequest;
import com.dgsw.bookice.dto.request.BookUpdateRequest;
import com.dgsw.bookice.dto.response.ApiResponse;
import com.dgsw.bookice.dto.response.BookResponse;
import com.dgsw.bookice.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 도서 등록
     * POST /api/books
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @Valid @RequestBody BookCreateRequest request) {
        log.info("POST /api/books - 도서 등록 요청");

        BookResponse response = bookService.createBook(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("도서가 성공적으로 등록되었습니다.", response));
    }

    /**
     * 도서 단건 조회
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable Long id) {
        log.info("GET /api/books/{} - 도서 조회 요청", id);

        BookResponse response = bookService.getBook(id);

        return ResponseEntity.ok(ApiResponse.success("도서 조회 성공", response));
    }

    /**
     * 전체 도서 목록 조회
     * GET /api/books
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAllBooks() {
        log.info("GET /api/books - 전체 도서 목록 조회 요청");

        List<BookResponse> response = bookService.getAllBooks();

        return ResponseEntity.ok(ApiResponse.success("도서 목록 조회 성공", response));
    }

    /**
     * 도서 검색 (키워드, 페이징)
     * GET /api/books/search?keyword=검색어&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/books/search - 도서 검색 요청: keyword={}", keyword);

        Page<BookResponse> response = bookService.searchBooks(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success("도서 검색 성공", response));
    }

    /**
     * 도서 동적 검색 (QueryDSL)
     * GET /api/books/search/advanced?title=제목&author=저자&category=카테고리
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooksAdvanced(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/books/search/advanced - 동적 검색 요청");

        Page<BookResponse> response = bookService.searchBooksByConditions(
                title, author, category, pageable);

        return ResponseEntity.ok(ApiResponse.success("도서 검색 성공", response));
    }

    /**
     * 제목으로 검색
     * GET /api/books/search/title?title=검색어
     */
    @GetMapping("/search/title")
    public ResponseEntity<ApiResponse<List<BookResponse>>> searchByTitle(
            @RequestParam String title) {
        log.info("GET /api/books/search/title - 제목 검색: {}", title);

        List<BookResponse> response = bookService.searchByTitle(title);

        return ResponseEntity.ok(ApiResponse.success("제목 검색 성공", response));
    }

    /**
     * 저자로 검색
     * GET /api/books/search/author?author=검색어
     */
    @GetMapping("/search/author")
    public ResponseEntity<ApiResponse<List<BookResponse>>> searchByAuthor(
            @RequestParam String author) {
        log.info("GET /api/books/search/author - 저자 검색: {}", author);

        List<BookResponse> response = bookService.searchByAuthor(author);

        return ResponseEntity.ok(ApiResponse.success("저자 검색 성공", response));
    }

    /**
     * 카테고리로 검색
     * GET /api/books/search/category?category=카테고리
     */
    @GetMapping("/search/category")
    public ResponseEntity<ApiResponse<List<BookResponse>>> searchByCategory(
            @RequestParam String category) {
        log.info("GET /api/books/search/category - 카테고리 검색: {}", category);

        List<BookResponse> response = bookService.searchByCategory(category);

        return ResponseEntity.ok(ApiResponse.success("카테고리 검색 성공", response));
    }

    /**
     * 가격 범위로 검색
     * GET /api/books/search/price?minPrice=10000&maxPrice=30000
     */
    @GetMapping("/search/price")
    public ResponseEntity<ApiResponse<List<BookResponse>>> searchByPriceRange(
            @RequestParam Integer minPrice,
            @RequestParam Integer maxPrice) {
        log.info("GET /api/books/search/price - 가격 범위 검색: {} ~ {}", minPrice, maxPrice);

        List<BookResponse> response = bookService.searchByPriceRange(minPrice, maxPrice);

        return ResponseEntity.ok(ApiResponse.success("가격 범위 검색 성공", response));
    }

    /**
     * 재고가 있는 도서 조회
     * GET /api/books/in-stock
     */
    @GetMapping("/in-stock")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksInStock() {
        log.info("GET /api/books/in-stock - 재고 있는 도서 조회");

        List<BookResponse> response = bookService.getBooksInStock();

        return ResponseEntity.ok(ApiResponse.success("재고 있는 도서 조회 성공", response));
    }

    /**
     * 도서 정보 수정
     * PUT /api/books/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        log.info("PUT /api/books/{} - 도서 수정 요청", id);

        BookResponse response = bookService.updateBook(id, request);

        return ResponseEntity.ok(ApiResponse.success("도서가 성공적으로 수정되었습니다.", response));
    }

    /**
     * 도서 삭제
     * DELETE /api/books/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        log.info("DELETE /api/books/{} - 도서 삭제 요청", id);

        bookService.deleteBook(id);

        return ResponseEntity.ok(ApiResponse.success("도서가 성공적으로 삭제되었습니다."));
    }

    /**
     * 재고 증가
     * POST /api/books/{id}/stock/increase?quantity=10
     */
    @PostMapping("/{id}/stock/increase")
    public ResponseEntity<ApiResponse<BookResponse>> increaseStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        log.info("POST /api/books/{}/stock/increase - 재고 증가 요청: {}", id, quantity);

        BookResponse response = bookService.increaseStock(id, quantity);

        return ResponseEntity.ok(ApiResponse.success("재고가 증가되었습니다.", response));
    }

    /**
     * 재고 감소
     * POST /api/books/{id}/stock/decrease?quantity=5
     */
    @PostMapping("/{id}/stock/decrease")
    public ResponseEntity<ApiResponse<BookResponse>> decreaseStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        log.info("POST /api/books/{}/stock/decrease - 재고 감소 요청: {}", id, quantity);

        BookResponse response = bookService.decreaseStock(id, quantity);

        return ResponseEntity.ok(ApiResponse.success("재고가 감소되었습니다.", response));
    }
}
