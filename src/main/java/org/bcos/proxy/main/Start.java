package org.bcos.proxy.main;

import org.bcos.proxy.config.Config;
import org.bcos.proxy.server.HttpServer;
import org.eclipse.jetty.server.Server;

/**
 * Created by fisco-dev on 17/11/8.
 */
public class Start {
    public static void main(String []args) throws Exception {

        Config config = Config.getConfig();
        String serverStr = System.getProperty("server");

        if (serverStr != null && serverStr.equals("http")) {
            int port = System.getProperty("port") == null? 8080 : Integer.parseInt(System.getProperty("port"));
            Server server = new Server(port);
            server.setHandler(new HttpServer(config));
            server.start();
            server.join();
        } else {
            System.out.println("not support server.try -Dserver=http or -Dserver=rmb(only for webank)");
        }
    }
}
