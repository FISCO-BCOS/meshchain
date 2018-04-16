package org.bcos.proxy.server;

/**
 * Created by fisco-dev on 17/11/8.
 */
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import org.bcos.channel.client.Service;
import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.channel.dto.EthereumResponse;
import org.bcos.proxy.config.Config;
import org.bcos.proxy.contract.Meshchain;
import org.bcos.proxy.contract.RouteManager;
import org.bcos.proxy.contract.Set;
import org.bcos.proxy.protocol.UserInfo;
import org.bcos.proxy.protocol.UserReq;
import org.bcos.proxy.task.RelayTask;
import org.bcos.proxy.util.Error;
import org.bcos.proxy.util.ToolUtil;
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
import org.bcos.web3j.protocol.core.methods.request.ProofMerkle;
import org.bcos.web3j.protocol.core.methods.request.RawTransaction;
import org.bcos.web3j.protocol.core.methods.request.Transaction;
import org.bcos.web3j.protocol.core.methods.response.EthCall;
import org.bcos.web3j.protocol.core.methods.response.EthGetProofMerkle;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.utils.Numeric;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HttpServer extends AbstractHandler {

    public final static String ROUTE_SERVICE_NAME = "routeService";
    public final static String CONTRACT_NAME = "Meshchain";

    public final static String CONTRACT_METHOD_REGISTER = "register";
    public final static String CONTRACT_METHOD_DEPOSIT = "deposit";
    public final static String CONTRACT_METHOD_TRANSFER = "transfer";
    public final static String CONTRACT_METHOD_TRANSFER_CANCEL = "transferInterChainCancel";
    public final static String CONTRACT_METHOD_TRANSFER_CONFIRM = "transferInterChainConfirm";
    public final static String CONTRACT_METHOD_USER_INFO = "getUserInfo";
    public final static String CONTRACT_METHOD_HOT_ACCOUNT_INFO = "getHotAccoutByName";
    public final static String CONTRACT_METHOD_SUB_HOT_ACCOUNT_INFO = "getSubHotAccoutByName";

    public final static int NORMAL_ACCOUNT = 0;
    public final static int HOT_ACCOUNT = 1;//hot account
    public final static int SUB_HOT_ACCOUNT = 2;//sub hot account

    public final static int FUTURE_MAX_WAIT_SECOND = 5;

    public static BigInteger gasPrice = new BigInteger("1000000");
    public static BigInteger gasLimit = new BigInteger("1000000");

    private static Logger logger = LoggerFactory.getLogger(HttpServer.class);
    public final static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    @Data
    public static class  WCS {
        private String name;
        private Web3j web3j;
        private Credentials credentials;
        private Service service;

        public WCS(Web3j web3j, Credentials credentials, Service service, String name) {
            this.service = service;
            this.web3j = web3j;
            this.credentials = credentials;
            this.name = name;
        }
    }

    @Data
    public static class Rsp {
        private Error error;
        private String data;

        Rsp(Error error, String data) {
            this.error = error;
            this.data = data;
        }
    }

    /**
     * route chain service
     */
    private WCS routeWCS;

    private RouteManager routeManager;//路由的合约实例

    /**
     * relay task
     */
    private RelayTask relayTask;

    /**
     * map set service
     */
    private ConcurrentHashMap<String, WCS> nameSetServiceMap = new ConcurrentHashMap();

    private Config config;

    public HttpServer(Config config) {
        this.config = config;
        this.init();
    }

    public void init() {
        this.initSevice();
        this.initRouteManager();
        this.initRelayTask();
    }


    private void initRouteManager() {
        this.routeManager = RouteManager.load(this.config.getRouteAddress(), this.routeWCS.getWeb3j(), this.routeWCS.getCredentials(), gasPrice, gasLimit);
    }

    private void initRelayTask() {
        if (Config.enableTimeTask == 1) {
            this.relayTask = new RelayTask(Config.timeTaskIntervalSecond * 1000, this.nameSetServiceMap, this.config.getHotChainName(), this.config.getHotAccountList());
            this.relayTask.schedule();
        }
    }

    private void initSevice() {

        //init for route service
        Credentials credentialsRoute = Credentials.create(this.config.getPrivateKey());
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service serviceRoute = (Service)context.getBean(ROUTE_SERVICE_NAME);
        ChannelEthereumService channelEthereumServiceRoute = new ChannelEthereumService();
        channelEthereumServiceRoute.setChannelService(serviceRoute);
        serviceRoute.run();

        Web3j web3jRoute = Web3j.build(channelEthereumServiceRoute);
        this.routeWCS = new WCS(web3jRoute, credentialsRoute, serviceRoute, ROUTE_SERVICE_NAME);


        //init for all set service
        for (String setName : context.getBeanNamesForType(Service.class)) {
            Service serviceSet = (Service)context.getBean(setName);
            ChannelEthereumService setChannelEthereumService = new ChannelEthereumService();
            setChannelEthereumService.setChannelService(serviceSet);
            serviceSet.run();
            Web3j web3jSet = Web3j.build(setChannelEthereumService);
            WCS wcs = new WCS(web3jSet, Credentials.create(this.config.getPrivateKey()), serviceSet, setName);
            nameSetServiceMap.put(setName, wcs);
        }

        try {
            //3s wait for building connection
            logger.info("wait 3s for init services");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.error("InterruptedException for sleep.", e);
        }

    }

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException
    {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);


        ServletInputStream in = request.getInputStream();
        ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len = -1;
        while ((len = in.read(buff)) != -1) {
            dataBuffer.write(buff, 0, len);
        }
        byte[] contentBuffer = dataBuffer.toByteArray();
        String content = new String(contentBuffer, "UTF-8");

        logger.debug("get handle message:{}", content);

        try {
            UserReq userReq = objectMapper.readValue(content, UserReq.class);
            if (ToolUtil.isEmpty(userReq.getFunc()) || ToolUtil.isEmpty(userReq.getContractName())
                    || userReq.getParams() == null
                    || userReq.getParams().size() == 0) {

                response.getWriter().println(getResponse(-1, "", "func,contract name, or params is empty"));
                return;
            }

            String func = userReq.getFunc();
            String contractName = userReq.getContractName();
            String version = userReq.getVersion();
            List<Object> tmParams = userReq.getParams();
            List<Object> params = new ArrayList<>();
            params.add(userReq.getUid());
            params.addAll(tmParams);

            if (!CONTRACT_NAME.equals(contractName)) {
                response.getWriter().println(getResponse(-1, "", "contract name not support"));
                return;
            }

            JSONObject jsonObject = JSON.parseObject("{}");
            jsonObject.put("contract", contractName);
            jsonObject.put("version", version);
            jsonObject.put("params", params);

            if (!ToolUtil.isEmpty(userReq.getUid())) {
                BigInteger setId = registerUserInRouteIfAbsent(userReq.getUid());
                if (setId == null) {
                    response.getWriter().println(getResponse(Integer.parseInt(Error.USER_REGISTER_ERROR.getCode()), "", Error.USER_REGISTER_ERROR.getDescription()));
                    return;
                }

                WCS wcs = getSetNameService(new Uint256(setId));
                if (wcs == null) {
                    response.getWriter().println(getResponse(Integer.parseInt(Error.SERVICE_ERROR.getCode()), "", Error.SERVICE_ERROR.getDescription()));
                    return;
                }

                switch (func) {
                    case CONTRACT_METHOD_DEPOSIT:
                        jsonObject.put("func", CONTRACT_METHOD_DEPOSIT);
                        final CompletableFuture<Rsp> depFuture = new CompletableFuture<>();
                        sendChainMessage(wcs, jsonObject.toJSONString(), depFuture);
                        Rsp depRsp = depFuture.get(FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
                        response.getWriter().println(getResponse(Integer.parseInt(depRsp.getError().getCode()), depRsp.getData() == null ? "" : depRsp.getData(), depRsp.getError().getDescription()));
                        return;
                    case CONTRACT_METHOD_REGISTER:
                        jsonObject.put("func", CONTRACT_METHOD_REGISTER);
                        final CompletableFuture<Rsp> registerFuture = new CompletableFuture<>();
                        sendChainMessage(wcs, jsonObject.toJSONString(), registerFuture);
                        Rsp registerRsp = registerFuture.get(FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
                        response.getWriter().println(getResponse(Integer.parseInt(registerRsp.getError().getCode()), registerRsp.getData() == null ? "" : registerRsp.getData(), registerRsp.getError().getDescription()));
                        return;
                    case CONTRACT_METHOD_TRANSFER:
                        if (params.size() < 3) {
                            //first is from uid,second is to uid,third is assets
                            response.getWriter().println(getResponse(Integer.parseInt(Error.ARGS_ERROR.getCode()), "", Error.ARGS_ERROR.getDescription()));
                            return;
                        }

                        String toUid = params.get(1).toString();
                        BigInteger toUidSetId = registerUserInRouteIfAbsent(toUid);
                        if (toUidSetId == null) {
                            response.getWriter().println(getResponse(Integer.parseInt(Error.USER_REGISTER_ERROR.getCode()), "", Error.USER_REGISTER_ERROR.getDescription()));
                            return;
                        }

                        WCS toWcs = getSetNameService(new Uint256(toUidSetId));
                        if (toWcs == null) {
                            response.getWriter().println(getResponse(Integer.parseInt(Error.SERVICE_ERROR.getCode()), "", Error.SERVICE_ERROR.getDescription()));
                            return;
                        }

                        //first to transfer in `from`
                        UserInfo userInfo = queryUserInfo(toWcs, toUid);
                        if (userInfo == null) {
                            response.getWriter().println(getResponse(-1, "", "not found user info"));
                            return;
                        }

                        int identity = userInfo.getIdentity();
                        //from uid and to uid are in same chain or to uid is a hot account
                        if (setId.intValue() == toUidSetId.intValue() || identity == HOT_ACCOUNT) {
                            Rsp rsp = transferInSameChain(wcs, userReq.getUid(), toUid, userInfo.getName(), Integer.parseInt(params.get(2).toString()), identity, contractName, version);
                            response.getWriter().println(getResponse(Integer.parseInt(rsp.getError().getCode()), rsp.getData(), rsp.getError().getDescription()));
                            return;
                        } else {
                            //from uid and to uid are inter chain
                            Rsp rsp = transferInterChain(wcs, toWcs, userReq.getUid(), toUid, Integer.parseInt(params.get(2).toString()), contractName, version);
                            response.getWriter().println(getResponse(Integer.parseInt(rsp.getError().getCode()), rsp.getData(), rsp.getError().getDescription()));
                            return;
                        }
                }

            } else {
                response.getWriter().println(getResponse(Integer.parseInt(Error.ARGS_ERROR.getCode()), "", "missed uid"));
                return;
            }

        } catch (Exception e) {
            logger.error("Exception:", e);
            response.getWriter().println(getResponse(-1, "", e.getMessage()));
            return;
        }

    }


    /**
     *
     * @param uid user id
     * @return set id
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private BigInteger registerUserInRouteIfAbsent(String uid) throws ExecutionException, InterruptedException {

        byte[] uidBytes = Arrays.copyOf(uid.getBytes(), 32);
        Bytes32 uidByte32 = new Bytes32(uidBytes);

        List<Type> typeList = this.routeManager.getRoute(uidByte32).get();
        if (typeList.size() != 2) {
            //size must match RouteManager contract return
            logger.error("get route error.uid:{}", uid);
            return null;
        }

        Boolean existed = (Boolean)typeList.get(0).getValue();
        BigInteger setId = (BigInteger)typeList.get(1).getValue();

        if (existed) {
            return setId;
        }

        TransactionReceipt transactionReceipt = this.routeManager.registerRoute(uidByte32).get();
        List<RouteManager.RegisterRetLogEventResponse> responses = routeManager.getRegisterRetLogEvents(transactionReceipt);
        List<Set.WarnEventResponse> warnResponseList = Set.getWarnEvents(transactionReceipt);
        if (warnResponseList.size() > 0) {
            //catch warn size log
            Set.WarnEventResponse  warnResponse = warnResponseList.get(0);
            logger.warn("uid:{} register warn.code:{}, setId:{}, msg:{}", warnResponse.code.getValue(), warnResponse.setid.getValue(), warnResponse.msg);
        }

        if (responses.size() > 0) {
            RouteManager.RegisterRetLogEventResponse registerRetLogEventResponse = responses.get(0);
            //first on is bool, second is setId
            if (registerRetLogEventResponse.ok.getValue()) {
                logger.info("uid:{} register ok", uid);
                return registerRetLogEventResponse.set.getValue();
            } else {
                //register failed
                logger.error("uid:{} register failed.may be set is full...setid:{}", uid, registerRetLogEventResponse.set.getValue().intValue());
                return null;
            }
        } else {
            //event log is empty.
            logger.error("event log size is 0");
            return null;
        }

    }

    private String getResponse(int code, String data, String message) {
        JSONObject rsp = JSON.parseObject("{}");
        rsp.put("code", code);
        rsp.put("data", data);
        rsp.put("message", message);
        return rsp.toJSONString();
    }

    /**
     *
     * @param setId
     * @return set service
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private WCS getSetNameService(Uint256 setId) throws ExecutionException, InterruptedException {
        logger.info("getSetNameService setId:{}", setId.getValue());
        Future<Utf8String> addressFuture = this.routeManager.m_setNames(setId);
        String setName = addressFuture.get().toString();
        return this.nameSetServiceMap.get(setName);
    }


    /**
     *
     * @param fromWCS from uid's service
     * @param fromUid
     * @param toUid
     * @param toName to uid's username
     * @param assets
     * @param identity
     * @param contractName contract name
     * @param version contract version
     * @return rsp contain code and data
     * @throws Exception
     */
    public static Rsp transferInSameChain(WCS fromWCS, String fromUid, String toUid, String toName, int assets, int identity, String contractName, String version) throws Exception {
        //`from` and `to` are in same chain
        JSONObject jsonObject = JSON.parseObject("{}");
        jsonObject.put("contract", contractName);
        jsonObject.put("version", version);
        jsonObject.put("func", "transferOneChain");
        if (identity == HOT_ACCOUNT) {
            //check toName if sub hot account
            UserInfo hotAccount = getAccountInfoByName(fromWCS, toName, true);
            if (hotAccount == null || "".equals(hotAccount.getUid()) || hotAccount.getIdentity() != SUB_HOT_ACCOUNT) {
                logger.error("can not found sub account name:{}, fromUid:{}, toUid:{}", toName, fromUid, toUid);
                Rsp rsp = new Rsp(Error.CONTRACT_USER_NOT_EXIST, "not found sub hot account");
                return rsp;
            }

            List<Object> params = new ArrayList<>();
            params.add(fromUid);
            params.add(hotAccount.getUid());
            params.add(assets);
            jsonObject.put("params", params);
        } else {
            List<Object> params = new ArrayList<>();
            params.add(fromUid);
            params.add(toUid);
            params.add(assets);
            jsonObject.put("params", params);
        }

        final CompletableFuture<Rsp> transferFuture = new CompletableFuture<>();
        sendChainMessage(fromWCS, jsonObject.toJSONString(), transferFuture);
        Rsp rsp = transferFuture.get(FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
        if (rsp.getError().getCode().equals(Error.OK.getCode())) {
            JSONObject tmpJson = JSON.parseObject(rsp.getData());
            JSONObject resJson = JSON.parseObject("{}");
            resJson.put("from_transfer_id", tmpJson.getString("transferId"));
            resJson.put("to_transfer_id", tmpJson.getString("transferId"));
            rsp.setData(resJson.toJSONString());
        }

        return rsp;
    }

    /**
     *
     * @param fromWCS from uid's service
     * @param toWCS to uid's service
     * @param fromUid
     * @param toUid
     * @param assets
     * @param contractName contract name
     * @param version contract version
     * @return rsp contain code and data
     * @throws Exception
     */
    public static Rsp transferInterChain(WCS fromWCS, WCS toWCS, String fromUid, String toUid, int assets, String contractName, String version) throws Exception {
        //first to transfer in `from`
        JSONObject jsonObject = JSON.parseObject("{}");
        jsonObject.put("contract", contractName);
        jsonObject.put("version", version);
        jsonObject.put("func", "transferInterChainByFrom");
        List<Object> params = new ArrayList<>();
        params.add(fromUid);
        params.add(toUid);
        params.add(assets);
        jsonObject.put("params", params);

        final CompletableFuture<Rsp> transferFromFuture = new CompletableFuture<>();
        sendChainMessage(fromWCS, jsonObject.toJSONString(), transferFromFuture);
        Rsp rsp = transferFromFuture.get(FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
        if (rsp.getError().getCode() != Error.OK.getCode()) {
            //get from error, return now
            logger.error("error code for transferInterChainByFrom.code:{}", rsp.getError().getCode());
            return rsp;
        }

        JSONObject jsonMerkleProof = getProofMerkle(fromWCS, rsp.getData());
        boolean verifyOk = false;

        try {
            verifyOk = verifySign(toWCS, jsonMerkleProof);
            if (!verifyOk) {
                JSONObject jsonRsp = JSON.parseObject(rsp.getData());
                final CompletableFuture<Rsp> cancelFuture = new CompletableFuture<>();
                transferInterChainCancel(toWCS, jsonRsp.getBigInteger("transferId"), cancelFuture);
                Rsp cancelRsp = cancelFuture.get(FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
                if (cancelRsp.getError().getCode() != Error.OK.getCode()) {
                    logger.error("transferInterChainCancel error.transfer id:{}", jsonRsp.getBigInteger("transferId"));
                    return cancelRsp;
                }
            }
        } catch (Exception e) {
            logger.error("verifySign exception.", e);
        }

        if (!verifyOk) {
            logger.error("verify sign error.");
            rsp.setError(Error.CONTRACT_VERIFY_SIGN_ERROR);
            return rsp;
        }

        JSONObject jsonTransactionRsp = JSON.parseObject(rsp.getData());
        logger.debug("jsonMerkleProof:{}, jsonTransactionRsp:{}", jsonMerkleProof.toJSONString(), jsonTransactionRsp.toJSONString());


        List<Object> transferInterChainByToParamList= new ArrayList<>();
        transferInterChainByToParamList.add(jsonMerkleProof.getString("root"));

        JSONArray proofArr = jsonMerkleProof.getJSONArray("proofs");
        StringBuilder proofSb = new StringBuilder();
        for(Object proof : proofArr) {
            proofSb.append(proof.toString()).append(";");
        }

        String proofStr = "";
        if (proofSb.length() > 0) {
            proofStr = proofSb.substring(0, proofSb.length() - 1);
        }

        transferInterChainByToParamList.add(proofStr);
        transferInterChainByToParamList.add(jsonTransactionRsp.getString("transactionIndex"));
        transferInterChainByToParamList.add(jsonMerkleProof.getString("value"));
        transferInterChainByToParamList.add(fromUid);//from
        transferInterChainByToParamList.add(toUid);//to
        transferInterChainByToParamList.add(assets);//assets

        jsonObject.put("func", "transferInterChainByTo");
        jsonObject.put("params", transferInterChainByToParamList);

        final CompletableFuture<Rsp> transferToFuture = new CompletableFuture<>();
        sendChainMessage(toWCS, jsonObject.toJSONString(), transferToFuture);
        Rsp toRsp = transferToFuture.get(FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
        if (toRsp.getError().getCode() != Error.OK.getCode()) {
            //get to error, cancel and return now.
            JSONObject jsonRsp = JSON.parseObject(rsp.getData());
            final CompletableFuture<Rsp> cancelFuture = new CompletableFuture<>();
            transferInterChainCancel(toWCS, jsonRsp.getBigInteger("transferId"), cancelFuture);
            Rsp cancelRsp = cancelFuture.get(FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
            if (cancelRsp.getError().getCode() != Error.OK.getCode()) {
                logger.error("transferInterChainCancel error.transfer id:{}", jsonRsp.getBigInteger("transferId"));
            }

            logger.error("error for transferInterChainByTo.code:{}", toRsp.getError().getCode());
            return toRsp;
        }

        JSONObject jsonRsp = JSON.parseObject(rsp.getData());
        final CompletableFuture<Rsp> confirmFuture = new CompletableFuture<>();
        transferInterChainConfirm(fromWCS, jsonRsp.getBigInteger("transferId"), confirmFuture);
        Rsp confirmRsp = confirmFuture.get(FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
        if (confirmRsp.getError().getCode() != Error.OK.getCode()) {
            //confirm error, cancel and return now.
            logger.error("transferInterChainConfirm error.transfer id:{}, jsonTransactionRsp:{}", jsonRsp.getBigInteger("transferId"), jsonTransactionRsp.toJSONString());
            return confirmRsp;
        }

        JSONObject tmpJson = JSON.parseObject("{}");
        JSONObject jsonFromRsp = JSON.parseObject(rsp.getData());
        JSONObject jsonToRsp = JSON.parseObject(toRsp.getData());

        tmpJson.put("from_transfer_id", jsonFromRsp.getString("transferId"));
        tmpJson.put("to_transfer_id", jsonToRsp.getString("transferId"));

        rsp.setError(Error.OK);
        rsp.setData(tmpJson.toJSONString());
        return rsp;
    }

    /**
     *
     * @param wcs set service
     * @param jsonStr cns json req
     * @param future
     * @return boolean
     * @throws IOException
     */
    public static boolean sendChainMessage(WCS wcs, String jsonStr, final CompletableFuture<Rsp> future) throws IOException {
        if (wcs == null) {
            logger.error("wcs is null");
            Rsp rsp = new Rsp(Error.SERVICE_ERROR, null);
            future.complete(rsp);
            return false;
        }

        logger.debug("sendChainMessage to set:{},jsonStr:{}", wcs.getName(), jsonStr);
        Random r = new Random();
        BigInteger randomid = new BigInteger(250, r);
        BigInteger blockLimit = wcs.getWeb3j().getBlockNumberCache();
        RawTransaction rawTransaction = RawTransaction.createTransaction(randomid, gasPrice, gasLimit, blockLimit, "", Numeric.toHexString(jsonStr.getBytes()));
        String signMsg = Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, wcs.getCredentials()));
        org.bcos.web3j.protocol.core.Request request =  wcs.getWeb3j().ethSendRawTransaction(signMsg);
        request.setNeedTransCallback(true);
        request.setTransactionSucCallback(new TransactionSucCallback() {
            @Override
            public void onResponse(EthereumResponse ethereumResponse) {
                //receipt transaction success notify
                try {
                    logger.debug("onResponse callback content:{}", ethereumResponse.getContent());
                    TransactionReceipt transactionReceipt = objectMapper.readValue(ethereumResponse.getContent(), TransactionReceipt.class);
                    List<Meshchain.RetLogEventResponse> retLogresponses = Meshchain.getRetLogEvents(transactionReceipt);
                    if (retLogresponses.size() > 0) {
                        Meshchain.RetLogEventResponse retResponse = retLogresponses.get(0);
                        if (retResponse.code.getValue().intValue() != 0) {
                            //get error from event log `retLog`
                            Error error = Error.ERROR_MAP.get(retResponse.code.getValue().toString());
                            logger.error("sendChainMessage onResponse data code:{}, desc:{}", retResponse.code.getValue(), error.getDescription());

                            Rsp rsp = new Rsp(error, null);
                            future.complete(rsp);
                            return;
                        }

                        List<Meshchain.DepLogEventResponse> depLogresponses = Meshchain.getDepLogEvents(transactionReceipt);

                        if (depLogresponses.size() > 0) {
                            //deposit function
                            Meshchain.DepLogEventResponse depLogEventResponse = depLogresponses.get(0);
                            Error ok = Error.OK;
                            JSONObject retJson = JSON.parseObject("{}");
                            retJson.put("deposit_id", depLogEventResponse.id.getValue().toString());

                            Rsp rsp = new Rsp(ok, retJson.toJSONString());
                            future.complete(rsp);
                            return;
                        }

                        List<Meshchain.TransferLogEventResponse> transferLogresponses = Meshchain.getTransferLogEvents(transactionReceipt);

                        if (transferLogresponses.size() > 0) {
                            //transfer function
                            Meshchain.TransferLogEventResponse transferLogEventResponse = transferLogresponses.get(0);
                            Error ok = Error.OK;
                            JSONObject retJson = JSON.parseObject("{}");
                            retJson.put("blockHash", transactionReceipt.getBlockHash());
                            retJson.put("transactionIndex", transactionReceipt.getTransactionIndexRaw());
                            retJson.put("transferId", transferLogEventResponse.id.getValue().intValue());

                            Rsp rsp = new Rsp(ok, retJson.toJSONString());
                            future.complete(rsp);
                            return;
                        }

                        Error error = Error.ERROR_MAP.get(retResponse.code.getValue().toString());
                        Rsp rsp = new Rsp(error, null);
                        future.complete(rsp);
                        return;
                    } else {
                        Rsp rsp = new Rsp(Error.CONTRACT_ERROR, null);
                        future.complete(rsp);
                        return;
                    }

                } catch (Exception e) {
                    logger.error("onResponse Exception", e);
                    Rsp rsp = new Rsp(Error.SYSTEM_ERROR, null);
                    future.complete(rsp);
                }

            }
        });

        request.send();
        return true;
    }

    /**
     * @desc transaction merkle proof
     * @param wcs
     * @param jsonStr {"blockHash":"", "transactionIndex:""}
     * @return {"root":"", "proofs":[], "pubs":[], "value":""}
     * @throws IOException
     */
    public static JSONObject getProofMerkle(WCS wcs, String jsonStr) throws Exception {
        if (wcs == null) {
            logger.error("wcs is null");
            return null;
        }

        JSONObject reqJson = JSON.parseObject(jsonStr);
        if (!reqJson.containsKey("blockHash") || !reqJson.containsKey("transactionIndex")) {
            throw new Exception("getProofMerkle request:" + jsonStr + " error");
        }

        logger.debug("getProofMerkle to set:{},jsonStr:{}", wcs.getName(), jsonStr);
        ProofMerkle proofMerkle = new ProofMerkle(reqJson.getString("blockHash"), reqJson.getString("transactionIndex"));
        org.bcos.web3j.protocol.core.Request<?, EthGetProofMerkle> request =  wcs.getWeb3j().ethGetProofMerkle(proofMerkle);

        EthGetProofMerkle response = request.send();

        if (response.getResult() == null) {
            throw new Exception("getProofMerkle result null:");
        }

        String proofStr = objectMapper.writeValueAsString(response.getResult());
        logger.info("getProofMerkle response hash result:{}", proofStr);

        JSONObject retJson = JSON.parseObject(proofStr);
        if (!retJson.containsKey("root") || !retJson.containsKey("proofs")
                || !retJson.containsKey("pubs") || !retJson.containsKey("value") || !retJson.containsKey("signs")) {
            throw new Exception("getProofMerkle response error:" + retJson.toJSONString());
        }

        return retJson;
    }


    /**
     *
     * @param wcs
     * @param jsonMerkleProof
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static boolean verifySign(WCS wcs, JSONObject jsonMerkleProof) throws ExecutionException, InterruptedException {
        String hash = jsonMerkleProof.getString("hash");
        JSONArray pubArr = jsonMerkleProof.getJSONArray("pubs");
        StringBuilder pubSb = new StringBuilder();
        for(Object pub : pubArr) {
            pubSb.append(pub.toString()).append(";");
        }

        String pubStr = "";
        if (pubSb.length() > 0) {
            pubStr = pubSb.substring(0, pubSb.length() - 1);
        }

        JSONArray signArr = jsonMerkleProof.getJSONArray("signs");
        StringBuilder signSb = new StringBuilder();
        StringBuilder idxSb = new StringBuilder();

        for(int i = 0; i < signArr.size(); i++) {
            JSONObject idxAndsign = signArr.getJSONObject(i);
            signSb.append(idxAndsign.getString("sign").toString()).append(";");
            idxSb.append(idxAndsign.getString("idx").toString()).append(";");
        }

        String signStr = "";
        String idxStr = "";
        if (signSb.length() > 0) {
            signStr = signSb.substring(0, signSb.length() - 1);
        }

        if (idxSb.length() > 0) {
            idxStr = idxSb.substring(0, idxSb.length() - 1);
        }

        List<Object> signPubListParams = new ArrayList<>();
        signPubListParams.add(hash);
        signPubListParams.add(pubStr);
        signPubListParams.add(signStr);
        signPubListParams.add(idxStr);

        JSONObject jsonObject = JSON.parseObject("{}");
        jsonObject.put("contract", CONTRACT_NAME);
        jsonObject.put("func", "verifySign");
        jsonObject.put("version", "");
        jsonObject.put("params", signPubListParams);

        String data = Numeric.toHexString(jsonObject.toJSONString().getBytes());

        EthCall ethCall = wcs.getWeb3j().ethCall(Transaction.createEthCallTransaction(null, null, data), DefaultBlockParameterName.LATEST).sendAsync().get();
        String value = ethCall.getResult();
        JSONArray array = JSON.parseArray(value);
        if (array.size() == 0) {
            logger.warn("verifySign get array size is 0");
            return false;
        }

        int code = array.getInteger(0);
        return code == 0 ? true : false;

    }

    public static void transferInterChainCancel(WCS wcs, BigInteger transferId, final CompletableFuture<Rsp> future) throws IOException {
        List<Object> params = new ArrayList<>();
        params.add(transferId.intValue());

        JSONObject jsonObject = JSON.parseObject("{}");
        jsonObject.put("contract", CONTRACT_NAME);
        jsonObject.put("func", CONTRACT_METHOD_TRANSFER_CANCEL);
        jsonObject.put("version", "");
        jsonObject.put("params", params);

        sendChainMessage(wcs, jsonObject.toJSONString(), future);
    }

    public static void transferInterChainConfirm(WCS wcs, BigInteger transferId, final CompletableFuture<Rsp> future) throws IOException {
        List<Object> params = new ArrayList<>();
        params.add(transferId.intValue());

        JSONObject jsonObject = JSON.parseObject("{}");
        jsonObject.put("contract", CONTRACT_NAME);
        jsonObject.put("func", CONTRACT_METHOD_TRANSFER_CONFIRM);
        jsonObject.put("version", "");
        jsonObject.put("params", params);

        sendChainMessage(wcs, jsonObject.toJSONString(), future);
    }

    /**
     * @desc get user info
     * @param uid
     */
    public static UserInfo queryUserInfo(WCS wcs, String uid) throws Exception {
        Web3j web3j = wcs.getWeb3j();

        if (web3j == null) {
            logger.error("queryUserInfo web3j is null");
            return null;
        }

        JSONObject jsonObject = JSON.parseObject("{}");
        jsonObject.put("contract", CONTRACT_NAME);
        jsonObject.put("func", CONTRACT_METHOD_USER_INFO);
        jsonObject.put("version", "");

        List<Object> params = new ArrayList<>();
        params.add(uid);

        jsonObject.put("params", params);

        String data = Numeric.toHexString(jsonObject.toJSONString().getBytes());

        EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(null, null, data), DefaultBlockParameterName.LATEST).sendAsync().get();
        String value = ethCall.getResult();
        JSONArray array = JSON.parseArray(value);
        if (array.size() != 4) {
            logger.error("queryUserInfo array size 0");
            return null;
        }

        int availAssets = array.getInteger(0);
        int unAvailAssets = array.getInteger(1);
        int identity = array.getInteger(2);
        String name = array.getString(3);

        UserInfo userInfo = new UserInfo(uid, availAssets, unAvailAssets, identity, name);
        return userInfo;

    }

    /**
     * @desc get hot account or sub hot account info
     * @param name account name
     * @param sub true: sub hot account false:
     */
    public static UserInfo getAccountInfoByName(WCS wcs, String name, boolean sub) throws Exception {
        Web3j web3j = wcs.getWeb3j();

        if (web3j == null) {
            logger.error("getHotAccountInfo web3j is null");
            return null;
        }

        JSONObject jsonObject = JSON.parseObject("{}");
        jsonObject.put("contract", CONTRACT_NAME);
        if (!sub) {
            jsonObject.put("func", CONTRACT_METHOD_HOT_ACCOUNT_INFO);
        } else {
            jsonObject.put("func", CONTRACT_METHOD_SUB_HOT_ACCOUNT_INFO);
        }

        jsonObject.put("version", "");

        List<Object> params = new ArrayList<>();
        params.add(name);

        jsonObject.put("params", params);

        String data = Numeric.toHexString(jsonObject.toJSONString().getBytes());

        EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(null, null, data), DefaultBlockParameterName.LATEST).sendAsync().get();
        String value = ethCall.getResult();
        JSONArray array = JSON.parseArray(value);
        if (array.size() == 0) {
            logger.error("getHotAccountInfo array size 0");
            return null;
        }

        String uid = array.getString(0);
        int availAssets = array.getInteger(1);
        int unAvailAssets = array.getInteger(2);
        int identity = array.getInteger(3);

        UserInfo userInfo = new UserInfo(uid, availAssets, unAvailAssets, identity, name);
        return userInfo;

    }


}
