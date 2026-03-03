// service/SearchService.java
package com.example.demo.service;

import com.example.demo.common.Result;
import java.util.Map;

public interface SearchService {
    Result searchAll(String keyword, Integer page, Integer size);
    Result searchProducts(String keyword, Integer page, Integer size);
    Result searchTopics(String keyword, Integer page, Integer size);
    Result searchUsers(String keyword, Integer page, Integer size);
}
