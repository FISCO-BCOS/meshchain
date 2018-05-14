package org.bcos.proxy.protocol;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by fisco-dev on 17/8/27.
 */
public class BaseReq {
    private @Getter @Setter String func;
    private @Getter @Setter String contractName;
    private @Getter @Setter String version;
    private @Getter @Setter List<Object> params;
}
