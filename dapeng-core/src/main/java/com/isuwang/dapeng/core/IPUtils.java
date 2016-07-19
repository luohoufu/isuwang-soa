package com.isuwang.dapeng.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;

/**
 * IP Utils
 *
 * @author craneding
 * @date 16/1/19
 */
public class IPUtils {

    static final Logger LOGGER = LoggerFactory.getLogger(IPUtils.class);
    static InetAddress inetAddress = null;

    static {
        try {
            inetAddress = InetAddress.getLocalHost();

            if (inetAddress.getHostAddress() == null || inetAddress.getHostAddress().equals("127.0.0.1")) {
                NetworkInterface ni = NetworkInterface.getByName("bond0");
                if (ni == null)
                    throw new RuntimeException("wrong with get ip");

                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress nextElement = ips.nextElement();
                    if (nextElement.getHostAddress().equals("127.0.0.1") || nextElement instanceof Inet6Address || nextElement.getHostAddress().contains(":"))
                        continue;
                    inetAddress = nextElement;
                }
            }
        } catch (UnknownHostException e) {
            LOGGER.error("InetAddress.getLocalHost error.", e);
        } catch (SocketException e) {
            LOGGER.error("InetAddress.getLocalHost error.", e);
        }
    }

    public static String localIp() {
        return (SoaSystemEnvProperties.SOA_LOCAL_HOST_NAME != null && !SoaSystemEnvProperties.SOA_LOCAL_HOST_NAME.trim().isEmpty()) ? SoaSystemEnvProperties.SOA_LOCAL_HOST_NAME : inetAddress.getHostAddress();
    }
}
