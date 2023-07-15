package com.chunlei.mall.search.service.Impl;

import com.alibaba.fastjson.JSON;
import com.chunlei.mall.common.to.SkuEsModel;
import com.chunlei.mall.search.config.ElasticConfig;
import com.chunlei.mall.search.constant.EsConstant;
import com.chunlei.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.ibatis.logging.Log;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        //保存到es
        //1.给es中建立索引，product,建立映射关系

        //2.给es保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel esModel : skuEsModels) {
            //构造保存请求
            IndexRequest indexRequest  = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(esModel.getSkuId().toString());
            String jsonString = JSON.toJSONString(esModel);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticConfig.COMMON_OPTIONS);
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        if (b) {
            log.error("商品上架错误：{}", collect);
        }
        return  b;
    }
}
