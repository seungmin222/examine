package com.example.examine.dto.request;

public record TagDetailRequest (
        Long id,
        String type,
        String overview,
        String intro
){
}
