package com.depromeet.stonebed.global.common.response;

import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.ErrorResponse;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.depromeet")
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public ApiResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(
            MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        HttpServletResponse servletResponse =
                ((ServletServerHttpResponse) response).getServletResponse();
        HttpStatus status = HttpStatus.valueOf(servletResponse.getStatus());

        ApiResponse apiResponse;
        if (status.is2xxSuccessful()) {
            apiResponse = ApiResponse.success(status.value(), body);
        } else {
            apiResponse = ApiResponse.fail(status.value(), (ErrorResponse) body);
        }

        if (MappingJackson2HttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            servletResponse.setStatus(apiResponse.status());
            return apiResponse;
        } else if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            try {
                response.getHeaders().set("Content-Type", "application/json");
                String json = objectMapper.writeValueAsString(apiResponse);
                servletResponse.setStatus(apiResponse.status());
                return json;
            } catch (JsonProcessingException e) {
                throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR);
            }
        }

        return apiResponse;
    }
}
