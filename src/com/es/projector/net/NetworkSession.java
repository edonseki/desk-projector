package com.es.projector.net;

import java.net.*;
import java.util.Enumeration;
import java.util.Random;
import java.util.regex.Pattern;

public class NetworkSession {
    private static String sessionId;

    public void initSession(){
        NetworkSession.sessionId = generateProjectorId();
    }

    public String getSessionId() {
        return NetworkSession.sessionId;
    }

    public String getHostName(){
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "!";
        }
    }

    private String generateProjectorId(){
        String ipAddr = extractIp();

        if(ipAddr == null){
            return "-";
        }

        String[] parts = ipAddr.split(Pattern.quote("."));

        if(parts.length < 2){
            return "-";
        }

        return String.format("%s.%s-%s", parts[parts.length-2], parts[parts.length-1], generateRandomString(4));
    }

    public String extractIp(){
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!(addr instanceof Inet4Address)){
                        continue;
                    }
                    if(addr.getHostAddress().startsWith("127.") || addr.getHostAddress().startsWith("172.")){
                        continue;
                    }
                    ip = addr.getHostAddress();
                }
            }
        } catch (SocketException e) {
            return null;
        }
        return ip;
    }

    private String generateRandomString(int length) {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

    public String getHostFromSessionId(String sessionId){
        String[] sessionParts = sessionId.split(Pattern.quote("-"));

        if(sessionParts.length != 2){
            return null;
        }

        String ipAddr = extractIp();

        if(ipAddr == null){
            return null;
        }

        String[] parts = ipAddr.split(Pattern.quote("."));

        if(parts.length < 2){
            return null;
        }

        return String.format("%s.%s.%s", parts[0], parts[1], sessionParts[0]);
    }
}
