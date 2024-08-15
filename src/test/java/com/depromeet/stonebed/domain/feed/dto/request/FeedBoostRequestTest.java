package com.depromeet.stonebed.domain.feed.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@WebMvcTest
public class FeedBoostRequestTest {
    @Autowired private Validator validator;

    @Configuration
    static class TestConfig {
        @Bean
        public Validator validator() {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            return factory.getValidator();
        }
    }

    @Test
    void 피드_부스트_카운트가_0인_경우() {
        // Given
        Long count = 0L;
        Long missionRecordId = 1L;
        FeedBoostRequest feedBoostRequest = new FeedBoostRequest(count, missionRecordId);

        // When
        Set<ConstraintViolation<FeedBoostRequest>> constraintViolations =
                validator.validate(feedBoostRequest);

        // Then
        Iterator<ConstraintViolation<FeedBoostRequest>> constraintViolationIterator =
                constraintViolations.iterator();
        List<String> messages = new ArrayList<>();

        while (constraintViolationIterator.hasNext()) {
            ConstraintViolation<FeedBoostRequest> constraintViolation =
                    constraintViolationIterator.next();
            messages.add(constraintViolation.getMessage());
        }

        assertThat(messages).contains("1 이상이어야 합니다");
    }

    @Test
    void 피드_부스트_카운트가_500보다_큰_경우() {
        // Given
        Long count = 1000L;
        Long missionRecordId = 1L;
        FeedBoostRequest feedBoostRequest = new FeedBoostRequest(count, missionRecordId);

        // When
        Set<ConstraintViolation<FeedBoostRequest>> constraintViolations =
                validator.validate(feedBoostRequest);

        // Then
        Iterator<ConstraintViolation<FeedBoostRequest>> constraintViolationIterator =
                constraintViolations.iterator();
        List<String> messages = new ArrayList<>();

        while (constraintViolationIterator.hasNext()) {
            ConstraintViolation<FeedBoostRequest> constraintViolation =
                    constraintViolationIterator.next();
            messages.add(constraintViolation.getMessage());
        }

        assertThat(messages).contains("500 이하여야 합니다");
    }

    @Test
    void 피드_부스트_미션_기록_고유번호가_0인_경우() {
        // Given
        Long count = 10L;
        Long missionRecordId = 0L;
        FeedBoostRequest feedBoostRequest = new FeedBoostRequest(count, missionRecordId);

        // When
        Set<ConstraintViolation<FeedBoostRequest>> constraintViolations =
                validator.validate(feedBoostRequest);

        // Then
        Iterator<ConstraintViolation<FeedBoostRequest>> constraintViolationIterator =
                constraintViolations.iterator();
        List<String> messages = new ArrayList<>();

        while (constraintViolationIterator.hasNext()) {
            ConstraintViolation<FeedBoostRequest> constraintViolation =
                    constraintViolationIterator.next();
            messages.add(constraintViolation.getMessage());
        }

        assertThat(messages).contains("1 이상이어야 합니다");
    }
}
