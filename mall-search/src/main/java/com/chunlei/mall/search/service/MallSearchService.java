package com.chunlei.mall.search.service;

import com.chunlei.mall.search.vo.SearchParam;
import com.chunlei.mall.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}
