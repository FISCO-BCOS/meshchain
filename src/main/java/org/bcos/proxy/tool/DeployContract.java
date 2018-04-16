package org.bcos.proxy.tool;

import org.bcos.channel.client.Service;
import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.channel.dto.EthereumResponse;
import org.bcos.proxy.config.Config;
import org.bcos.proxy.contract.Node;
import org.bcos.proxy.contract.RouteManager;
import org.bcos.proxy.contract.Set;
import org.bcos.proxy.protocol.UserInfo;
import org.bcos.proxy.server.HttpServer;
import org.bcos.proxy.util.ToolUtil;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.Bool;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.ObjectMapperFactory;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.bcos.web3j.protocol.core.DefaultBlockParameterName;
import org.bcos.web3j.protocol.core.methods.request.Transaction;
import org.bcos.web3j.protocol.core.methods.response.EthCall;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.utils.Numeric;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by fisco-dev on 17/8/25.
 */
public class DeployContract {
    public static java.math.BigInteger gasPrice = new BigInteger("1000000");
    public static java.math.BigInteger gasLimit = new BigInteger("1000000");
    public static java.math.BigInteger initialWeiValue = new BigInteger("0");
    public static HttpServer.WCS routeWCS;

    public static String contractVersion = "";

    public static ConcurrentHashMap<String, HttpServer.WCS> nameSetServiceMap = new ConcurrentHashMap();

    static {

        try {
            Config config = Config.getConfig();
            ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

            //先初始化route的
            Credentials credentialsRoute = Credentials.create(config.getPrivateKey());
            Service serviceRoute = (Service) context.getBean(HttpServer.ROUTE_SERVICE_NAME);
            ChannelEthereumService channelEthereumServiceRoute = new ChannelEthereumService();
            channelEthereumServiceRoute.setChannelService(serviceRoute);
            serviceRoute.run();

            Web3j web3jRoute = Web3j.build(channelEthereumServiceRoute);
            routeWCS = new HttpServer.WCS(web3jRoute, credentialsRoute, serviceRoute, HttpServer.ROUTE_SERVICE_NAME);

            //init for all set service
            for (String setName : context.getBeanNamesForType(Service.class)) {
                Service serviceSet = (Service) context.getBean(setName);
                ChannelEthereumService setChannelEthereumService = new ChannelEthereumService();
                setChannelEthereumService.setChannelService(serviceSet);
                serviceSet.run();
                Web3j web3jSet = Web3j.build(setChannelEthereumService);
                HttpServer.WCS wcs = new HttpServer.WCS(web3jSet, Credentials.create(config.getPrivateKey()), serviceSet, setName);
                nameSetServiceMap.put(setName, wcs);
            }

            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @desc read json file,format like:[{"set_name":"", "set_warn_num":8,"set_max_num":10,"set_node_list":[{"ip":"","p2p_port":12,"rpc_port":34,"node_id":"","type":1}]}]
     * @param fileName
     * @return JSONObject
     * @throws IOException
     */
    public static JSONArray readJSONFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        while((tmp = reader.readLine()) != null) {
            sb.append(tmp);
        }

        reader.close();
        JSONArray jsonArray = JSON.parseArray(sb.toString());
        return jsonArray;
    }

    /**
     * @desc deploy route contract
     * @param jsonArray
     * @throws Exception
     */
    public static void deployContract(JSONArray jsonArray) throws Exception {
        if (jsonArray == null) {
            throw new Exception("addSetToRoute jsonArray is null");
        }

        Future<RouteManager> routeManagerFuture = RouteManager.deploy(routeWCS.getWeb3j(), routeWCS.getCredentials(), gasPrice, gasLimit, initialWeiValue, new Uint256(0), new Utf8String(""));
        RouteManager routeManager = routeManagerFuture.get();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String setName = jsonObject.getString("set_name");
            int setWarnNum = jsonObject.getInteger("set_warn_num");
            int setMaxNum = jsonObject.getInteger("set_max_num");
            JSONArray nodeList = jsonObject.getJSONArray("set_node_list");
            Future<Set> setFuture = Set.deploy(routeWCS.getWeb3j(), routeWCS.getCredentials(), gasPrice, gasLimit, initialWeiValue, new Uint256(setMaxNum), new Uint256(setWarnNum));
            Set set = setFuture.get();
            for(int j = 0; j < nodeList.size(); j++) {
                JSONObject nodeJson = nodeList.getJSONObject(j);
                Utf8String nodeId = new Utf8String(nodeJson.getString("node_id"));
                Utf8String ip = new Utf8String(nodeJson.getString("ip"));
                Uint256 p2p = new Uint256(nodeJson.getIntValue("p2p_port"));
                Uint256 rpc = new Uint256(nodeJson.getIntValue("rpc_port"));
                Uint256 type = new Uint256(nodeJson.getInteger("type"));
                Utf8String desc = new Utf8String("");
                Utf8String caHash = new Utf8String("");
                Utf8String agencyInfo = new Utf8String("");
                Future<Node> nodeFuture = Node.deploy(routeWCS.getWeb3j(), routeWCS.getCredentials(), gasPrice, gasLimit, initialWeiValue, nodeId, ip, p2p, rpc, type, desc, caHash, agencyInfo);
                Node node = nodeFuture.get();
                String address = node.getContractAddress();
                Future<TransactionReceipt> receiptFuture = set.addNode(new Address(address));
                TransactionReceipt receipt = receiptFuture.get();
                if (receipt == null || receipt.getTransactionHash() == null) {
                    System.out.println("get receipt is null or transaction hash is null.exec again");
                    return;
                }
            }

            Future<TransactionReceipt> transactionReceiptFuture = routeManager.registerSet(new Address(set.getContractAddress()), new Utf8String(setName));
            TransactionReceipt transactionReceipt = transactionReceiptFuture.get();
            if (transactionReceipt == null || transactionReceipt.getTransactionHash() == null) {
                System.out.println("get receipt is null or transaction hash is null.exec again");
                return;
            }

        }

        System.out.println("register route contract success.address:" + routeManager.getContractAddress());
    }


    /**
     * @desc get user info
     * @param uid
     */
    public static UserInfo queryUserInfo(String uid) throws Exception {

        byte[] uidBytes = Arrays.copyOf(uid.getBytes(), 32);
        Bytes32 uidByte32 = new Bytes32(uidBytes);

        RouteManager routeManager = RouteManager.load(Config.getConfig().getRouteAddress(), routeWCS.getWeb3j(), routeWCS.getCredentials(), gasPrice, gasLimit);

        List<Type> typeList = routeManager.getRoute(uidByte32).get();
        if (typeList.size() != 2) {
            return null;
        }

        Boolean existed = (Boolean)typeList.get(0).getValue();
        BigInteger setId = (BigInteger)typeList.get(1).getValue();

        if (!existed) {
            return null;
        }

        Future<Utf8String> addressFuture = routeManager.m_setNames(new Uint256(setId));
        String setName = addressFuture.get().toString();


        JSONObject jsonObject = JSON.parseObject("{}");
        jsonObject.put("contract", "Meshchain");
        jsonObject.put("func", "getUserInfo");
        jsonObject.put("version", "");

        List<Object> params = new ArrayList<>();
        params.add(uid);
        jsonObject.put("params", params);

        Web3j web3j = nameSetServiceMap.get(setName).getWeb3j();

        if (web3j == null) {
            throw new Exception("bad chain name");
        }

        String data = Numeric.toHexString(jsonObject.toJSONString().getBytes());

        EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(null, null, data), DefaultBlockParameterName.LATEST).sendAsync().get();
        String value = ethCall.getResult();
        JSONArray array = JSON.parseArray(value);
        if (array.size() == 0) {
            throw new Exception("array size = 0");
        }

        int availAssets = array.getInteger(0);
        int unAvailAssets = array.getInteger(1);
        int identity = array.getInteger(2);
        String name = array.getString(3);

        UserInfo userInfo = new UserInfo(uid, availAssets, unAvailAssets, identity, name);
        System.out.printf("uid:%s,set id:%d\n", uid, setId.intValue());
        return userInfo;

    }

    public static void getSetUsers(int setId) throws Exception {
        if (setId < 0) {
            System.out.println("set id error: set id:" + setId);
            return;
        }

        RouteManager routeManager = RouteManager.load(Config.getConfig().getRouteAddress(), routeWCS.getWeb3j(), routeWCS.getCredentials(), gasPrice, gasLimit);
        List<Type> typeList = routeManager.getSetAddress(new Uint256(setId)).get();
        if (typeList.size() != 2) {
            System.out.println("get setId:" + setId + " failed");
            return;
        }

        Bool ok = (Bool)typeList.get(0);
        Address setAddress = (Address)typeList.get(1);

        if (!ok.getValue()) {
            System.out.println("setId:" + setId + " not existed");
            return;
        }

        Set set = Set.load(setAddress.toString(), routeWCS.getWeb3j(), routeWCS.getCredentials(), gasPrice, gasLimit);
        Uint256 userNum = set.getUsersNum().get();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < userNum.getValue().intValue(); i++) {
            Bytes32 user = set.m_users(new Uint256(i)).get();
            sb.append(ToolUtil.byte32ToString(user.getValue())).append(",");
        }

        String users = sb.toString();
        if (users.length() > 0) {
            users = users.substring(0, users.length() - 1);
        }

        System.out.println("set id :" + setId + ", users:[" + users + "]");
    }

    /**
     * @desc get set capacity
     * @param setId
     * @throws Exception
     */
    public static void getSetCapacity(int setId) throws Exception {
        if (setId < 0) {
            System.out.println("set id error: set id:" + setId);
            return;
        }

        RouteManager routeManager = RouteManager.load(Config.getConfig().getRouteAddress(), routeWCS.getWeb3j(), routeWCS.getCredentials(), gasPrice, gasLimit);
        List<Type> typeList = routeManager.getSetAddress(new Uint256(setId)).get();
        if (typeList.size() != 2) {
            System.out.println("get setId:" + setId + " failed");
            return;
        }

        Bool ok = (Bool)typeList.get(0);
        Address setAddress = (Address)typeList.get(1);

        if (!ok.getValue()) {
            System.out.println("setId:" + setId + " not existed");
            return;
        }

        Set set = Set.load(setAddress.toString(), routeWCS.getWeb3j(), routeWCS.getCredentials(), gasPrice, gasLimit);
        List<Type> retListType = set.getSetCapacity().get();

        if (retListType.size() != 2) {
            System.out.println("get set capacity failed");
            return;
        }

        Uint256 warn = (Uint256)retListType.get(0);
        Uint256 max = (Uint256)retListType.get(1);

        System.out.println("warn num:" + warn.getValue().intValue() + ", max num:" + max.getValue().intValue());
    }

    /**
     * @desc  expand set capacity
     * @param setId
     * @param warn
     * @param max
     * @throws Exception
     */
    public static void expandSet(int setId, int warn, int max) throws Exception {
        if (setId < 0) {
            System.out.println("set id error: set id:" + setId);
            return;
        }

        RouteManager routeManager = RouteManager.load(Config.getConfig().getRouteAddress(), routeWCS.getWeb3j(), routeWCS.getCredentials(), gasPrice, gasLimit);
        List<Type> typeList = routeManager.getSetAddress(new Uint256(setId)).get();
        if (typeList.size() != 2) {
            System.out.println("get setId:" + setId + " failed");
            return;
        }

        Bool ok = (Bool)typeList.get(0);
        Address setAddress = (Address)typeList.get(1);

        if (!ok.getValue()) {
            System.out.println("setId:" + setId + " not existed");
            return;
        }


        Set set = Set.load(setAddress.toString(), routeWCS.getWeb3j(), routeWCS.getCredentials(), gasPrice, gasLimit);
        set.expandSet(new Uint256(max), new Uint256(warn), new TransactionSucCallback() {
            @Override
            public void onResponse(EthereumResponse ethereumResponse) {
                try {
                    TransactionReceipt transactionReceipt = ObjectMapperFactory.getObjectMapper().readValue(ethereumResponse.getContent(), TransactionReceipt.class);
                    List<Set.RetEventResponse> retEventResponseList = Set.getRetEvents(transactionReceipt);
                    if (retEventResponseList.size() != 1) {
                        System.out.println("get expandSet event log failed.");
                        return;
                    }

                    Set.RetEventResponse retEventResponse = retEventResponseList.get(0);
                    if (retEventResponse.code.getValue().intValue() == 0) {
                        System.out.println("expandSet success.");
                    } else {
                        System.out.println("expandSet failed.code:" + retEventResponse.code.getValue().intValue());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static void main(String[] args) throws Exception {

        if (args.length < 1 || args[0] == "-h") {
            System.out.println("usage:[deploy route.json");
            System.out.println("      [queryUserInfo $uid");
            System.out.println("      [expandSet $setid $warnNum $maxNum");
            System.out.println("      [getSetCapacity $setid");
            System.exit(0);
        }

        switch (args[0]) {
            case "deploy":
                if (args.length < 2) {
                    System.out.println("miss the route.json file");
                    return;
                }

                JSONArray jsonArray = readJSONFile(args[1]);
                deployContract(jsonArray);
                break;
            case "queryUserInfo":
                if (args.length < 2) {
                    System.out.println("miss the uid");
                    return;
                }
                UserInfo userInfo = queryUserInfo(args[1]);
                if (userInfo == null) {
                    System.out.println("uid:" + args[1] + " not exist");
                    return;
                }

                System.out.printf("uid:%s, queryUserInfo get availAssets:%d unAvailAssets: %d, identity:%d, name:%s\n", userInfo.getUid(),
                        userInfo.getAvailAssets(), userInfo.getUnAvailAssets(), userInfo.getIdentity(), userInfo.getName());
                break;
            case "expandSet":
                if (args.length < 4) {
                    System.out.println("miss the args:$setid $warnNum $maxNum");
                    return;
                }

                expandSet(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                break;
            case "getSetCapacity":
                if (args.length < 2) {
                    System.out.println("miss the $setid");
                    return;
                }

                getSetCapacity(Integer.parseInt(args[1]));
                break;
            case "getSetUsers":
                if (args.length < 2) {
                    System.out.println("miss the $setid");
                    return;
                }

                getSetUsers(Integer.parseInt(args[1]));
                break;
            default:
                System.out.println("not support command");
                return;
        }

        Thread.sleep(3 * 1000);
        System.exit(0);
    }
}
