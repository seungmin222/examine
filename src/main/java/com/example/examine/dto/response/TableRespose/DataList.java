package com.example.examine.dto.response.TableRespose;

import java.util.List;

public record DataList<T>(List<T> list) implements DataStructure {}

