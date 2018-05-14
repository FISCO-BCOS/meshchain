package org.bcos.proxy.task;

/**
 * Created by fisco fisco-dev on 17/9/27.
 */

import org.bcos.proxy.protocol.UserInfo;
import org.bcos.proxy.server.HttpServer;
import org.bcos.proxy.util.Error;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RelayTask {

    private static Logger logger = LoggerFactory.getLogger(RelayTask.class);

    private Timer timer;
    private TimeTask timeTask;
    private long delayMilliseconds;
    private String hotAccountServiceChainName;
    private List<String> hotAcountList;

    /**
     * map set service
     */
    private ConcurrentHashMap<String, HttpServer.WCS> nameSetServiceMap = new ConcurrentHashMap();

    public RelayTask(long delayMilliseconds, ConcurrentHashMap<String, HttpServer.WCS> nameSetServiceMap, String hotAccountServiceChainName, List<String> hotAcountList) {
        this.timer = new Timer();
        this.delayMilliseconds = delayMilliseconds;
        this.timeTask = new TimeTask();
        this.nameSetServiceMap = nameSetServiceMap;
        this.hotAccountServiceChainName = hotAccountServiceChainName;
        this.hotAcountList = hotAcountList;
    }

    public void schedule() {
        this.timer.schedule(this.timeTask, this.delayMilliseconds, this.delayMilliseconds);
    }


    public class TimeTask extends TimerTask {

        @Override
        public void run() {
            logger.debug("RelayTask start");
            if (hotAccountServiceChainName == null || "".equals(hotAccountServiceChainName)) {
                logger.error("hotAccountServiceChainName is null or empty.");
                return;
            }

            if (!nameSetServiceMap.containsKey(hotAccountServiceChainName)) {
                logger.error("not found {} in nameSetServiceMap.", hotAccountServiceChainName);
                return;
            }


            if (hotAcountList == null || hotAcountList.size() == 0) {
                logger.warn("hotAcountList is null or empty");
                return;
            }

            HttpServer.WCS toWCS = nameSetServiceMap.get(hotAccountServiceChainName);

            for (String hotAccount : hotAcountList) {

                UserInfo toUserInfo = null;
                try {
                    toUserInfo = HttpServer.getAccountInfoByName(nameSetServiceMap.get(hotAccountServiceChainName), hotAccount, false);
                } catch (Exception e) {
                    logger.error("get exception in queryUserInfo", e);
                    continue;
                }

                if (toUserInfo == null || "".equals(toUserInfo.getUid()) || toUserInfo.getIdentity() != HttpServer.HOT_ACCOUNT) {
                    logger.error("account name:{} not found as hot account", hotAccount);
                    continue;
                }

                for (String from : nameSetServiceMap.keySet()) {
                    if (from.equals(hotAccountServiceChainName)) {
                        continue;
                    }

                    HttpServer.WCS fromWCS = nameSetServiceMap.get(from);
                    try {
                        UserInfo fromUserInfo = HttpServer.getAccountInfoByName(fromWCS, hotAccount, true);
                        if (fromUserInfo == null || "".equals(fromUserInfo.getUid()) || fromUserInfo.getIdentity() != HttpServer.SUB_HOT_ACCOUNT) {
                            logger.error("from user info is null or sub hot account:{} uid:{}, identity:{} not exist.", hotAccount,
                                    fromUserInfo == null ? "" : fromUserInfo.getUid(), fromUserInfo == null ? "" : fromUserInfo.getIdentity());
                            continue;
                        }

                        int availAssets = fromUserInfo.getAvailAssets();
                        if (availAssets == 0) {
                            logger.info("from:{}, account:{} availAssets is 0.", from, hotAccount);
                            continue;
                        }

                        List<Object> params = new ArrayList<>();
                        params.add(fromUserInfo.getUid());//from uid
                        params.add(toUserInfo.getUid());//to uid
                        params.add(availAssets);

                        JSONObject jsonObject = JSON.parseObject("{}");
                        jsonObject.put("contract", HttpServer.CONTRACT_NAME);
                        jsonObject.put("version", "");
                        jsonObject.put("params", params);
                        jsonObject.put("func", "transferInterChainByFrom");

                        logger.debug("sendChainMessage:{}", jsonObject.toJSONString());
                        final CompletableFuture<HttpServer.Rsp> transferFromFuture = new CompletableFuture<>();
                        HttpServer.sendChainMessage(fromWCS, jsonObject.toJSONString(), transferFromFuture);
                        HttpServer.Rsp rsp = transferFromFuture.get(HttpServer.FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
                        if (rsp.getError().getCode() != Error.OK.getCode()) {
                            //get from error
                            logger.error("transferInterChainByFrom err.code:{}, msg:{}", Integer.parseInt(rsp.getError().getCode()), rsp.getError().getDescription());
                            continue;
                        }


                        JSONObject jsonMerkleProof = HttpServer.getProofMerkle(fromWCS, rsp.getData());
                        boolean verifyOk = false;

                        try {
                            verifyOk = HttpServer.verifySign(toWCS, jsonMerkleProof);
                            if (!verifyOk) {
                                JSONObject jsonRsp = JSON.parseObject(rsp.getData());
                                final CompletableFuture<HttpServer.Rsp> cancelFuture = new CompletableFuture<>();
                                HttpServer.transferInterChainCancel(fromWCS, jsonRsp.getBigInteger("transferId"), cancelFuture);
                                HttpServer.Rsp cancelRsp = cancelFuture.get(HttpServer.FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
                                if (cancelRsp.getError().getCode() != Error.OK.getCode()) {
                                    logger.error("transferInterChainCancel error.transfer id:{}", jsonRsp.getBigInteger("transferId"));
                                }
                            }
                        } catch (Exception e) {
                            logger.error("verifySign exception.", e);
                        }

                        if (!verifyOk) {
                            logger.error("verifyOk is false.from:{}, account:{}", from, hotAccount);
                            continue;
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
                        transferInterChainByToParamList.add(fromUserInfo.getUid());//from
                        transferInterChainByToParamList.add(toUserInfo.getUid());//to
                        transferInterChainByToParamList.add(availAssets);//assets

                        jsonObject.put("func", "transferInterChainByTo");
                        jsonObject.put("params", transferInterChainByToParamList);

                        final CompletableFuture<HttpServer.Rsp> transferToFuture = new CompletableFuture<>();
                        HttpServer.sendChainMessage(toWCS, jsonObject.toJSONString(), transferToFuture);
                        HttpServer.Rsp toRsp = transferToFuture.get(HttpServer.FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
                        if (toRsp.getError().getCode() != Error.OK.getCode()) {
                            //get to error, cancel and return now.
                            JSONObject jsonRsp = JSON.parseObject(rsp.getData());
                            final CompletableFuture<HttpServer.Rsp> cancelFuture = new CompletableFuture<>();
                            HttpServer.transferInterChainCancel(fromWCS, jsonRsp.getBigInteger("transferId"), cancelFuture);
                            HttpServer.Rsp cancelRsp = cancelFuture.get(HttpServer.FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
                            if (cancelRsp.getError().getCode() != Error.OK.getCode()) {
                                logger.error("transferInterChainCancel error.transfer id:{}", jsonRsp.getBigInteger("transferId"));
                            }

                            logger.error("transferInterChainByTo err.code:{}, msg:{}", Integer.parseInt(toRsp.getError().getCode()), toRsp.getError().getDescription());
                            continue;
                        }

                        JSONObject jsonRsp = JSON.parseObject(rsp.getData());
                        final CompletableFuture<HttpServer.Rsp> confirmFuture = new CompletableFuture<>();
                        HttpServer.transferInterChainConfirm(fromWCS, jsonRsp.getBigInteger("transferId"), confirmFuture);
                        HttpServer.Rsp confirmRsp = confirmFuture.get(HttpServer.FUTURE_MAX_WAIT_SECOND, TimeUnit.SECONDS);
                        if (confirmRsp.getError().getCode() != Error.OK.getCode()) {
                            //confirm error, cancel and return now.
                            logger.error("transferInterChainConfirm error.transfer id:{}, jsonTransactionRsp:{}", jsonRsp.getBigInteger("transferId"), jsonTransactionRsp.toJSONString());
                            logger.error("transferInterChainConfirm code:{}, msg:{}", Integer.parseInt(confirmRsp.getError().getCode()), confirmRsp.getError().getDescription());
                            continue;
                        }

                        JSONObject jsonFromRsp = JSON.parseObject(rsp.getData());
                        JSONObject jsonToRsp = JSON.parseObject(toRsp.getData());

                        logger.info("transfer success.from:{}, account name:{}, from_transfer_id:{}, to_transfer_id:{}, assets:{}", from, hotAccount,
                                jsonFromRsp.getString("transferId"), jsonToRsp.getString("transferId"), availAssets);
                    } catch (Exception e) {
                        logger.error("exception", e);
                    }

                }
            }

        }
    }
}

