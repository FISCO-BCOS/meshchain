package org.bcos.proxy.task;

/**
 * Created by fisco fisco-dev on 17/9/27.
 */

import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.channel.dto.EthereumResponse;
import org.bcos.proxy.contract.Meshchain;
import org.bcos.proxy.server.RMBServer;
import org.bcos.proxy.util.Error;
import org.bcos.proxy.util.RelayTaskException;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.crypto.TransactionEncoder;
import org.bcos.web3j.protocol.ObjectMapperFactory;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.DefaultBlockParameterName;
import org.bcos.web3j.protocol.core.Request;
import org.bcos.web3j.protocol.core.Response;
import org.bcos.web3j.protocol.core.methods.request.ProofMerkle;
import org.bcos.web3j.protocol.core.methods.request.RawTransaction;
import org.bcos.web3j.protocol.core.methods.request.Transaction;
import org.bcos.web3j.protocol.core.methods.response.EthCall;
import org.bcos.web3j.protocol.core.methods.response.EthGetProofMerkle;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.utils.Numeric;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RelayTask {

    private static Logger logger = LoggerFactory.getLogger(RelayTask.class);
    public final static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private Timer timer;
    private TimeTask timeTask;
    private long delayMilliseconds;
    private ExecutorService poolExecutor = Executors.newCachedThreadPool();

    private final int MAX_FUTURE_WAIT = 5;

    public static String contractName = "Meshchain";
    public static String contractVersion = "";

    //合约接口名字
    public static String getAllMerchantIds = "getAllMerchantIds";
    public static String merchantWithdrawal = "merchantWithdrawal";
    public static String verifySign = "verifySign";
    public static String verifyProofAndDeposit = "verifyProofAndDeposit";
    public static String confirmWithdrawal = "confirmWithdrawal";

    /**
     * hot chain service
     */
    private RMBServer.WCS hotWCS;

    /**
     * map set service
     */
    private ConcurrentHashMap<String, RMBServer.WCS> nameSetServiceMap = new ConcurrentHashMap();

    public RelayTask(long delayMilliseconds, RMBServer.WCS hotWCS, ConcurrentHashMap<String, RMBServer.WCS> nameSetServiceMap) {
        this.timer = new Timer();
        this.delayMilliseconds = delayMilliseconds;
        this.timeTask = new TimeTask();
        this.hotWCS = hotWCS;
        this.nameSetServiceMap = nameSetServiceMap;
    }

    public void schedule() {
        this.timer.schedule(this.timeTask, this.delayMilliseconds, this.delayMilliseconds);
    }


    public class TimeTask extends TimerTask {

        @Override
        public void run() {
            logger.info("RelayTask start");

            String requestStr = contructRequest(getAllMerchantIds, Arrays.asList());
            for(String key : nameSetServiceMap.keySet()) {
                Web3j web3j = nameSetServiceMap.get(key).getWeb3j();
                try {
                    String data = Numeric.toHexString(requestStr.getBytes());
                    EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(null, null, data), DefaultBlockParameterName.LATEST).sendAsync().get();
                    String value = ethCall.getResult();
                    JSONArray array = JSON.parseArray(value);
                    if (array.size() == 0) {
                        logger.warn("chain {} merchanId size is 0", key);
                        continue;
                    }

                    JSONArray resultJSON = array.getJSONArray(0);
                    for (Object obj : resultJSON) {
                        Bytes32 merchantId = new Bytes32(obj.toString().getBytes());
                        List<String> paramsList = new ArrayList<>();
                        paramsList.add(new String(merchantId.getValue()));
                        final CompletableFuture<Error> future = new CompletableFuture<>();
                        this.merchantWithdrawal(nameSetServiceMap.get(key), contructRequest(merchantWithdrawal, paramsList), future);
                        Error error = future.get(MAX_FUTURE_WAIT, TimeUnit.SECONDS);
                        if (error.getCode().equals(Error.OK.getCode())) {
                            try {
                                boolean verifySignOk = false;
                                boolean verifyProofOk = false;
                                JSONObject proofMerkle = this.getProofMerkle(nameSetServiceMap.get(key), error.getData());
                                logger.info("get getProofMerkle:{}", proofMerkle);

                                String hash = proofMerkle.getString("hash");

                                //拼接签名和公钥
                                JSONArray pubArr = proofMerkle.getJSONArray("pubs");
                                StringBuilder pubSb = new StringBuilder();
                                for(Object pub : pubArr) {
                                    pubSb.append(pub.toString()).append(";");
                                }

                                String pubStr = "";
                                if (pubSb.length() > 0) {
                                    pubStr = pubSb.substring(0, pubSb.length() - 1);
                                }

                                JSONArray signArr = proofMerkle.getJSONArray("signs");
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

                                boolean ok = this.verifySign(contructRequest(verifySign, signPubListParams));
                                if (!ok) {
                                    logger.error("verifySign failed");
                                } else {
                                    verifySignOk = true;
                                    //拼接proofs
                                    JSONArray proofArr = proofMerkle.getJSONArray("proofs");
                                    StringBuilder proofSb = new StringBuilder();
                                    for(Object proof : proofArr) {
                                        proofSb.append(proof.toString()).append(";");
                                    }

                                    String proofStr = "";
                                    if (proofSb.length() > 0) {
                                        proofStr = proofSb.substring(0, proofSb.length() - 1);
                                    }

                                    final CompletableFuture<Error> verifyFuture = new CompletableFuture<>();
                                    List<Object> verifyListParams = new ArrayList<>();
                                    verifyListParams.add(proofMerkle.getString("root"));
                                    verifyListParams.add(proofStr);
                                    verifyListParams.add(proofMerkle.getString("key"));
                                    verifyListParams.add(proofMerkle.getString("value"));
                                    //商户的id
                                    verifyListParams.add(new String(merchantId.getValue()));
                                    //要转移的资产
                                    JSONObject pfReq = JSON.parseObject(error.getData());
                                    verifyListParams.add(pfReq.getInteger("assets"));
                                    this.verifyAndDeposit(contructRequest(verifyProofAndDeposit, verifyListParams), verifyFuture);
                                    Error verifyError = verifyFuture.get(MAX_FUTURE_WAIT, TimeUnit.SECONDS);

                                    //释放冻结的资产
                                    final CompletableFuture<Error> confirmFuture = new CompletableFuture<>();
                                    List<Object> confirmParams = new ArrayList<>();
                                    confirmParams.add(new String(merchantId.getValue()));
                                    confirmParams.add(pfReq.getInteger("assets"));
                                    if (verifyError.getCode().equals(Error.OK.getCode())) {
                                        verifyProofOk = true;
                                    } else {
                                        verifyProofOk = false;
                                    }

                                    if (verifySignOk && verifyProofOk) {
                                        confirmParams.add(0);//验证成功
                                    } else {
                                        confirmParams.add(1);//验证失败
                                    }

                                    confirmWithdrawal(nameSetServiceMap.get(key), contructRequest(confirmWithdrawal, confirmParams), confirmFuture);
                                    Error confirmError = confirmFuture.get(MAX_FUTURE_WAIT, TimeUnit.SECONDS);
                                    if (confirmError.getCode().equals(Error.OK.getCode())) {
                                        logger.info("confirmWithdrawal success.code:{}", confirmError.getCode());
                                    } else {
                                        logger.error("confirmWithdrawal failed.code:{}", confirmError.getCode());
                                    }
                                }
                            } catch (RelayTaskException re) {
                                //要confirm
                                logger.error("RelayTaskException", re);
                            } catch (Exception e){

                            }

                        } else {
                            logger.error("merchantWithdrawal return code:{},merchantId:{}, chain:{}", error.getCode(), new String(merchantId.getValue()), key);
                        }
                    }

                } catch (Exception e) {
                    logger.error("exception", e);
                }
            }

        }

        private String contructRequest(String func, List<? extends Object> params) {
            JSONObject jsonObject = JSON.parseObject("{}");
            jsonObject.put("contract", contractName);
            jsonObject.put("func", func);
            jsonObject.put("version", contractVersion);
            jsonObject.put("params", params);
            return jsonObject.toJSONString();
        }

        /**
         * @desc 影子户出账
         * @param wcs
         * @param jsonStr
         * @param future
         * @return
         * @throws IOException
         */

        private boolean merchantWithdrawal(RMBServer.WCS wcs, String jsonStr, final CompletableFuture<Error> future) throws IOException {

            if (wcs == null) {
                logger.error("wcs is null");
                future.complete(Error.SERVICE_ERROR);
                return false;
            }

            logger.info("merchantWithdrawal to set:{},jsonStr:{}", wcs.getName(), jsonStr);
            Random r = new Random();
            BigInteger randomid = new BigInteger(250, r);
            BigInteger blockLimit = wcs.getWeb3j().getBlockNumberCache();
            RawTransaction rawTransaction = RawTransaction.createTransaction(randomid, RMBServer.gasPrice, RMBServer.gasLimit, blockLimit, "", Numeric.toHexString(jsonStr.getBytes()));
            String signMsg = Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, wcs.getCredentials()));
            Request request =  wcs.getWeb3j().ethSendRawTransaction(signMsg);
            request.setNeedTransCallback(true);
            request.setTransactionSucCallback(new TransactionSucCallback() {
                @Override
                public void onResponse(EthereumResponse ethereumResponse) {
                    //这里收到交易成功的通知
                    try {
                        TransactionReceipt transactionReceipt = RMBServer.objectMapper.readValue(ethereumResponse.getContent(), TransactionReceipt.class);
                        List<Meshchain.AssetsLogEventResponse> responses = Meshchain.getAssetsLogEvents(transactionReceipt);
                        if (responses.size() > 0) {
                            //按照业务合约解析event log,如果成功，则下一步向eth rpc接口获取merkle证明的数据
                            Meshchain.AssetsLogEventResponse response = responses.get(0);
                            logger.info("merchantWithdrawal onResponse data code:{}, availAssets:{}, frozenAssets:{}", response.code.getValue(),
                                    response.availAssets.getValue(), response.frozenAssets.getValue());
                            Error error = Error.ERROR_MAP.get(response.code.getValue().toString());
                            if (error.getCode().equals(Error.OK.getCode())) {
                                JSONObject retJson = JSON.parseObject("{}");
                                retJson.put("blockHash", transactionReceipt.getBlockHash());
                                retJson.put("transactionIndex", transactionReceipt.getTransactionIndexRaw());
                                retJson.put("assets", response.frozenAssets.getValue().toString());
                                error.setData(retJson.toJSONString());
                            }

                            future.complete(error);
                        } else {
                            future.complete(Error.CONTRACT_ERROR);
                        }
                    } catch (Exception e) {
                        logger.error("Exception", e);
                        future.complete(Error.CONTRACT_ERROR);
                    }

                }
            });

            Response response = request.send();
            //交易hash
            logger.info("merchantWithdrawal request:{}, response hash result:{}", jsonStr, response.getResult());
            return true;
        }


        /**
         * @desc 获取merkle证明的数据
         * @param wcs
         * @param jsonStr {"blockHash":"", "transactionIndex:""}
         * @return {"root":"", "proofs":[], "pubs":[], "value":""}
         * @throws IOException
         */
        private JSONObject getProofMerkle(RMBServer.WCS wcs, String jsonStr) throws Exception {
            if (wcs == null) {
                logger.error("wcs is null");
                return null;
            }

            logger.info("getProofMerkle jsonStr:{}", jsonStr);
            JSONObject reqJson = JSON.parseObject(jsonStr);
            if (!reqJson.containsKey("blockHash") || !reqJson.containsKey("transactionIndex")) {
                throw new Exception("getProofMerkle request:" + jsonStr + " error");
            }

            logger.info("getProofMerkle to set:{},jsonStr:{}", wcs.getName(), jsonStr);
            ProofMerkle proofMerkle = new ProofMerkle(reqJson.getString("blockHash"), reqJson.getString("transactionIndex"));
            Request<?, EthGetProofMerkle> request =  wcs.getWeb3j().ethGetProofMerkle(proofMerkle);

            EthGetProofMerkle response = request.send();

            if (response.getResult() == null) {
                throw new Exception("getProofMerkle result null:");
            }

            String proofStr = objectMapper.writeValueAsString(response.getResult());
            logger.info("getProofMerkle response hash result:{}", proofStr);

            JSONObject retJson = JSON.parseObject(proofStr);//拿到json结果,其中包含的数据{"root":"", "proofs":[], "pubs":[], "value":""}
            if (!retJson.containsKey("root") || !retJson.containsKey("proofs")
                    || !retJson.containsKey("pubs") || !retJson.containsKey("value") || !retJson.containsKey("signs")) {
                throw new Exception("getProofMerkle response error:" + retJson.toJSONString());
            }

            return retJson;
        }

        /**
         *
         * @param jsonStr {"contract":"Meshchain","func":"verifySign","version":"","params":[pubs,signs]}
         * @return
         */
        public boolean verifySign(String jsonStr) {
            logger.info("verifySign:{}", jsonStr);
            String data = Numeric.toHexString(jsonStr.getBytes());

            try {
                EthCall ethCall = hotWCS.getWeb3j().ethCall(Transaction.createEthCallTransaction(null, null, data), DefaultBlockParameterName.LATEST).sendAsync().get();
                String value = ethCall.getResult();
                JSONArray array = JSON.parseArray(value);
                if (array.size() == 0) {
                    logger.warn("verifySign get array size is 0");
                    return false;
                }

                int code = array.getInteger(0);
                logger.info("verifySign code:{}", code);
                return code == 0 ? true : false;
            } catch (Exception e) {
                logger.error("verifySign exection", e);
            }

            return false;
        }

        /**
         * @desc 验证交易的合法性，merkle树证明通过后，入账到热点账户
         * @param jsonStr 这里面包含了需要的证明数据
         * @param future
         * @return bool
         */
        private boolean verifyAndDeposit(String jsonStr, final CompletableFuture<Error> future) throws IOException {
            logger.info("verifyAndDeposit jsonStr:{}", jsonStr);
            Random r = new Random();
            BigInteger randomid = new BigInteger(250, r);
            BigInteger blockLimit = hotWCS.getWeb3j().getBlockNumberCache();
            RawTransaction rawTransaction = RawTransaction.createTransaction(randomid, RMBServer.gasPrice, RMBServer.gasLimit, blockLimit, "", Numeric.toHexString(jsonStr.getBytes()));
            String signMsg = Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, hotWCS.getCredentials()));
            Request request =  hotWCS.getWeb3j().ethSendRawTransaction(signMsg);
            request.setNeedTransCallback(true);
            request.setTransactionSucCallback(new TransactionSucCallback() {
                @Override
                public void onResponse(EthereumResponse ethereumResponse) {
                    //这里收到交易成功的通知
                    try {
                        TransactionReceipt transactionReceipt = RMBServer.objectMapper.readValue(ethereumResponse.getContent(), TransactionReceipt.class);
                        List<Meshchain.AssetsLogEventResponse> responses = Meshchain.getAssetsLogEvents(transactionReceipt);
                        if (responses.size() > 0) {
                            //按照业务合约解析event log,如果成功，则通知释放冻结
                            Meshchain.AssetsLogEventResponse response = responses.get(0);
                            logger.info("verifyAndDeposit onResponse code:{}", response.code.getValue());
                            Error error = Error.ERROR_MAP.get(response.code.getValue().toString());
                            future.complete(error);
                        } else {
                            future.complete(Error.CONTRACT_ERROR);
                        }
                    } catch (Exception e) {
                        logger.error("Exception", e);
                        future.complete(Error.CONTRACT_ERROR);
                    }

                }
            });

            Response response = request.send();
            //交易hash
            logger.info("verifyAndDeposit response hash result:{}", response.getResult());
            return true;
        }
    }

    private boolean confirmWithdrawal(RMBServer.WCS wcs, String jsonStr, final CompletableFuture<Error> future) throws IOException {
        logger.info("confirmWithdrawal jsonStr:{}", jsonStr);
        Random r = new Random();
        BigInteger randomid = new BigInteger(250, r);
        BigInteger blockLimit = wcs.getWeb3j().getBlockNumberCache();
        RawTransaction rawTransaction = RawTransaction.createTransaction(randomid, RMBServer.gasPrice, RMBServer.gasLimit, blockLimit, "", Numeric.toHexString(jsonStr.getBytes()));
        String signMsg = Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, wcs.getCredentials()));
        Request request =  wcs.getWeb3j().ethSendRawTransaction(signMsg);
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
                        logger.info("confirmWithdrawal onResponse data:{}", response.code.getValue());
                        Error error = Error.ERROR_MAP.get(response.code.getValue().toString());
                        future.complete(error);
                    } else {
                        future.complete(Error.CONTRACT_ERROR);
                    }
                } catch (Exception e) {
                    logger.error("Exception", e);
                    future.complete(Error.CONTRACT_ERROR);
                }

            }
        });

        Response response = request.send();
        //交易hash
        logger.info("confirmWithdrawal request:{}, response hash result:{}", jsonStr, response.getResult());
        return true;
    }
}

