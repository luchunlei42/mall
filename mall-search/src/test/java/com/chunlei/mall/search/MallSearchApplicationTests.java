package com.chunlei.mall.search;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;
    @Test
    public void contextLoads(){
        System.out.println(client);
    }
}
