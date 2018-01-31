package org.bcos.proxy.tool;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bcos.channel.client.Service;
import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.channel.dto.EthereumResponse;
import org.bcos.proxy.config.Config;
import org.bcos.proxy.contract.Node;
import org.bcos.proxy.contract.RouteManager;
import org.bcos.proxy.contract.Set;
import org.bcos.proxy.contract.Meshchain;
import org.bcos.proxy.server.RMBServer;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.Bool;
import org.bcos.web3j.abi.datatypes.DynamicArray;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.crypto.TransactionEncoder;
import org.bcos.web3j.protocol.ObjectMapperFactory;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.bcos.web3j.protocol.core.DefaultBlockParameterName;
import org.bcos.web3j.protocol.core.Request;
import org.bcos.web3j.protocol.core.methods.request.RawTransaction;
import org.bcos.web3j.protocol.core.methods.request.Transaction;
import org.bcos.web3j.protocol.core.methods.response.EthCall;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.utils.Numeric;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by fisco-dev on 17/8/25.
 */
public class DeployContract {
    public static java.math.BigInteger gasPrice = new BigInteger("1000000");
    public static java.math.BigInteger gasLimit = new BigInteger("1000000");
    public static java.math.BigInteger initialWeiValue = new BigInteger("0");
    public final static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    public static Service serviceRoute;
    public static Web3j web3jRoute;
    public static Credentials credentialsHot;
    public static Credentials credentialsRoute;
    public static Service serviceHot;
    public static Web3j web3jHot;

    public static String contractVersion = "";

    public static ConcurrentHashMap<String, RMBServer.WCS> nameSetServiceMap = new ConcurrentHashMap();

    static {

        try {
            Config config = Config.getConfig();
            ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

            //先初始化route的
            credentialsRoute = Credentials.create(config.getPrivateKey());
            serviceRoute = (Service)context.getBean(config.getRouteChainName());
            serviceRoute.setOrgID("WB");
            ChannelEthereumService channelEthereumServiceRoute = new ChannelEthereumService();
            channelEthereumServiceRoute.setChannelService(serviceRoute);
            serviceRoute.run();

            try {
                //让初始化完成
                Thread.sleep(3000);
                web3jRoute = Web3j.build(channelEthereumServiceRoute);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //初始化所有set的
            for (String setName : config.getSetNameList()) {
                Service serviceSet = (Service)context.getBean(setName);
                serviceSet.setOrgID("WB");
                ChannelEthereumService setChannelEthereumService = new ChannelEthereumService();
                setChannelEthereumService.setChannelService(serviceSet);
                try {
                    serviceSet.run();
                    Thread.sleep(3000);
                    Web3j web3jSet = Web3j.build(setChannelEthereumService);
                    RMBServer.WCS wcs = new RMBServer.WCS(web3jSet, Credentials.create(config.getPrivateKey()), serviceSet, setName);//先直接公用私钥
                    nameSetServiceMap.put(setName, wcs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //初始化热点链的
            credentialsHot = Credentials.create(config.getPrivateKey());
            serviceHot = (Service)context.getBean(config.getHotChainName());
            serviceHot.setOrgID("WB");
            ChannelEthereumService channelEthereumServiceHot = new ChannelEthereumService();
            channelEthereumServiceHot.setChannelService(serviceHot);
            serviceHot.run();


            try {
                //让初始化完成
                Thread.sleep(3000);
                web3jHot = Web3j.build(channelEthereumServiceHot);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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

        Future<RouteManager> routeManagerFuture = RouteManager.deploy(web3jRoute, credentialsRoute, gasPrice, gasLimit, initialWeiValue, new Uint256(0), new Utf8String(""));
        RouteManager routeManager = routeManagerFuture.get();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String setName = jsonObject.getString("set_name");
            int setWarnNum = jsonObject.getInteger("set_warn_num");
            int setMaxNum = jsonObject.getInteger("set_max_num");
            JSONArray nodeList = jsonObject.getJSONArray("set_node_list");
            Future<Set> setFuture = Set.deploy(web3jRoute, credentialsRoute, gasPrice, gasLimit, initialWeiValue, new Uint256(setMaxNum), new Uint256(setWarnNum));
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
                Future<Node> nodeFuture = Node.deploy(web3jRoute, credentialsRoute, gasPrice, gasLimit, initialWeiValue, nodeId, ip, p2p, rpc, type, desc, caHash, agencyInfo);
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
     * @desc 主意是要查询所有的商户id
     * @param jsonStr {"contract":"Meshchain","func":"getAllMerchantIds","version":"","params":[]}
     */
    public static void queryMerchantId(String chainName, String jsonStr) throws Exception {
        //String str = "{\"contract\":\"transactionTest\",\"func\":\"add\",\"version\":\"\",\"params\":[1]}";

        Web3j web3j = null;

        if (chainName.equals("hotService")) {
            web3j = web3jHot;
        } else if (chainName.equals("routeService")) {
            web3j = web3jRoute;
        } else {
            web3j = nameSetServiceMap.get(chainName).getWeb3j();
        }

        if (web3j == null) {
            throw new Exception("bad chain name");
        }

        String data = Numeric.toHexString(jsonStr.getBytes());

        EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(null, null, data), DefaultBlockParameterName.LATEST).sendAsync().get();
        String value = ethCall.getResult();
        JSONArray array = JSON.parseArray(value);
        if (array.size() == 0) {
            throw new Exception("array size is 0");
        }

        JSONArray resultJSON = array.getJSONArray(0);
        if (resultJSON.size() == 0) {
            System.out.println("not found any merchant");
            return;
        }

        for (Object obj : resultJSON) {
            Bytes32 merchantId = new Bytes32(obj.toString().getBytes());
            System.out.println("queryMerchantId get id:" + new String(merchantId.getValue()));
        }
    }

    /**
     * @desc 主意是要查资产
     * @param jsonStr {"contract":"Meshchain","func":"getMerchantAssets","version":"","params":[]} //查询商户的 {"contract":"Meshchain","func":"getUserAssets","version":"","params":[]} //查询用户的
     */
    public static List<Integer> queryAssets(String chainName, String jsonStr) throws Exception {
        //String str = "{\"contract\":\"transactionTest\",\"func\":\"add\",\"version\":\"\",\"params\":[1]}";

        Web3j web3j = null;

        if (chainName.equals("hotService")) {
            web3j = web3jHot;
        } else if (chainName.equals("routeService")) {
            web3j = web3jRoute;
        } else {
            web3j = nameSetServiceMap.get(chainName).getWeb3j();
        }

        if (web3j == null) {
            throw new Exception("bad chain name");
        }

        String data = Numeric.toHexString(jsonStr.getBytes());

        EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(null, null, data), DefaultBlockParameterName.LATEST).sendAsync().get();
        String value = ethCall.getResult();
        JSONArray array = JSON.parseArray(value);
        if (array.size() == 0) {
            throw new Exception("array size = 0");
        }

        int assets = array.getInteger(0);

        List<Integer> ret = new ArrayList<>();
        ret.add(assets);
        return ret;

    }

    public static void demoForQueryMerchantAssets(String merchantId) throws Exception {

        boolean existed = checkMerchantExisted(merchantId);
        if (!existed) {
            registerMerchant(merchantId, merchantId);
            Thread.sleep(2000);
        }

        String queryMerchantStr = String.format("{\"contract\":\"Meshchain\",\"func\":\"getMerchantAssets\",\"version\":\"\",\"params\":[\"%s\"]}", merchantId);
        String queryUserStr = "{\"contract\":\"Meshchain\",\"func\":\"getUserAssets\",\"version\":\"\",\"params\":[\"user_%d\"]}";

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        System.out.println("开始用户充值操作...");
                        for(int i = 0; i < 4; i++) {
                            String deposit = String.format("{\"method\":\"userDeposit\",\"uid\":\"user_%d\",\"version\":\"\",\"contractName\":\"Meshchain\",\"params\":[\"1000\"]}", i);
                            String res = doPost("http://127.0.0.1:8081", deposit);
                            JSONObject jsonObject = JSON.parseObject(res);
                            if (jsonObject.getString("code").equals("0")) {
                                System.out.printf("用户:user_%s 充值成功,金额:1000...\n", i);
                            } else {
                                System.out.printf("用户:user_%s 充值失败...result:%s\n", i, res);
                            }

                        }

                        Thread.sleep(1000);
                        System.out.println("开始用户给热点账户子账户转账操作...");

                        for(int i = 0; i < 4; i++) {
                            String consume = String.format("{\"method\":\"consume\",\"uid\":\"user_%d\",\"version\":\"\",\"contractName\":\"Meshchain\",\"params\":[\"%s\",1000]}", i, merchantId);
                            String res = doPost("http://127.0.0.1:8081", consume);
                            JSONObject jsonObject = JSON.parseObject(res);
                            if (jsonObject.getString("code").equals("0")) {
                                System.out.printf("用户:user_%d 给热点账户子账户:%s 转账成功, 金额:1000...\n", i, merchantId);
                            } else {
                                System.out.printf("用户:user_%d 给热点账户子账户:%s 转账失败...result:%s\n", i, merchantId, res);
                            }

                        }

                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        thread.start();
        while (true) {

            List<String> sortChainName = new ArrayList<>();
            for(String chainName : nameSetServiceMap.keySet()) {
                sortChainName.add(chainName);
            }

            Collections.sort(sortChainName, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });


            String format = "%-20s\t%-20s\t%-20s\t%-20s";
            String format1 = "%-20s\t%-28s\t%-28s\t%-20s";
            StringBuilder str = new StringBuilder(String.format(format, "链名字", "账户名字", "账户类别", "账户资产"));

            int i = 0;
            for(String set : sortChainName) {
                for(int j = 0; j < 2; j++, i++) {
                    String queryUserTmp = String.format(queryUserStr, i);
                    List<Integer> ret = queryAssets(set, queryUserTmp);
                    if (ret != null && ret.size() > 0) {
                        str.append("\n" + String.format(format1, String.format("用户链_%s", set), String.format("user_%d", i), "普通用户", ret.get(0).toString()));
                    }
                }

                List<Integer> hotRet = queryAssets(set, queryMerchantStr);
                if (hotRet != null && hotRet.size() > 0) {
                    str.append("\n" + String.format(format1, String.format("用户链_%s", set), merchantId, "热点子账户", hotRet.get(0).toString()));
                }

                System.out.println("\n");
            }

            List<Integer> hotRet = queryAssets("hotService", queryMerchantStr);
            if (hotRet != null && hotRet.size() > 0) {
                str.append("\n" + String.format(format1, "热点链", merchantId, "热点账户", hotRet.get(0).toString()));
            }

            System.out.println(str + "\n");
            Thread.sleep(2000);
        }


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

    public static boolean checkMerchantExisted(String merchantId) throws Exception {
        Web3j web3j = web3jHot;

        String str = String.format("{\"contract\":\"Meshchain\",\"func\":\"checkMerchantExisted\",\"version\":\"\",\"params\":[\"%s\"]}", merchantId);

        String data = Numeric.toHexString(str.getBytes());

        EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(null, null, data), DefaultBlockParameterName.LATEST).sendAsync().get();
        String value = ethCall.getResult();
        JSONArray array = JSON.parseArray(value);
        if (array.size() == 0) {
            throw new Exception("array size = 0");
        }

        return array.getBoolean(0);
    }
    /**
     *
     * @param merchantId 商户id
     * @param merchantName 商户名字
     */

    public static void registerMerchant(String merchantId, String merchantName) throws IOException, InterruptedException {

        JSONObject jsonObject = JSON.parseObject("{}");
        jsonObject.put("contract", "Meshchain");
        jsonObject.put("func", "registerMerchant");
        jsonObject.put("version", contractVersion);
        List<Object> paramsList = new ArrayList<>();
        paramsList.add(merchantId);
        paramsList.add(merchantName);
        paramsList.add(0);
        jsonObject.put("params", paramsList);

        Random r = new Random();
        BigInteger randomid = new BigInteger(250, r);
        BigInteger blockLimit = web3jHot.getBlockNumberCache();
        RawTransaction rawTransaction = RawTransaction.createTransaction(randomid, RMBServer.gasPrice, RMBServer.gasLimit, blockLimit, "", Numeric.toHexString(jsonObject.toJSONString().getBytes()));
        String signMsg = Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentialsHot));
        Request request =  web3jHot.ethSendRawTransaction(signMsg);
        request.setNeedTransCallback(true);
        request.setTransactionSucCallback(new TransactionSucCallback() {
            @Override
            public void onResponse(EthereumResponse ethereumResponse) {
                //这里收到交易成功的通知
                try {
                    TransactionReceipt transactionReceipt = RMBServer.objectMapper.readValue(ethereumResponse.getContent(), TransactionReceipt.class);
                    List<Meshchain.RetLogEventResponse> responses = Meshchain.getRetLogEvents(transactionReceipt);
                    if (responses.size() > 0) {
                        //按照业务合约解析event log,如果成功，则通知释放冻结
                        Meshchain.RetLogEventResponse response = responses.get(0);
                        if (response.code.getValue().intValue() == 0) {
                            System.out.println("registerMerchant in hot chain success");
                        } else {
                            System.out.println("registerMerchant in hot chain failed.code:" + response.code.getValue() + ",msg:" + org.bcos.proxy.util.Error.ERROR_MAP.get(response.code.getValue().toString()));
                        }
                    } else {
                        System.out.println("registerMerchant in hot chain onResponse error");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        request.send();

        for(String key : nameSetServiceMap.keySet()) {
            JSONObject subJsonObject = JSON.parseObject("{}");
            subJsonObject.put("contract", "Meshchain");
            subJsonObject.put("func", "registerMerchant");
            subJsonObject.put("version", contractVersion);
            List<Object> subParamsList = new ArrayList<>();
            subParamsList.add(merchantId);
            subParamsList.add(merchantName);
            subParamsList.add(1);
            subJsonObject.put("params", subParamsList);

            Random r1 = new Random();
            BigInteger randomid1 = new BigInteger(250, r1);
            BigInteger blockLimit1 = nameSetServiceMap.get(key).getWeb3j().getBlockNumberCache();
            RawTransaction rawTransaction1 = RawTransaction.createTransaction(randomid1, RMBServer.gasPrice, RMBServer.gasLimit, blockLimit1, "", Numeric.toHexString(subJsonObject.toJSONString().getBytes()));
            String signMsg1 = Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction1, nameSetServiceMap.get(key).getCredentials()));
            Request request1 =  nameSetServiceMap.get(key).getWeb3j().ethSendRawTransaction(signMsg1);
            request1.setNeedTransCallback(true);
            request1.setTransactionSucCallback(new TransactionSucCallback() {
                @Override
                public void onResponse(EthereumResponse ethereumResponse) {
                    //这里收到交易成功的通知
                    try {
                        TransactionReceipt transactionReceipt = RMBServer.objectMapper.readValue(ethereumResponse.getContent(), TransactionReceipt.class);
                        List<Meshchain.RetLogEventResponse> responses = Meshchain.getRetLogEvents(transactionReceipt);
                        if (responses.size() > 0) {
                            //按照业务合约解析event log,如果成功，则通知释放冻结
                            Meshchain.RetLogEventResponse response = responses.get(0);
                            if (response.code.getValue().intValue() == 0) {
                                System.out.printf("registerMerchant in %s success\n", key);
                            } else {
                                System.out.printf("registerMerchant in %s failed.code:%d, msg:%s\n", key, response.code.getValue(), org.bcos.proxy.util.Error.ERROR_MAP.get(response.code.getValue().toString()));
                            }

                        } else {
                            System.out.println("registerMerchant in:" + key + " chain onResponse failed");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            request1.send();
        }
    }

    /**
     * @desc 想热点链添加可信任公钥
     * @param pub 公钥
     * @throws IOException
     */
    public static void addPub(String pub) throws IOException {
        JSONObject jsonObject = JSON.parseObject("{}");
        jsonObject.put("contract", "Meshchain");
        jsonObject.put("func", "addPub");
        jsonObject.put("version", contractVersion);
        List<Object> paramsList = new ArrayList<>();
        paramsList.add(pub);
        jsonObject.put("params", paramsList);

        Random r = new Random();
        BigInteger randomid = new BigInteger(250, r);
        BigInteger blockLimit = web3jHot.getBlockNumberCache();
        RawTransaction rawTransaction = RawTransaction.createTransaction(randomid, RMBServer.gasPrice, RMBServer.gasLimit, blockLimit, "", Numeric.toHexString(jsonObject.toJSONString().getBytes()));
        String signMsg = Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentialsHot));
        Request request =  web3jHot.ethSendRawTransaction(signMsg);
        request.setNeedTransCallback(true);
        request.setTransactionSucCallback(new TransactionSucCallback() {
            @Override
            public void onResponse(EthereumResponse ethereumResponse) {
                //这里收到交易成功的通知
                try {
                    System.out.println("addPub onResponse success");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        request.send();
    }

    public static void queryPub(String jsonStr) throws Exception {

        Web3j web3j = web3jHot;

        String data = Numeric.toHexString(jsonStr.getBytes());

        EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(null, null, data), DefaultBlockParameterName.LATEST).sendAsync().get();
        String value = ethCall.getResult();
        JSONArray array = JSON.parseArray(value);
        if (array.size() != 1) {
            throw new Exception("array size != 1");
        }

        boolean exist = array.getBoolean(0);
        System.out.println("queryPub exist:" + exist);
    }

    public static void querySetUsers(int idx) throws Exception {
        String routeAddr = Config.getConfig().getRouteAddress();
        RouteManager routeManager = RouteManager.load(routeAddr, web3jRoute, credentialsRoute, gasPrice, gasLimit);
        Uint256 uint256 = new Uint256(idx);
        Future<List<Type>>  resultFuture = routeManager.getSetAddress(uint256);
        List<Type> result = resultFuture.get(5, TimeUnit.SECONDS);
        if (result.size() != 2) {
            System.out.println("querySetUsers error.size not 2");
            return;
        }

        Bool ok = (Bool)result.get(0);
        Address address = (Address) result.get(1);
        if (!ok.getValue()) {
            System.out.println("querySetUsers error.can not find idx = " + idx);
            return;
        }


        Set set = Set.load(address.toString(), web3jRoute, credentialsRoute, gasPrice, gasLimit);
        Future<DynamicArray<Bytes32>> userFuture = set.userList();
        DynamicArray<Bytes32> userResult = userFuture.get(5, TimeUnit.SECONDS);

        if (userResult.getValue().size() == 0) {
            System.out.println("not found any user id in idx = " + idx);
            return;
        }


        for (Bytes32 user : userResult.getValue()) {
            System.out.println("found user id = " + new String(user.getValue()));
        }
    }


    public static void main(String[] args) throws Exception {

        if (args == null || args.length < 2) {
            System.out.println("usage:[deploy nodes.json");
            System.out.println("      [queryMerchantId $chainName, $requestStr");
            System.out.println("      [registerMerchant $merchantId, $merchantName");
            System.out.println("      [queryAssets $chainName, $requestStr");
            System.out.println("      [querySetUsers $setIdx(0代表set1, 1代表set2,类推)]");
            System.out.println("      [demo $requestStr]");
            System.exit(0);
        }

        if ("deploy".equals(args[0])) {
            JSONArray jsonArray = readJSONFile(args[1]);
            deployContract(jsonArray);
        } else if ("queryMerchantId".equals(args[0])) {
            queryMerchantId(args[1], args[2]);
        } else if ("registerMerchant".equals(args[0])){
            registerMerchant(args[1], args[2]);
        } else if("queryAssets".equals(args[0])) {
            List<Integer> ret = queryAssets(args[1], args[2]);
            System.out.printf("chainName:%s, queryAssets get availAssets:%d\n", args[1], ret.get(0);
        } else if ("addPub".equals(args[0])){
            addPub(args[1]);
        } else if ("queryPub".equals(args[0])){
           queryPub(args[1]);
        } else if ("querySetUsers".equals(args[0])){
            querySetUsers(Integer.parseInt(args[1]));
        } else if ("demo".equals(args[0])){
            demoForQueryMerchantAssets(args[1]);
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
