package com.es.projector.net;

import com.es.projector.common.Constants;
import com.es.projector.net.rmi.ShareService;
import com.es.projector.net.rmi.ShareServiceImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.regex.Pattern;

public class Server {
    public static Server server;

    static ShareService shareService = new ShareServiceImpl();

    private String lastSessionId;
    private Registry rmiRegistry;

    private Server() {
    }

    public static Server init() {
        if (Server.server == null) {
            Server.server = new Server();
        }
        return Server.server;
    }

    public ShareService start(String sessionId) throws RemoteException {
        String sessionKey = this.extractSessionKey(sessionId);
        if (sessionKey == null) {
            return null;
        }
        lastSessionId = sessionId;
        String networkAddress = NetworkSession.instance().extractIp();
        System.setProperty("java.rmi.server.hostname", networkAddress);

        if (this.rmiRegistry == null) {
            this.rmiRegistry = LocateRegistry.createRegistry(1099);
        }

        ShareService stub = (ShareService) UnicastRemoteObject
                .exportObject(shareService, 1099);

        this.rmiRegistry.rebind(String.format(Constants.Stream.RMI_REGISTRY, networkAddress, sessionKey), stub);
        return stub;
    }

    public void stop() {
        try {
            String networkAddress = NetworkSession.instance().extractIp();
            UnicastRemoteObject.unexportObject(shareService, true);
            this.rmiRegistry.unbind(String.format(Constants.Stream.RMI_REGISTRY, networkAddress, this.extractSessionKey(this.lastSessionId)));
            shareService = new ShareServiceImpl();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    private String extractSessionKey(String sessionId) {
        String[] sessionData = sessionId.split(Pattern.quote("-"));
        if (sessionData.length != 2) {
            return null;
        }
        return sessionData[1];
    }
}
