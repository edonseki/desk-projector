package com.es.projector.net;

import com.es.projector.net.rmi.ShareService;
import com.es.projector.net.rmi.ShareServiceImpl;

import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.regex.Pattern;

public class Server {
    public static Server server;
    private NetworkSession networkSession;

    static ShareService shareService = new ShareServiceImpl();

    private String lastSessionId;
    private Registry rmiRegistry;

    private Server() {
        this.networkSession = new NetworkSession();
    }

    public static Server init() {
        if (Server.server == null) {
            Server.server = new Server();
        }
        return Server.server;
    }

    public ShareService start(String sessionId) throws RemoteException {
        String[] sessionData = sessionId.split(Pattern.quote("-"));
        if (sessionData.length != 2) {
            return null;
        }
        lastSessionId = sessionId;
        String networkAddress = this.networkSession.extractIp();
        System.setProperty("java.rmi.server.hostname", networkAddress);

        if (this.rmiRegistry == null) {
            this.rmiRegistry = LocateRegistry.createRegistry(1099);
        }

        ShareService stub = (ShareService) UnicastRemoteObject
                .exportObject(shareService, 1099);

        this.rmiRegistry.rebind("//" + networkAddress + ":1099/ProjectorShareService" + sessionData[1], stub);
        return stub;
    }

    public void stop() {
        try {
            String[] sessionData = this.lastSessionId.split(Pattern.quote("-"));
            String networkAddress = this.networkSession.extractIp();
            UnicastRemoteObject.unexportObject(shareService, true);
            this.rmiRegistry.unbind("//" + networkAddress + ":1099/ProjectorShareService" + sessionData[1]);
            shareService = new ShareServiceImpl();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
