package com.ivmiku.mikumq.manager;

import com.ivmiku.mikumq.core.Binding;
import com.ivmiku.mikumq.core.Exchange;
import com.ivmiku.mikumq.core.MessageQueue;
import com.ivmiku.mikumq.dao.BindingDao;
import com.ivmiku.mikumq.dao.ExchangeDao;
import com.ivmiku.mikumq.dao.QueueDao;
import com.ivmiku.mikumq.entity.Message;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理中心
 * @author Aurora
 */
public class ItemManager {
    private final ConcurrentHashMap<String, Exchange> exchangeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MessageQueue> queueMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LinkedList<Message>> messageList = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Binding>> bindingMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Message> messageMap = new ConcurrentHashMap<>();
    /**
     * 等待消费者发送确认消费信息
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Message>> waitQueueMap = new ConcurrentHashMap<>();
    /**
     * 死信队列
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Message>> deadQueueMap = new ConcurrentHashMap<>();

    public void insertExchange(Exchange exchange) {
        exchangeMap.put(exchange.getName(), exchange);
        if (exchange.isDurable()) {
            ExchangeDao.insertExchange(exchange);
        }
    }

    public Exchange getExchange(String name) {
        return exchangeMap.get(name);
    }

    public void deleteExchange(String name) {
        exchangeMap.remove(name);
        ExchangeDao.deleteExchange(name);
    }

    public void insertQueue(MessageQueue messageQueue) {
        if (!queueMap.containsKey(messageQueue.getName())) {
            queueMap.put(messageQueue.getName(), messageQueue);
            if (messageQueue.isDurable()) {
                QueueDao.insertQueue(messageQueue);
            }
        }
    }

    public MessageQueue getQueue(String name) {
        return queueMap.get(name);
    }

    public void deleteQueue(String name) {
        queueMap.remove(name);
        QueueDao.deleteQueue(name);
    }

    public void insertBinding(Binding binding) {
        ConcurrentHashMap<String, Binding> exchangeBindingMap = bindingMap.computeIfAbsent(binding.getExchangeName(), k -> new ConcurrentHashMap<>());
        Binding binding1;
        synchronized (bindingMap) {
            binding1 = exchangeBindingMap.put(binding.getQueueName(), binding);
        }
        if (binding1 == null) {
            BindingDao.insertBinding(binding);
        }
    }

    public Binding getBinding(String exchangeName, String queueName) {
        return bindingMap.get(exchangeName).get(queueName);
    }

    public ConcurrentHashMap<String, Binding> getBinding(String exchangeName) {
        return bindingMap.get(exchangeName);
    }

    /**
     * 删除绑定（存疑）
     * @param exchangeName 交换机名称
     * @param queueName 队列名称
     */
    public void deleteBinding(String exchangeName, String queueName) {
        String key = bindingMap.get(exchangeName).remove(queueName).getBindingKey();
        BindingDao.deleteExchange(key);
    }

    public void insertMessage(Message message) {
        messageMap.put(message.getId(), message);
    }

    public Message getMessage(String id) {
        return messageMap.get(id);
    }

    public void deleteMessage(String id) {
        messageMap.remove(id);
    }

    public void sendMessage(String queueName, Message message) {
        LinkedList<Message> list = messageList.computeIfAbsent(queueName, k -> new LinkedList<>());
        list.add(message);
    }

    public LinkedList<Message> getMessageList(String queueName) {
        return messageList.get(queueName);
    }

    public void init() {
        List<Exchange> exchangeList = ExchangeDao.getExchange();
        List<MessageQueue> queueList = QueueDao.getQueue();
        List<Binding> bindingList = BindingDao.getBinding();
        for (Exchange exchange : exchangeList) {
            exchangeMap.put(exchange.getName(), exchange);
        }
        for (MessageQueue queue : queueList) {
            queueMap.put(queue.getName(), queue);
        }
        for (Binding binding : bindingList) {
            ConcurrentHashMap<String, Binding> exchangeBindingMap = bindingMap.computeIfAbsent(binding.getExchangeName(), k -> new ConcurrentHashMap<>());
            exchangeBindingMap.put(binding.getQueueName(), binding);
        }
    }

    public void waitingForAck(String id, String queueName) {
        Message message = messageMap.get(id);
        messageList.get(queueName).remove(message);
        ConcurrentHashMap<String, Message> waitingMap = waitQueueMap.get(queueName);
        if (waitingMap == null) {
            waitingMap = new ConcurrentHashMap<>();
        }
        waitingMap.put(id, message);
        synchronized (waitQueueMap) {
            waitQueueMap.put(queueName, waitingMap);
        }
    }

    public void ackMessage(String id, String queueName) {
        waitQueueMap.get(queueName).remove(id);
    }

    public void enterDeadQueue(String messageId, String queueName) {
        Message message = waitQueueMap.get(queueName).remove(messageId);
        messageList.get(queueName).remove(message);
        ConcurrentHashMap<String, Message> deadMap = waitQueueMap.get(queueName);
        if (deadMap == null) {
            deadMap = new ConcurrentHashMap<>();
        }
        deadMap.put(messageId, message);
        synchronized (deadQueueMap) {
            deadQueueMap.put(queueName, deadMap);
        }
    }

    public List<String> getQueueList() {
        return Collections.list(queueMap.keys());
    }

    public List<String> getExchangeList() {
        return Collections.list(exchangeMap.keys());
    }
}
