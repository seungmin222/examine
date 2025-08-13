package com.example.examine.dto.response.TableRespose;

import java.util.List;
import java.util.Map;

public record DataListInMap<T>(Map<String, List<T>> map) implements DataStructure {}
