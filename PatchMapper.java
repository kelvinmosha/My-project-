package com.university.hostel.util;

import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Applies a partial (PATCH-style) update onto an existing DTO snapshot.
 * Only the keys present in the "updates" map are overwritten - any field
 * left out of the request body keeps its current value. This gives PATCH
 * true partial-update semantics (unlike PUT, which is a full replace).
 */
@Component
public class PatchMapper {

    private final ObjectMapper objectMapper;

    public PatchMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T merge(T target, Map<String, Object> updates) {
        try {
            return objectMapper.updateValue(target, updates);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid patch payload: " + e.getMessage(), e);
        }
    }
}
