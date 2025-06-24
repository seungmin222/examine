// SupplementRequest.java
package com.example.examine.dto.request;

import com.example.examine.entity.*;
import java.math.BigDecimal;
import java.util.List;

public record SupplementRequest(
        String korName,
        String engName,
        BigDecimal dosageValue,
        String dosageUnit,
        BigDecimal cost,
        List<Long> typeIds
) {

}
