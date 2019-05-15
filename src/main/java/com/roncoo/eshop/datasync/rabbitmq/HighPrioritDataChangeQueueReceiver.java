package com.roncoo.eshop.datasync.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.roncoo.eshop.datasync.service.EshopProductService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 数据同步服务
 */
@Component
@RabbitListener(queues = "high-priority-data-chang-queue")
public class HighPrioritDataChangeQueueReceiver {
    @Autowired
    private EshopProductService eshopProductService;
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private  RabbitMQSender rabbitMQSender;
    private Set<String> dimDataChangeMessageSet= Collections.synchronizedSet(new HashSet<>());
public HighPrioritDataChangeQueueReceiver(){
    new sendThread().start();
}
    @RabbitHandler
    public void process(String message) {
        // 对这个message进行解析
        JSONObject jsonObject = JSONObject.parseObject(message);

        // 先获取data_type
        String dataType = jsonObject.getString("data_type");
        if("brand".equals(dataType)) {
            processBrandDataChangeMessage(jsonObject);
        } else if("category".equals(dataType)) {
            processCategoryDataChangeMessage(jsonObject);
        } else if("product_intro".equals(dataType)) {
            processProductIntroDataChangeMessage(jsonObject);
        } else if("product_property".equals(dataType)) {
            processProductPropertyDataChangeMessage(jsonObject);
        } else if("product".equals(dataType)) {
            processProductDataChangeMessage(jsonObject);
        } else if("product_specification".equals(dataType)) {
            processProductSpecificationDataChangeMessage(jsonObject);
        }
    }

    private void processBrandDataChangeMessage(JSONObject messageJSONObject) {
        Long id = messageJSONObject.getLong("id");
        String eventType = messageJSONObject.getString("event_type");

        if("add".equals(eventType) || "update".equals(eventType)) {
            JSONObject dataJSONObject = JSONObject.parseObject(eshopProductService.findBrandById(id));
            Jedis jedis = jedisPool.getResource();
            jedis.set("brand_" + dataJSONObject.getLong("id"), dataJSONObject.toJSONString());
        } else if ("delete".equals(eventType)) {
            Jedis jedis = jedisPool.getResource();
            jedis.del("brand_" + id);
        }

       dimDataChangeMessageSet.add("{\"dim_type\": \"brand\", \"id\": " + id + "}");
        System.out.println("【平牌维度数据变更消息被放入内存set中】");
    }

    private void processCategoryDataChangeMessage(JSONObject messageJSONObject) {
        Long id = messageJSONObject.getLong("id");
        String eventType = messageJSONObject.getString("event_type");

        if("add".equals(eventType) || "update".equals(eventType)) {
            JSONObject dataJSONObject = JSONObject.parseObject(eshopProductService.findCategoryById(id));
            System.out.println("=============="+id+"===============");
            System.out.println(dataJSONObject);
            Jedis jedis = jedisPool.getResource();
            jedis.set("category_" + dataJSONObject.getLong("id"), dataJSONObject.toJSONString());
        } else if ("delete".equals(eventType)) {
            Jedis jedis = jedisPool.getResource();
            jedis.del("category_" + id);
        }

       dimDataChangeMessageSet.add("{\"dim_type\": \"category\", \"id\": " + id + "}");
    }

    private void processProductIntroDataChangeMessage(JSONObject messageJSONObject) {
        Long id = messageJSONObject.getLong("id");
        Long productId = messageJSONObject.getLong("product_id");
        String eventType = messageJSONObject.getString("event_type");

        if("add".equals(eventType) || "update".equals(eventType)) {
            JSONObject dataJSONObject = JSONObject.parseObject(eshopProductService.findProductIntroById(id));
            Jedis jedis = jedisPool.getResource();
            jedis.set("product_intro_" + productId, dataJSONObject.toJSONString());
        } else if ("delete".equals(eventType)) {
            Jedis jedis = jedisPool.getResource();
            jedis.del("product_intro_" + productId);
        }

        dimDataChangeMessageSet.add("{\"dim_type\": \"product_intro\", \"id\": " + productId + "}");

    }

    private void processProductDataChangeMessage(JSONObject messageJSONObject) {
        Long id = messageJSONObject.getLong("id");
        String eventType = messageJSONObject.getString("event_type");

        if("add".equals(eventType) || "update".equals(eventType)) {
            JSONObject dataJSONObject = JSONObject.parseObject(eshopProductService.findProductById(id));
            Jedis jedis = jedisPool.getResource();
            jedis.set("product_" + id, dataJSONObject.toJSONString());
        } else if ("delete".equals(eventType)) {
            Jedis jedis = jedisPool.getResource();
            jedis.del("product_" + id);
        }

       dimDataChangeMessageSet.add("{\"dim_type\": \"product\", \"id\": " + id + "}");
    }

    private void processProductPropertyDataChangeMessage(JSONObject messageJSONObject) {
        Long id = messageJSONObject.getLong("id");
        Long productId = messageJSONObject.getLong("product_id");
        String eventType = messageJSONObject.getString("event_type");

        if("add".equals(eventType) || "update".equals(eventType)) {
            JSONObject dataJSONObject = JSONObject.parseObject(eshopProductService.findProductPropertyById(id));
            Jedis jedis = jedisPool.getResource();
            jedis.set("product_property_" + productId, dataJSONObject.toJSONString());
        } else if ("delete".equals(eventType)) {
            Jedis jedis = jedisPool.getResource();
            jedis.del("product_property_" + productId);
        }

        dimDataChangeMessageSet.add("{\"dim_type\": \"product_property\", \"id\": " + productId + "}");
    }

    private void processProductSpecificationDataChangeMessage(JSONObject messageJSONObject) {
        Long id = messageJSONObject.getLong("id");
        Long productId = messageJSONObject.getLong("product_id");
        String eventType = messageJSONObject.getString("event_type");

        if("add".equals(eventType) || "update".equals(eventType)) {
            JSONObject dataJSONObject = JSONObject.parseObject(eshopProductService.findProductSpecificationById(id));
            Jedis jedis = jedisPool.getResource();
            jedis.set("product_specification_" + productId, dataJSONObject.toJSONString());
        } else if ("delete".equals(eventType)) {
            Jedis jedis = jedisPool.getResource();
            jedis.del("product_specification_" + productId);
        }

      dimDataChangeMessageSet.add("{\"dim_type\": \"product_specification\", \"id\": " + productId + "}");
    }
    private class sendThread extends Thread{
        public void run(){
            while(true){
                if (!dimDataChangeMessageSet.isEmpty()){
                    for (String message:dimDataChangeMessageSet){
                        rabbitMQSender.send("high-priority-aggr-data-chang-queue",message);
                        System.out.println("【将去重消息发送到下一个queue】+"+message);
                    }
                    dimDataChangeMessageSet.clear();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}  