package com.dgsw.bookice.repository;

import com.dgsw.bookice.entity.Book;
import com.dgsw.bookice.entity.QBook;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Book> searchByConditions(String title, String author, String category, Pageable pageable) {
        QBook book = QBook.book;

        // 동적 쿼리 생성
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(title)) {
            builder.and(book.title.containsIgnoreCase(title));
        }

        if (StringUtils.hasText(author)) {
            builder.and(book.author.containsIgnoreCase(author));
        }

        if (StringUtils.hasText(category)) {
            builder.and(book.category.eq(category));
        }

        // 전체 개수 조회
        Long total = queryFactory
                .select(book.count())
                .from(book)
                .where(builder)
                .fetchOne();

        // 페이징된 결과 조회
        List<Book> books = queryFactory
                .selectFrom(book)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(book.createdAt.desc())
                .fetch();

        return new PageImpl<>(books, pageable, total != null ? total : 0L);
    }
}
