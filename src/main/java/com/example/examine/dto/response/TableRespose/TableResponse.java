package com.example.examine.dto.response.TableRespose;

import java.util.List;

    public record TableResponse<T extends DataStructure>(
     T data, // List, Map<string, List>
     boolean hasMore,
     int limit,
     int offset,
     int userLevel
) {
}
