package com.example.examine.dto.request;

public record UserRequest (
        String username,
        String password,
        String role
){
}
