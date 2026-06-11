package com.microservice.justanotherapp.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDto {
    private Integer status;
    private String message;
    private Long timestamp;
}
