package org.bcos.proxy.tool;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bcos.channel.client.Service;
import org.bcos.proxy.config.Config;
import org.bcos.proxy.contract.Node;
import org.bcos.proxy.contract.RouteManager;
import org.bcos.proxy.contract.Set;
import org.bcos.proxy.protocol.UserInfo;
import org.bcos.proxy.server.HttpServer;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
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
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
     * @desc 读取json文件,格式如下
     * @param fileName
     * @return JSONObject[{"set_name":"", "set_warn_num":8,"set_max_num":10,"set_node_list":[{"ip":"","p2p_port":12,"rpc_port":34,"node_id":"","type":1}]}]
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
     * @desc 部署合约
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
     * @desc 主意是要查用户信息
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
        return userInfo;

    }


    public static String doPost(String url, String jsonStr) throws Exception {
        String result = null;
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        StringEntity postingString = new StringEntity(jsonStr);
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");
        HttpResponse response = httpClient.execute(post);
        try {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new Exception("http code is not 200");
            } else {
                result = new String(EntityUtils.toString(response.getEntity()));
            }
        } catch (Exception e) {
            throw new Exception("deal respose exception.",e);
        }

        return result;
    }


    public static void main(String[] args) throws Exception {

        if (args == null || args.length < 2) {
            System.out.println("usage:[deploy route.json");
            System.out.println("      [queryUserInfo $uid");
            //System.out.println("      [demo $requestStr]");
            System.exit(0);
        }

        if ("deploy".equals(args[0])) {
            JSONArray jsonArray = readJSONFile(args[1]);
            deployContract(jsonArray);
        } else if("queryUserInfo".equals(args[0])) {
            UserInfo userInfo = queryUserInfo(args[1]);
            System.out.printf("uid:%s, queryUserInfo get availAssets:%d unAvailAssets: %d, identity:%d, name:%s\n", userInfo.getUid(),
                    userInfo.getAvailAssets(), userInfo.getUnAvailAssets(), userInfo.getIdentity(), userInfo.getName());
        } else if ("demo".equals(args[0])){
            //demoForQueryMerchantAssets(args[1]);
        } else {
            System.out.println("not support method");
        }

        Thread.sleep(3 * 1000);
        System.exit(0);

        /*
        String jsonFile = args[0];
        String str = "{\"contract\":\"transactionTest\",\"func\":\"add\",\"version\":\"\",\"params\":[1]}";
        Random r = new Random();
        BigInteger randomid = new BigInteger(250, r);
        BigInteger blockLimit = web3j.getBlockNumberCache();
        RawTransaction rawTransaction = RawTransaction.createTransaction(randomid, gasPrice, gasLimit, blockLimit, "", Numeric.toHexString(str.getBytes()));
        String signMsg = Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials));
        Request request =  web3j.ethSendRawTransaction(signMsg);
        Response response = request.send();*/

    }
}
