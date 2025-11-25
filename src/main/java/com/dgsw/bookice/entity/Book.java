package com.dgsw.bookice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "isbn", unique = true, length = 13)
    private String isbn;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder
    public Book(String title, String author, String category, String publisher,
                String isbn, Integer price, Integer stockQuantity, String description) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.publisher = publisher;
        this.isbn = isbn;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
    }

    /**
     * 도서 정보 수정
     */
    public void update(String title, String author, String category, String publisher,
                       Integer price, String description) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.publisher = publisher;
        this.price = price;
        this.description = description;
    }

    /**
     * 재고 증가
     */
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * 재고 감소
     */
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + this.stockQuantity);
        }
        this.stockQuantity -= quantity;
    }
}
