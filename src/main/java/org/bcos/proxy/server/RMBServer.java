package org.bcos.proxy.server;

import org.bcos.channel.client.Service;
import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.channel.dto.EthereumResponse;
import org.bcos.proxy.config.Config;
import org.bcos.proxy.contract.Meshchain;
import org.bcos.proxy.contract.RouteManager;
import org.bcos.proxy.task.RelayTask;
import org.bcos.proxy.util.Error;
import org.bcos.web3j.abi.datatypes.Utf8String;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by fisco-dev on 17/8/27.
 */
public  class RMBServer {
    public static BigInteger gasPrice = new BigInteger("1000000");
    public static BigInteger gasLimit = new BigInteger("1000000");

    private static Logger logger = LoggerFactory.getLogger(RMBServer.class);
    public final static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    /**
     * route chain service
     */
    private WCS routeWCS;

    private RouteManager routeManager;//路由的合约实例

    /**
     * hot chain service
     */
    private WCS hotWCS;

    /**
     * map set service
     */

    /**
     * relay task
     */
    private RelayTask relayTask;

    private ConcurrentHashMap<String, WCS> nameSetServiceMap = new ConcurrentHashMap();

    private Config config;

    public  RMBServer(Config config) {
        this.config = config;
    }

    public void init() {
        this.initSevice();
        this.initRouteManager();
        this.initRelayTask();
    }

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
            this.routeWCS = new WCS(web3jRoute, credentialsRoute, serviceRoute, this.config.getRouteChainName());
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
                WCS wcs = new WCS(web3jSet, Credentials.create(this.config.getPrivateKey()), serviceSet, setName);//先直接公用私钥
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
            this.hotWCS = new WCS(web3jHot, credentialsHot, serviceHot, this.config.getHotChainName());
        } catch (InterruptedException e) {
            logger.error("initSevice InterruptedException ", e);
        }

    }


    private WCS getSetNameService(Uint256 setId) throws ExecutionException, InterruptedException {
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
    private boolean sendChainMessage(WCS wcs, String jsonStr, final CompletableFuture<Error> future) throws IOException {
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
