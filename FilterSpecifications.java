package com.university.hostel.util;

import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class FilterSpecifications {
    private FilterSpecifications() {
    }

    public static <T> Specification<T> equal(String property, Object value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(property), value);
    }

    public static <T> Specification<T> containsIgnoreCase(String property, String value) {
        String pattern = "%" + escapeLike(value.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(property)), pattern, '\\');
    }

    public static <T> Specification<T> exactIgnoreCase(String property, String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                criteriaBuilder.lower(root.get(property)), normalized);
    }

    public static <T, V extends Comparable<? super V>> Specification<T> greaterThanOrEqualTo(
            String property, V value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.<V>get(property), value);
    }

    public static <T, V extends Comparable<? super V>> Specification<T> lessThanOrEqualTo(
            String property, V value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.<V>get(property), value);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}