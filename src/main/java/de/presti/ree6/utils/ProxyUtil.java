package de.presti.ree6.utils;

import io.netty.bootstrap.ChannelFactory;
import io.netty.channel.socket.oio.OioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.*;

public class ProxyUtil {

    private static Proxy proxy;
    private static OioSocketChannel socketChannel;

    public static Proxy getProxy() {
        return proxy;
    }

    public static void setProxy(Proxy proxy) {
        ProxyUtil.proxy = proxy;
    }

    public static void setProxy(String ip, String port) {
        try {
            setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ip, Integer.parseInt(port))));
        } catch (Exception exception) {
            setProxy(null);
            exception.printStackTrace();
        }
    }

    public static ChannelFactory<OioSocketChannel> createProxyChannel() {

        if(getProxy() != null && getProxy() != Proxy.NO_PROXY) {
            if(socketChannel != null) {
                closeCurrentProxy();
            }
        }

        return () -> {
            if (getProxy() == null || getProxy() == Proxy.NO_PROXY) {
                return new OioSocketChannel(new Socket(Proxy.NO_PROXY));
            }
            final Socket sock = new Socket(getProxy());
            try {
                Method m = sock.getClass().getDeclaredMethod("getImpl");
                m.setAccessible(true);
                Object sd = m.invoke(sock);
                Method m1 = sd.getClass().getDeclaredMethod("setV4");
                m1.setAccessible(true);
                m1.invoke(sd);
                return socketChannel = new OioSocketChannel(sock);
            }
            catch (Exception ex2) {
                throw new RuntimeException("Failed to create socks 4 proxy!", new Exception());
            }
        };
    }

    public static void closeCurrentProxy() {
        socketChannel.close();
    }

    public static String getProxies() throws Exception {
        HttpURLConnection conn = (HttpURLConnection)(new URL("https://api.proxyscrape.com/v2/?request=getproxies&protocol=socks4&timeout=150&country=all")).openConnection();
        conn.setDoInput(true);
        BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String resp = "";
        for (String read; (read = bf.readLine()) != null;) {
            resp = resp + read + "\n";
        }
        if (resp.endsWith("\n")) resp = resp.substring(0, resp.length() - 1);

        bf.close();
        return resp;
    }

}