package com.example.examine.dto.response;

import java.util.List;

    public record TableResponse<T>(
     List<T> data,
     boolean hasMore
) {
}
