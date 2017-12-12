package org.bcos.proxy.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fisco-dev on 17/8/27.
 */
public enum Error {
    /*
		合约的errcode:
		10000:热点账户已存在
		10001:用户已存在
		10002:用户状态不正常
		10003:用户不存在
		10004:热点账户不存在
		10005:热点账户状态不正常
		10006:用户余额不足
		10007:释放冻结金额不合法
		10008:热点账户余额为0
		10009:没有可释放的金额
	*/

    OK("0","ok"),
    SYSTEM_ERROR("999999","系统异常"),
    ARGS_ERROR("990001", "参数错误"),
    CONTRACT_ERROR("990002", "合约错误"),
    USER_REGISTER_ERROR("990003", "注册用户错误"),
    SERVICE_ERROR("990004", "查找service错误"),
    CONTRACT_MERCHANT_EXIST("10000", "热点账户已存在"),
    CONTRACT_USER_EXIST("10001", "用户已存在"),
    CONTRACT_USER_STATUS_ERROR("10002", "用户状态不正常"),
    CONTRACT_USER_NOT_EXIST("10003", "用户不存在"),
    CONTRACT_MERCHANT_NOT_EXIST("10004", "热点账户不存在"),
    CONTRACT_MERCHANT_STATUS_ERROR("10005", "热点账户状态不正常"),
    CONTRACT_USER_BALANCE_ERROR("10006", "用户余额不足"),
    CONTRACT_FREE_FROZEN_ERROR("10007", "释放冻结金额不合法"),
    CONTRACT_MERCHANT_BALANCE_ZERO_ERROR("10008", "热点账户余额为0"),
    CONTRACT_NO_FREE_FROZEN_ERROR("10009", "没有可释放的金额"),
    CONTRACT_NO_MERCHANT_ERROR("1001O", "非热点账户"),
    CONTRACT_NO_SUB_MERCHANT_ERROR("10011", "非影子户"),
    CONTRACT_TRIE_PROOF_ERROR("10012", "trie proof验证失败"),
    CONTRACT_SUB_MERCHANT_NOT_EXIST_ERROR("10013", "影子户不存在"),
    CONTRACT_SUB_MERCHANT_STATUS_ERROR("10014", "影子户状态不正常"),
    CONTRACT_SUB_MERCHANT_EXIST_ERROR("10015", "影子户已存在"),
    CONTRACT_PUBKEY_NOT_EXIST_ERROR("10016", "公钥不存在"),
    CONTRACT_VERIFY_SIGN_ERROR("10017", "验证签名失败"),
    CONTRACT_ASSETS_ERROR("10018", "金额非法")
    ;

    public static Map<String, Error> ERROR_MAP;

    static {
        ERROR_MAP = new HashMap<>();
        ERROR_MAP.put("0", OK);
        ERROR_MAP.put("999999", SYSTEM_ERROR);
        ERROR_MAP.put("990001", ARGS_ERROR);
        ERROR_MAP.put("990002", CONTRACT_ERROR);
        ERROR_MAP.put("990003", USER_REGISTER_ERROR);
        ERROR_MAP.put("990004", SERVICE_ERROR);
        ERROR_MAP.put("10000", CONTRACT_MERCHANT_EXIST);
        ERROR_MAP.put("10001", CONTRACT_USER_EXIST);
        ERROR_MAP.put("10002", CONTRACT_USER_STATUS_ERROR);
        ERROR_MAP.put("10003", CONTRACT_USER_NOT_EXIST);
        ERROR_MAP.put("10004", CONTRACT_MERCHANT_NOT_EXIST);
        ERROR_MAP.put("10005", CONTRACT_MERCHANT_STATUS_ERROR);
        ERROR_MAP.put("10006", CONTRACT_USER_BALANCE_ERROR);
        ERROR_MAP.put("10007", CONTRACT_FREE_FROZEN_ERROR);
        ERROR_MAP.put("10008", CONTRACT_MERCHANT_BALANCE_ZERO_ERROR);
        ERROR_MAP.put("10009", CONTRACT_NO_FREE_FROZEN_ERROR);
        ERROR_MAP.put("10010", CONTRACT_NO_MERCHANT_ERROR);
        ERROR_MAP.put("10011", CONTRACT_NO_SUB_MERCHANT_ERROR);
        ERROR_MAP.put("10012", CONTRACT_TRIE_PROOF_ERROR);
        ERROR_MAP.put("10013", CONTRACT_SUB_MERCHANT_NOT_EXIST_ERROR);
        ERROR_MAP.put("10014", CONTRACT_SUB_MERCHANT_STATUS_ERROR);
        ERROR_MAP.put("10015", CONTRACT_SUB_MERCHANT_EXIST_ERROR);
        ERROR_MAP.put("10016", CONTRACT_PUBKEY_NOT_EXIST_ERROR);
        ERROR_MAP.put("10017", CONTRACT_VERIFY_SIGN_ERROR);
        ERROR_MAP.put("10018", CONTRACT_ASSETS_ERROR);
    }

    private String code;
    private String description;
    private String data;

    Error(String code, String description, String data) {
        this.code = code;
        this.description = description;
        this.data = data;
    }

    Error(String code, String description) {
        this(code, description, null);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
