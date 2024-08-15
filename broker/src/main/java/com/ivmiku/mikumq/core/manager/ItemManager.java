package com.ivmiku.mikumq.core.manager;

import com.ivmiku.mikumq.core.*;
import com.ivmiku.mikumq.dao.BindingDao;
import com.ivmiku.mikumq.dao.ExchangeDao;
import com.ivmiku.mikumq.dao.MessageDao;
import com.ivmiku.mikumq.dao.QueueDao;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.utils.BalancerUtil;
import com.ivmiku.mikumq.utils.DurableUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理中心
 * @author Aurora
 */
public class ItemManager {
    private final ConcurrentHashMap<String, Exchange> exchangeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MessageQueue> queueMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LinkedList<String>> messageList = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Binding>> bindingMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Message> messageMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<MessageRecorder>> consumerMsgMap = new ConcurrentHashMap<>();
    /**
     * 等待消费者发送确认消费信息
     */
    private final ConcurrentHashMap<String, List<String>> waitQueueMap = new ConcurrentHashMap<>();
    /**
     * 死信队列
     */
    private final ConcurrentHashMap<String, List<String>> deadQueueMap = new ConcurrentHashMap<>();

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
        Message message = messageMap.remove(id);
        if (message.isDurable()) {
            DurableMessage durableMessage = MessageDao.selectMessageById(message.getId());
            if (durableMessage != null) {
                DurableUtil.invalidateMessage(Integer.parseInt(durableMessage.getStart()));
                MessageDao.deleteMessage(message.getId());
            }
        }
    }

    public void deleteFromQueue(String queueName, String id) {
        messageList.get(queueName).remove(id);
    }

    public void sendMessage(String queueName, Message message) {
        List<String> list = messageList.computeIfAbsent(queueName, k -> new LinkedList<>());
        MessageQueue queue = queueMap.get(queueName);
        list.add(message.getId());
        if (!queue.getListener().isEmpty()) {
            Integer robin = BalancerUtil.getRobin(queueName);
            if (robin >= queue.getListener().size()) {
                robin %= queue.getListener().size();
                BalancerUtil.resetQueue(queueName);
            }
            String consumerTag = queue.getListener().get(robin);
            List<MessageRecorder> consumerMsg = consumerMsgMap.computeIfAbsent(consumerTag, k-> new LinkedList<>());
            MessageRecorder recorder = new MessageRecorder(message.getId(), queueName);
            consumerMsg.add(recorder);
        } else {
            queue.setNeedInspect(true);
        }
        if (queue.isNeedInspect() && !queue.getListener().isEmpty()) {
            List<String> deliveredMessage = new ArrayList<>();
            for (String tag : queue.getListener()) {
                List<MessageRecorder> consumerMsg = consumerMsgMap.get(tag);
                for (MessageRecorder recorder : consumerMsg) {
                    deliveredMessage.add(recorder.getMessageId());
                }
            }
            for (String messageId : list) {
                if (!deliveredMessage.contains(messageId)) {
                    Integer robin = BalancerUtil.getRobin(queueName);
                    if (robin >= queue.getListener().size()) {
                        robin %= queue.getListener().size();
                        BalancerUtil.resetQueue(queueName);
                    }
                    String consumerTag = queue.getListener().get(robin);
                    List<MessageRecorder> consumerMsg = consumerMsgMap.computeIfAbsent(consumerTag, k-> new LinkedList<>());
                    MessageRecorder recorder = new MessageRecorder(message.getId(), queueName);
                    consumerMsg.add(recorder);
                }
            }
            queue.setNeedInspect(false);
        }
    }

    public List<String> getMessageList(String queueName) {
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
        List<String> waitingList = waitQueueMap.get(queueName);
        if (waitingList == null) {
            waitingList = new ArrayList<>();
        }
       waitingList.add(id);
        synchronized (waitQueueMap) {
            waitQueueMap.put(queueName, waitingList);
        }
        messageList.get(queueName).remove(id);
    }

    public void ackMessage(String id, String queueName, String consumerTag) {
        waitQueueMap.get(queueName).remove(id);
    }

    public void enterDeadQueue(String messageId, String queueName, String consumerTag) {
        waitQueueMap.get(queueName).remove(messageId);
        List<String> deadList = deadQueueMap.get(queueName);
        if (deadList == null) {
            deadList = new ArrayList<>();
        }
        deadList.add(messageId);
        synchronized (deadQueueMap) {
            deadQueueMap.put(queueName, deadList);
        }
    }

    public void insertUnreadMessage(String messageId, String consumerTag, String queueName) {
        List<MessageRecorder> messageList = consumerMsgMap.computeIfAbsent(consumerTag, k -> new LinkedList<>());
        messageList.add(new MessageRecorder(messageId, queueName));
    }

    public List<MessageRecorder> getUnreadMessage(String consumerTag) {
        return consumerMsgMap.get(consumerTag);
    }

    public List<String> getQueueList() {
        return Collections.list(queueMap.keys());
    }

    public List<String> getExchangeList() {
        return Collections.list(exchangeMap.keys());
    }

    public Message declareDeadQueue(String queueName) {
        String messageId = deadQueueMap.get(queueName).removeFirst();
        return messageMap.get(messageId);
    }

    public boolean ifInQueue(String queueName, String messageId) {
        return messageList.get(queueName).contains(messageId);
    }

    public boolean ifWaitingAck(String queueName, String messageId) {
        return waitQueueMap.get(queueName).contains(messageId);
    }

    public boolean ifDead(String queueName, String messageId) {
        return deadQueueMap.get(queueName).contains(messageId);
    }

    public void removeFromQueue(String queueName, String messageId) {
        messageList.get(queueName).remove(messageId);
    }

    public void removeFromAck(String queueName, String messageId) {
        waitQueueMap.get(queueName).remove(messageId);
    }

    public void removeFromDead(String queueName, String messageId) {
        deadQueueMap.get(queueName).remove(messageId);
    }

    public void removeFromConsumer(String consumerTag, String messageId) {
        Iterator<MessageRecorder> it = consumerMsgMap.get(consumerTag).iterator();
        while (it.hasNext()) {
            MessageRecorder recorder = it.next();
            if (recorder.getMessageId().equals(messageId)) {
                it.remove();
                break;
            }
        }
    }

    public boolean ifHaveMessage(String consumerTag) {
        return consumerMsgMap.get(consumerTag) == null || consumerMsgMap.get(consumerTag).isEmpty();
    }
}
