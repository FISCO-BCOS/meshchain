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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fisco-dev on 17/8/27.
 */
public @Data class Config {

    private String privateKey;//发送交易的私钥
    private String routeAddress;//route的合约地址
    private List<String> hotAccountList;//hot account list
    private String hotChainName;//hot chain

    public static int timeTaskIntervalSecond = 60;
    public static int enableTimeTask = 0;

    private static Config instance;

    private Config(String privateKey, String routeAddress, List<String> hotAccountList, String hotChainName){
        this.privateKey = privateKey;
        this.routeAddress = routeAddress;
        this.hotAccountList = hotAccountList;
        this.hotChainName = hotChainName;
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

        NodeList routeAddressNodeList = configElement.getElementsByTagName("routeAddress");
        if (routeAddressNodeList == null ||routeAddressNodeList.getLength() == 0){
            throw new ParserConfigurationException("routeAddress is not defined");
        }

        Node routeAddressNode = routeAddressNodeList.item(0);
        String routeAddress = routeAddressNode.getTextContent();


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

        NodeList hotAccountNodeList = configElement.getElementsByTagName("hotAccounts");
        List<String> hotAccountList = new ArrayList<>();

        if (hotAccountNodeList != null){
            Node hotAccountNode =  hotAccountNodeList.item(0);
            for(String hotAccount : hotAccountNode.getTextContent().split(",")) {
                hotAccountList.add(hotAccount);
            }
        }

        NodeList hotChainNameNodeList = configElement.getElementsByTagName("hotChainName");
        String hotChainName = null;

        if (hotChainNameNodeList != null){
            Node hotChainNode =  hotChainNameNodeList.item(0);
            hotChainName = hotChainNode.getTextContent();
        }

        Config config = new Config(privateKey, routeAddress, hotAccountList, hotChainName);
        return config;
    }
}
