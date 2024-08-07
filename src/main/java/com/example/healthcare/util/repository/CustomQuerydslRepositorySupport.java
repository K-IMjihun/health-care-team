package com.example.healthcare.util.repository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.List;

@Repository
public abstract class CustomQuerydslRepositorySupport {

  private final Class domainClass;
  private Querydsl querydsl;
  private EntityManager entityManager;
  private JPAQueryFactory queryFactory;

  protected CustomQuerydslRepositorySupport(Class<?> domainClass) {
    this.domainClass = domainClass;
  }

  @Autowired
  public void setEntityManager(EntityManager entityManager) {
    JpaEntityInformation entityInformation = JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager);
    SimpleEntityPathResolver resolver = SimpleEntityPathResolver.INSTANCE;
    EntityPath path = resolver.createPath(entityInformation.getJavaType());
    this.entityManager = entityManager;
    this.querydsl = new Querydsl(entityManager, new PathBuilder<>(path.getType(), path.getMetadata()));
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  @PostConstruct
  public void validate() {
    Assert.notNull(domainClass, "Domain class must not be null!");
    Assert.notNull(querydsl, "Querydsl must not be null!");
    Assert.notNull(entityManager, "EntityManager must not be null!");
    Assert.notNull(queryFactory, "JPAQueryFactory must not be null!");
  }

  protected JPAQueryFactory getQueryFactory() {
    return queryFactory;
  }

  protected Querydsl getQuerydsl() {
    return querydsl;
  }

  protected EntityManager getEntityManager() {
    return entityManager;
  }

  protected <T> JPAQuery<T> select(Expression<T> expr) {
    return getQueryFactory().select(expr);
  }

  protected JPAQuery<Integer> selectOne() {
    return getQueryFactory().selectOne();
  }

  protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
    return getQueryFactory().selectFrom(from);
  }

  protected <T> JPAQuery<T> selectDistinct(Expression<T> from) {
    return getQueryFactory().selectDistinct(from);
  }

  protected <T> Page<T> applyPagination(Pageable pageable, JPAQuery<T> jpaQuery) {
    List<T> result = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();
    return PageableExecutionUtils.getPage(result, pageable, jpaQuery::fetchCount);
  }

  protected <T> Page<T> applyPagination(Pageable pageable, JPAQuery<T> jpaQuery, JPAQuery<Long> countQuery) {
    List<T> result = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();
    return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
  }

  protected <T> Page<T> applyNativeCountPagination(Pageable pageable, JPAQuery<T> jpaQuery) {
    List<T> result = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();
    return PageableExecutionUtils.getPage(result, pageable, () -> nativeQueryCount(jpaQuery));
  }

  protected <T> long nativeQueryCount(JPAQuery<T> jpaQuery) {
    Query nativeQuery = NativeQueryUtil.toNativeCountQuery(jpaQuery, getEntityManager());
    return ((BigInteger) (nativeQuery.getSingleResult())).longValue();
  }



}
