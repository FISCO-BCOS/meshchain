package org.bcos.proxy.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fisco-dev on 17/8/27.
 */
public enum Error {

    OK("0","ok"),
    SYSTEM_ERROR("999999","system error"),
    ARGS_ERROR("990001", "args error"),
    CONTRACT_ERROR("990002", "contract error"),
    USER_REGISTER_ERROR("990003", "register user error"),
    SERVICE_ERROR("990004", "get set service error"),
    CONTRACT_HOT_ACCOUNT_EXIST("10000", "hot account existed"),
    CONTRACT_USER_EXIST("10001", "account existed"),
    CONTRACT_USER_STATUS_ERROR("10002", "user status error"),
    CONTRACT_USER_NOT_EXIST("10003", "account not exist"),
    CONTRACT_MERCHANT_NOT_EXIST("10004", "hot account not exist"),
    CONTRACT_MERCHANT_STATUS_ERROR("10005", "hot account status error"),
    CONTRACT_USER_BALANCE_ERROR("10006", "blance error"),
    CONTRACT_FREE_FROZEN_ERROR("10007", "asset error"),
    CONTRACT_MERCHANT_BALANCE_ZERO_ERROR("10008", "blance zero error"),
    CONTRACT_NO_FREE_FROZEN_ERROR("10009", "no unavailable asset error"),
    CONTRACT_NOT_HOT_ACCOUNT_ERROR("1001O", "not hot account error"),
    CONTRACT_NOT_SUB_HOT_ACCOUNT_ERROR("10011", "not sub hot account error"),
    CONTRACT_TRIE_PROOF_ERROR("10012", "trie proof verify error"),
    CONTRACT_SUB_HOT_ACCOUNT_NOT_EXIST_ERROR("10013", "sub hot account not exist"),
    ONTRACT_SUB_HOT_ACCOUNT_EXIST_ERROR("10015", "sub hot account existed"),
    CONTRACT_PUBKEY_NOT_EXIST_ERROR("10016", "public key error"),
    CONTRACT_VERIFY_SIGN_ERROR("10017", "verify sign error"),
    CONTRACT_ASSETS_ERROR("10018", "asset not match error"),
    CONTRACT_TRANSACTION_NOT_EXIST_ERROR("10019", "transaction not existed"),
    CONTRACT_HOT_ACCOUNT_EXIST_ERROR("10020", "hot account existed")
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
        ERROR_MAP.put("10000", CONTRACT_HOT_ACCOUNT_EXIST);
        ERROR_MAP.put("10001", CONTRACT_USER_EXIST);
        ERROR_MAP.put("10002", CONTRACT_USER_STATUS_ERROR);
        ERROR_MAP.put("10003", CONTRACT_USER_NOT_EXIST);
        ERROR_MAP.put("10004", CONTRACT_MERCHANT_NOT_EXIST);
        ERROR_MAP.put("10005", CONTRACT_MERCHANT_STATUS_ERROR);
        ERROR_MAP.put("10006", CONTRACT_USER_BALANCE_ERROR);
        ERROR_MAP.put("10007", CONTRACT_FREE_FROZEN_ERROR);
        ERROR_MAP.put("10008", CONTRACT_MERCHANT_BALANCE_ZERO_ERROR);
        ERROR_MAP.put("10009", CONTRACT_NO_FREE_FROZEN_ERROR);
        ERROR_MAP.put("10010", CONTRACT_NOT_HOT_ACCOUNT_ERROR);
        ERROR_MAP.put("10011", CONTRACT_NOT_SUB_HOT_ACCOUNT_ERROR);
        ERROR_MAP.put("10012", CONTRACT_TRIE_PROOF_ERROR);
        ERROR_MAP.put("10013", CONTRACT_SUB_HOT_ACCOUNT_NOT_EXIST_ERROR);
        ERROR_MAP.put("10015", ONTRACT_SUB_HOT_ACCOUNT_EXIST_ERROR);
        ERROR_MAP.put("10016", CONTRACT_PUBKEY_NOT_EXIST_ERROR);
        ERROR_MAP.put("10017", CONTRACT_VERIFY_SIGN_ERROR);
        ERROR_MAP.put("10018", CONTRACT_ASSETS_ERROR);
        ERROR_MAP.put("10019", CONTRACT_TRANSACTION_NOT_EXIST_ERROR);
        ERROR_MAP.put("10020", CONTRACT_HOT_ACCOUNT_EXIST_ERROR);
    }

    private String code;
    private String description;

    Error(String code, String description, String data) {
        this.code = code;
        this.description = description;
    }

    Error(String code, String description) {
        this(code, description, null);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
