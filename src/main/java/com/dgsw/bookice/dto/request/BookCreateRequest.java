package com.dgsw.bookice.dto.request;

import com.dgsw.bookice.entity.Book;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "저자는 필수입니다.")
    @Size(max = 100, message = "저자는 100자를 초과할 수 없습니다.")
    private String author;

    @NotBlank(message = "카테고리는 필수입니다.")
    @Size(max = 50, message = "카테고리는 50자를 초과할 수 없습니다.")
    private String category;

    @Size(max = 100, message = "출판사는 100자를 초과할 수 없습니다.")
    private String publisher;

    @Pattern(regexp = "^\\d{13}$", message = "ISBN은 13자리 숫자여야 합니다.")
    private String isbn;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private Integer stockQuantity;

    private String description;

    public Book toEntity() {
        return Book.builder()
                .title(title)
                .author(author)
                .category(category)
                .publisher(publisher)
                .isbn(isbn)
                .price(price)
                .stockQuantity(stockQuantity)
                .description(description)
                .build();
    }
}
