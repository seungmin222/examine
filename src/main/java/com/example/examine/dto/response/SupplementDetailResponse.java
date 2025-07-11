package com.example.examine.dto.response;

import java.util.List;

public record SupplementDetailResponse(
        List<SupplementResponse> supplement,
        DetailResponse detail,
        List<TagResponse>effects,
        List<TagResponse>sideEffects
) {
}
