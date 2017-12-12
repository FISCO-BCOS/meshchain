package org.bcos.proxy.protocol;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by fisco-dev on 17/8/27.
 */
public class UserReq extends BaseReq {
    private @Getter @Setter String uid;
}
