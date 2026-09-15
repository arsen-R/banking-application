package com.arsen.userservice.client;

import com.arsen.userservice.exception.HttpErrorResponseException;
import com.arsen.userservice.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomErrorDecoder implements ErrorDecoder {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        try (InputStream inputStream = response.body().asInputStream()) {
            Map<String, String> errors = mapper.readValue(IOUtils.toString(inputStream, StandardCharsets.UTF_8), Map.class);
            if (response.status() == 400) {
                return new ValidationException(errors);
            } else {
                return new HttpErrorResponseException(errors.get("error"), HttpStatus.valueOf(response.status()));
            }
        } catch (IOException e) {
            throw new HttpErrorResponseException(e.getMessage(), HttpStatus.valueOf(response.status()));
        }
    }
}
