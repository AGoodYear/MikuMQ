//package com.ivmiku.mikumq.manager;
//
//import com.alibaba.nacos.api.NacosFactory;
//import com.alibaba.nacos.api.config.ConfigService;
//import com.alibaba.nacos.api.config.listener.Listener;
//import com.alibaba.nacos.api.exception.NacosException;
//import com.alibaba.nacos.api.naming.NamingService;
//
//import java.beans.PropertyEditor;
//import java.beans.PropertyEditorSupport;
//import java.util.HashMap;
//import java.util.Properties;
//import java.util.concurrent.Executor;
//
//public class ClusterManager {
//    private HashMap<String, String> params;
//    private Properties properties;
//
//    public void init(HashMap<String, String> params) {
//        this.params = params;
//    }
//
//    public void start() {
//        String address = params.get("address");
//        ConfigService configService;
//        properties = new Properties();
//        try {
//            configService = NacosFactory.createConfigService(address);
//            NamingService namingService = NacosFactory.createNamingService(address);
//            String config = configService.getConfigAndSignListener("com.ivmiku.mikumq.broker", "cluster", 3000, new Listener() {
//                @Override
//                public Executor getExecutor() {
//                    return null;
//                }
//
//                @Override
//                public void receiveConfigInfo(String s) {
//                    properties.load(s);
//                    PropertyEditor editor = new PropertyEditorSupport();
//                    editor.setAsText(s);
//                }
//            });
//        } catch (NacosException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//}
