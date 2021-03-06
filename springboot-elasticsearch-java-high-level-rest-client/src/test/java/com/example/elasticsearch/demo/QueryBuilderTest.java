package com.example.elasticsearch.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.elasticsearch.demo.model.BaseInfo;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QueryBuilderTest {

    @Resource
    private RestHighLevelClient client;


    /**
     * 查询全部
     */
    @Test
    public void allQuery() throws Exception {
        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        System.out.println(searchByQueryBuilder(client, queryBuilder));
    }

    /**
     * 匹配查询
     */
    @Test
    public void matchQuery() throws IOException {
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("projectName", "北***京");
        String queryName = queryBuilder.queryName();// 本次查询的名称，可自定义
        String name = queryBuilder.getName(); // match
        System.out.println(searchByQueryBuilder(client, queryBuilder));
        client.close();
    }

    /**
     * 多个匹配查询
     */
    @Test
    public void multiMatchQuery() throws Exception {
        // 查询projectName、shortName、businessAddress、businessMode这四个字段中含有北京的条目，支持通配符，字段名区分大小写

        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("金食", "projectName", "shortName", "businessModel", "businessAddress")
                .operator(Operator.OR)
                //.field("updateTime")
                //.fuzziness(Fuzziness.AUTO)
                //.analyzer("ik_smart")
                //.type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                ;
        // 查询以Name结尾或以business开头的字段中含有北京的
//        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("北京", "*Name", "business*", "shortName");
        // 不提供字段的时候默认为全部字段，上限为1024
        // QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("北京");
        System.out.println(searchByQueryBuilder(client, queryBuilder));
        client.close();
    }

    @Test
    public void termQuery() throws Exception {
        QueryBuilder queryBuilder = QueryBuilders.termsQuery("projectName", "北京", "宁德", "百度", "businessAddress");
        System.out.println(searchByQueryBuilder(client, queryBuilder));
        client.close();
    }

    @Test
    public void termsQuery() throws Exception {
        QueryBuilder queryBuilder = QueryBuilders.termsQuery("projectName", "北京", "宁德", "百度", "businessAddress");
        System.out.println(searchByQueryBuilder(client, queryBuilder));
        client.close();
    }

    @Test
    public void boolQuery() throws Exception {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 名称中包含北京
        QueryBuilder nameQueryBuilder = QueryBuilders.termQuery("projectName", "宁德");
        boolQueryBuilder.must(nameQueryBuilder);
        //时间大于43671
        RangeQueryBuilder timeQueryBuilder = QueryBuilders.rangeQuery("updateTime").gt(43671);
        boolQueryBuilder.must(timeQueryBuilder);
        // id范围
        int[] ids = {93, 94, 95, 96, 97, 98, 99, 124, 125, 126, 127, 128};
        TermsQueryBuilder idQueryBuilder = QueryBuilders.termsQuery("id", ids);
        boolQueryBuilder.must(idQueryBuilder);

        System.out.println(searchByQueryBuilder(client, boolQueryBuilder));
        client.close();
    }

    @Test
    public void stringQuery() throws Exception {
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("股权");
        System.out.println(searchByQueryBuilder(client, queryBuilder));
        client.close();
    }


    private static List searchByQueryBuilder(RestHighLevelClient client, QueryBuilder queryBuilder) throws IOException {
        //String[] includes = {"id", "investId", "projectName", "shortName", "businessAddress", "businessModel", "updateTime"};
        SearchRequest searchRequest = new SearchRequest("index_baseinfo*");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0).size(10000);
        //searchSourceBuilder.fetchSource(includes, null);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        List<BaseInfo> list = new ArrayList<>();
        for (SearchHit searchHit : hits) {
            String sourceAsString = searchHit.getSourceAsString();
            list.add(JSON.parseObject(sourceAsString, new TypeReference<BaseInfo>() {
            }));
        }
        return list;
    }
}

