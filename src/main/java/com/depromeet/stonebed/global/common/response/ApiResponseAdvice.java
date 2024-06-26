package com.depromeet.stonebed.global.common.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestControllerAdvice(basePackages = "com.depromeet")
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

	private final ObjectMapper objectMapper;

	public ApiResponseAdvice(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
		Class<? extends HttpMessageConverter<?>> selectedConverterType,
		ServerHttpRequest request, ServerHttpResponse response) {

		ApiResponse apiResponse;
		if (body instanceof ApiResponse) {
			apiResponse = (ApiResponse) body;
		} else {
			apiResponse = ApiResponse.success(body);
		}

		if (MappingJackson2HttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
			return apiResponse;
		}

		try {
			response.getHeaders().set("Content-Type", "application/json");
			return objectMapper.writeValueAsString(apiResponse);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
