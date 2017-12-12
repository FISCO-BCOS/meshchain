package org.bcos.proxy.config;

import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by fisco-dev on 17/8/27.
 */
public @Data class Config {

    private String privateKey;//发送交易的私钥
    private String serviceId;//RMB的serviceid
    private String scenario;//RMB的scenario
    private String routeAddress;//route的合约地址
    private List<String> setNameList;//所有set的名字，这个主要是针对applicationContext.xml里面所有orgId完成初始化
    private String hotChainName;//热点链的名字，对应applicationContext.xml里面orgId=hotChainName完成初始化
    private String routeChainName;//路由链的名字，对应applicationContext.xml里面orgId=routeChainName完成初始化
    public static int timeTaskIntervalSecond = 60;
    public static int enableTimeTask = 0;

    private static Config instance;

    private Config(String privateKey, String serviceId, String scenario, String routeAddress, List<String> setNameList, String hotChainName, String routeChainName){
        this.privateKey = privateKey;
        this.serviceId = serviceId;
        this.scenario = scenario;
        this.routeAddress = routeAddress;
        this.setNameList = setNameList;
        this.hotChainName = hotChainName;
        this.routeChainName = routeChainName;
    }


    public synchronized static Config getConfig() throws Exception {
        if (instance == null) {
            instance = initConfig("config.xml");
        }

        return instance;
    }


    private static Config initConfig(String fileName) throws Exception {

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputStream in = Config.class.getClassLoader().getResourceAsStream(fileName);
        Document doc = docBuilder.parse(in);
        in.close();

        Element configElement = doc.getDocumentElement();
        NodeList privateNodeList = configElement.getElementsByTagName("privateKey");
        if (privateNodeList == null ||privateNodeList.getLength() == 0){
            throw new ParserConfigurationException("privateKey is not defined");
        }

        Node privateNode = privateNodeList.item(0);
        String privateKey = privateNode.getTextContent();

        NodeList serviceIdNodeList = configElement.getElementsByTagName("serviceId");
        if (serviceIdNodeList == null ||serviceIdNodeList.getLength() == 0){
            throw new ParserConfigurationException("serviceId is not defined");
        }

        Node serviceIdNode = serviceIdNodeList.item(0);
        String serviceId = serviceIdNode.getTextContent();


        NodeList scenarioNodeList = configElement.getElementsByTagName("scenario");
        if (scenarioNodeList == null ||scenarioNodeList.getLength() == 0){
            throw new ParserConfigurationException("scenario is not defined");
        }

        Node scenarioNode = scenarioNodeList.item(0);
        String scenario = scenarioNode.getTextContent();


        NodeList routeAddressNodeList = configElement.getElementsByTagName("routeAddress");
        if (routeAddressNodeList == null ||routeAddressNodeList.getLength() == 0){
            throw new ParserConfigurationException("routeAddress is not defined");
        }

        Node routeAddressNode = routeAddressNodeList.item(0);
        String routeAddress = routeAddressNode.getTextContent();


        NodeList setNameNodeList = configElement.getElementsByTagName("setNameList");
        if (setNameNodeList == null ||setNameNodeList.getLength() == 0){
            throw new ParserConfigurationException("setNameList is not defined");
        }

        Node setNameNode = setNameNodeList.item(0);
        String setNameListStr = setNameNode.getTextContent();


        NodeList hotChainNameNodeList = configElement.getElementsByTagName("hotChainName");
        if (hotChainNameNodeList == null ||hotChainNameNodeList.getLength() == 0){
            throw new ParserConfigurationException("hotChainName is not defined");
        }

        Node hotChainNameNode = hotChainNameNodeList.item(0);
        String hotChainName = hotChainNameNode.getTextContent();

        NodeList routeChainNameNodeList = configElement.getElementsByTagName("routeChainName");
        if (routeChainNameNodeList == null ||routeChainNameNodeList.getLength() == 0){
            throw new ParserConfigurationException("routeChainName is not defined");
        }

        Node routeChainNameNode = routeChainNameNodeList.item(0);
        String routeChainName = routeChainNameNode.getTextContent();


        NodeList timeTaskIntervalNodeList = configElement.getElementsByTagName("timeTaskIntervalSecond");
        if (timeTaskIntervalNodeList != null && timeTaskIntervalNodeList.getLength() != 0){
            Node timeTaskIntervalSecondNode = timeTaskIntervalNodeList.item(0);
            String timeTaskIntervalSecondStr = timeTaskIntervalSecondNode.getTextContent();
            try {
                Config.timeTaskIntervalSecond = Integer.parseInt(timeTaskIntervalSecondStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        NodeList enableTimeTaskList = configElement.getElementsByTagName("enableTimeTask");
        if (enableTimeTaskList != null && enableTimeTaskList.getLength() != 0){
            Node enableTaskIntervalNode = enableTimeTaskList.item(0);
            String enableTimeTaskInterval = enableTaskIntervalNode.getTextContent();
            try {
                Config.enableTimeTask = Integer.parseInt(enableTimeTaskInterval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> setNameList = Arrays.asList(setNameListStr.split(","));
        Config config = new Config(privateKey, serviceId, scenario, routeAddress, setNameList, hotChainName, routeChainName);

        return config;
    }
}
