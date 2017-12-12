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

import org.bcos.channel.client.Service;
import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.channel.dto.EthereumResponse;
import org.bcos.proxy.config.Config;
import org.bcos.proxy.contract.Meshchain;
import org.bcos.proxy.contract.RouteManager;
import org.bcos.proxy.contract.Set;
import org.bcos.proxy.protocol.UserReq;
import org.bcos.proxy.task.RelayTask;
import org.bcos.proxy.util.Error;
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
import org.bcos.web3j.protocol.core.Response;
import org.bcos.web3j.protocol.core.methods.request.RawTransaction;
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
    public static BigInteger gasPrice = new BigInteger("1000000");
    public static BigInteger gasLimit = new BigInteger("1000000");
    private static Logger logger = LoggerFactory.getLogger(HttpServer.class);
    public final static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    /**
     * route chain service
     */
    private RMBServer.WCS routeWCS;

    private RouteManager routeManager;//路由的合约实例

    /**
     * hot chain service
     */
    private RMBServer.WCS hotWCS;

    /**
     * map set service
     */

    /**
     * relay task
     */
    private RelayTask relayTask;

    private ConcurrentHashMap<String, RMBServer.WCS> nameSetServiceMap = new ConcurrentHashMap();

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
            this.relayTask = new RelayTask(Config.timeTaskIntervalSecond * 1000, this.hotWCS, this.nameSetServiceMap);
            this.relayTask.schedule();
        }
    }

    private void initSevice() {

        //先初始化route的
        Credentials credentialsRoute = Credentials.create(this.config.getPrivateKey());
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service serviceRoute = (Service)context.getBean(this.config.getRouteChainName());
        serviceRoute.setOrgID("WB");
        ChannelEthereumService channelEthereumServiceRoute = new ChannelEthereumService();
        channelEthereumServiceRoute.setChannelService(serviceRoute);
        serviceRoute.run();

        try {
            //让初始化完成
            logger.info("waitting 3s for init route chain service");
            Thread.sleep(3000);
            Web3j web3jRoute = Web3j.build(channelEthereumServiceRoute);
            this.routeWCS = new RMBServer.WCS(web3jRoute, credentialsRoute, serviceRoute, this.config.getRouteChainName());
        } catch (InterruptedException e) {
            logger.error("initSevice InterruptedException ", e);
        }

        //初始化所有set的
        for (String setName : this.config.getSetNameList()) {
            Service serviceSet = (Service)context.getBean(setName);
            serviceSet.setOrgID("WB");
            ChannelEthereumService setChannelEthereumService = new ChannelEthereumService();
            setChannelEthereumService.setChannelService(serviceSet);
            try {
                serviceSet.run();
                logger.info("waitting 3s for init set:{} chain service", setName);
                Thread.sleep(3000);
                Web3j web3jSet = Web3j.build(setChannelEthereumService);
                RMBServer.WCS wcs = new RMBServer.WCS(web3jSet, Credentials.create(this.config.getPrivateKey()), serviceSet, setName);//先直接公用私钥
                nameSetServiceMap.put(setName, wcs);
            } catch (Exception e) {
                logger.error("initSevice exception", e);
            }
        }

        //初始化热点链的
        Credentials credentialsHot = Credentials.create(this.config.getPrivateKey());
        Service serviceHot = (Service)context.getBean(this.config.getHotChainName());
        serviceHot.setOrgID("WB");
        ChannelEthereumService channelEthereumServiceHot = new ChannelEthereumService();
        channelEthereumServiceHot.setChannelService(serviceHot);
        serviceHot.run();


        try {
            //让初始化完成
            logger.info("waitting 3s for init hot chain service");
            Thread.sleep(3000);
            Web3j web3jHot = Web3j.build(channelEthereumServiceHot);
            this.hotWCS = new RMBServer.WCS(web3jHot, credentialsHot, serviceHot, this.config.getHotChainName());
        } catch (InterruptedException e) {
            logger.error("initSevice InterruptedException ", e);
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

        logger.info("get handle message:{}", content);
        final CompletableFuture<Error> resFuture = new CompletableFuture<>();

        try {
            UserReq userReq = objectMapper.readValue(content, UserReq.class);
            if (ToolUtil.isEmpty(userReq.getMethod()) || ToolUtil.isEmpty(userReq.getContractName())
                    || userReq.getParams() == null
                    || userReq.getParams().size() == 0) {

                response.getWriter().println(getResponse(-1, "", "method,contract name, or params is empty"));
                return;
            }

            String method = userReq.getMethod();
            String contractName = userReq.getContractName();
            String version = userReq.getVersion();
            List<Object> tmParams = userReq.getParams();
            List<Object> params = new ArrayList<>();
            params.add(userReq.getUid());
            params.addAll(tmParams);

            JSONObject jsonObject = JSON.parseObject("{}");
            jsonObject.put("contract", contractName);
            jsonObject.put("func", method);
            jsonObject.put("version", version);
            jsonObject.put("params", params);

            if (!ToolUtil.isEmpty(userReq.getUid())) {
                byte[] uidBytes = Arrays.copyOf(userReq.getUid().getBytes(), 32);
                Bytes32 uidByte = new Bytes32(uidBytes);
                Future<List<Type>> routeFuture = this.routeManager.getRoute(uidByte);
                try {
                    List<Type> result = routeFuture.get();
                    if (result.size() != 2) {
                        //size跟RouteManager合约返回的两个元素保持一致
                        response.getWriter().println(getResponse(-1, "", "route contract error"));
                        return;
                    }

                    Boolean existed = (Boolean)result.get(0).getValue();
                    BigInteger setId = (BigInteger)result.get(1).getValue();
                    logger.info("uid:{},existed:{},setId:{}", userReq.getUid(), existed, setId);
                    if (!existed) {
                        logger.info("start to register uid:{}", userReq.getUid());
                        this.routeManager.registerRoute(uidByte, new TransactionSucCallback() {
                            @Override
                            public void onResponse(EthereumResponse ethereumResponse) {
                                logger.info("registerRoute onResponse:{}", ethereumResponse.getContent());
                                if (ethereumResponse == null || ethereumResponse.getContent() == null) {
                                    logger.error("ethereumResponse content is null");
                                    resFuture.complete(Error.SYSTEM_ERROR);
                                    return;
                                }

                                //解析用户成功注册的event log
                                try {
                                    TransactionReceipt transactionReceipt = objectMapper.readValue(ethereumResponse.getContent(), TransactionReceipt.class);
                                    List<RouteManager.RegisterRetLogEventResponse> responses = routeManager.getRegisterRetLogEvents(transactionReceipt);
                                    List<Set.WarnEventResponse> warnResponseList = Set.getWarnEvents(transactionReceipt);
                                    if (warnResponseList.size() > 0) {
                                        //有警告的日志
                                        Set.WarnEventResponse  warnResponse = warnResponseList.get(0);
                                        logger.warn("uid:{} register warn.code:{}, setId:{}, msg:{}", warnResponse.code, warnResponse.setid, warnResponse.msg);
                                    }

                                    if (responses.size() > 0) {
                                        RouteManager.RegisterRetLogEventResponse registerRetLogEventResponse = responses.get(0);
                                        //第一个是bool，第二个是setId
                                        if (registerRetLogEventResponse.ok.getValue()) {
                                            logger.info("uid:{} register ok", userReq.getUid());
                                            RMBServer.WCS wcs = getSetNameService(new Uint256(setId));
                                            sendChainMessage(wcs, jsonObject.toString(), resFuture);
                                            //registerFuture.complete(registerRetLogEventResponse.set.getValue());
                                        } else {
                                            //register failed
                                            logger.error("uid:{} register failed.may be set is full...", userReq.getUid());
                                            resFuture.complete(Error.USER_REGISTER_ERROR);
                                        }
                                    } else {
                                        //event log为空，注册失败
                                        logger.error("event log size is 0");
                                        resFuture.complete(Error.USER_REGISTER_ERROR);
                                    }
                                } catch (Exception e) {
                                    logger.error("Exception", e);
                                    resFuture.complete(Error.SYSTEM_ERROR);
                                }
                            }
                        });

                    } else {
                        if (setId.intValue() < 0) {
                            logger.error("user register failed.setId:{}", setId);
                            response.getWriter().println(getResponse(-1, "", "user register error"));
                            return;
                        }

                        logger.info("uid:{} is existed.", userReq.getUid());
                        RMBServer.WCS wcs = getSetNameService(new Uint256(setId));
                        sendChainMessage(wcs, jsonObject.toString(), resFuture);
                    }

                } catch (Exception e) {
                    logger.error("getRoute exception,uid:{}", userReq.getUid(), e);
                    response.getWriter().println(getResponse(-1, "", e.getMessage()));
                    return;
                }

            } else {

            }

            Error err = resFuture.get(5, TimeUnit.SECONDS);
            response.getWriter().println(getResponse(Integer.parseInt(err.getCode()), err.getDescription(), ""));

        } catch (Exception e) {
            logger.error("Exception:", e);
            response.getWriter().println(getResponse(-1, "", e.getMessage()));
            return;
        }

    }


    private String getResponse(int code, String data, String message) {
        JSONObject rsp = JSON.parseObject("{}");
        rsp.put("code", code);
        rsp.put("data", data);
        rsp.put("message", message);
        return rsp.toJSONString();
    }

    private RMBServer.WCS getSetNameService(Uint256 setId) throws ExecutionException, InterruptedException {
        logger.info("getSetNameService setId:{}", setId.getValue());
        Future<Utf8String> addressFuture = this.routeManager.m_setNames(setId);
        String setName = addressFuture.get().toString();
        return this.nameSetServiceMap.get(setName);
    }


    /**
     *
     * @param wcs 具体的service
     * @param jsonStr 发送的内容
     * @param future  主要是用到complete方法
     * @return boolean
     * @throws IOException
     */
    private boolean sendChainMessage(RMBServer.WCS wcs, String jsonStr, final CompletableFuture<Error> future) throws IOException {
        if (wcs == null) {
            logger.error("wcs is null");
            future.complete(Error.SERVICE_ERROR);
            return false;
        }

        logger.info("sendChainMessage to set:{},jsonStr:{}", wcs.getName(), jsonStr);
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
                //这里收到交易成功的通知
                try {
                    logger.info("onResponse callback");
                    TransactionReceipt transactionReceipt = objectMapper.readValue(ethereumResponse.getContent(), TransactionReceipt.class);
                    List<Meshchain.RetLogEventResponse> responses = Meshchain.getRetLogEvents(transactionReceipt);
                    if (responses.size() > 0) {
                        //按照业务合约解析event log,设置message的内容，同时要notify
                        Meshchain.RetLogEventResponse response = responses.get(0);
                        Error error = Error.ERROR_MAP.get(response.code.getValue().toString());
                        logger.info("sendChainMessage onResponse data code:{}, desc:{}", response.code.getValue(), error.getDescription());
                        future.complete(error);
                    }
                } catch (Exception e) {
                    logger.error("onResponse Exception", e);
                    future.complete(Error.SYSTEM_ERROR);
                }

            }
        });

        Response response = request.send();
        //交易hash
        logger.info("getSetNameService response result:{}", response.getResult());
        return true;
    }

}
