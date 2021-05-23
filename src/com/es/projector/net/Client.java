package com.es.projector.net;

import com.es.projector.net.rmi.ShareService;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.regex.Pattern;

public class Client {
    public static Client client;

    private NetworkSession networkSession;

    private Client() {
        this.networkSession = new NetworkSession();
    }

    public static Client init() {
        if (Client.client == null) {
            Client.client = new Client();
        }
        return Client.client;
    }

    public ShareService connect(String projectorId) throws NotBoundException, RemoteException {
        String networkAddress = networkSession.getHostFromSessionId(projectorId);
        System.setProperty("java.rmi.server.hostname", networkAddress);
        String[] sessionData = projectorId.split(Pattern.quote("-"));

        Registry registry = LocateRegistry.getRegistry(networkAddress);
        return (ShareService) registry
                .lookup("//" + networkAddress + ":1099/ProjectorShareService"+sessionData[1]);
    }
}
