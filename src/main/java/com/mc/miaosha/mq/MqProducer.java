package com.mc.miaosha.mq;

import com.alibaba.fastjson.JSON;
import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.service.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqProducer {

    @Autowired
    private OrderService orderService;

    private DefaultMQProducer producer;

    private TransactionMQProducer transactionMQProducer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @PostConstruct
    public void init() throws MQClientException {
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object args) {
                //真正创建订单
                Map argMap = (Map) args;
                Integer itemId = (Integer) argMap.get("itemId");
                Integer amount = (Integer) argMap.get("amount");
                Integer userId = (Integer) argMap.get("userId");
                Integer promoId = (Integer) argMap.get("promoId");

                try {
                    orderService.createOrder(userId,itemId,promoId,amount);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                return null;
            }
        });
    }

    //事务型异步扣减库存消息
    public boolean transactionAsyncReduceStock(Integer itemId,Integer amount, Integer userId,Integer promoId) {
        Map<String,Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId",itemId);
        bodyMap.put("amount",amount);

        Map<String,Integer> argsMap = new HashMap<>();
        argsMap.put("itemId",itemId);
        argsMap.put("amount",amount);
        argsMap.put("userId",userId);
        argsMap.put("promoId",promoId);

        Message msg = new Message(topicName,"increase",
                JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));

        try {
            transactionMQProducer.sendMessageInTransaction(msg,argsMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    //同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId,Integer amount) {
        Map<String,Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId",itemId);
        bodyMap.put("amount",amount);

        Message msg = new Message(topicName,"increase",
                JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));

        try {
            producer.send(msg);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
