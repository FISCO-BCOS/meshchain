package org.bcos.proxy.protocol;

import lombok.Data;

/**
 * Created by fisco-dev on 18/3/23.
 */

public @Data
class UserInfo {
    private String uid;
    private int availAssets;
    private int unAvailAssets;
    private String name;
    private int identity;

    public UserInfo(String uid, int availAssets, int unAvailAssets, int identity, String name) {
        this.uid = uid;
        this.availAssets = availAssets;
        this.unAvailAssets = unAvailAssets;
        this.identity = identity;
        this.name = name;
    }
}
