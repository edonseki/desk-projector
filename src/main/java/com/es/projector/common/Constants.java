package com.es.projector.common;

public class Constants {
    public static class Application {
        public final static String NAME = "Projector";
        public final static String VERSION = "1.0.2";
        public final static String AUTHOR = "Edon Sekiraqa";
    }
    public static class Stream{
        public final static int REFRESH_RATE = 15;
        public final static String RMI_REGISTRY = "//%s:1099/ProjectorShareService%s";
    }
    public static class Texts{
        public final static String SERVER_NOT_STARTED = "The sharing server couldn't start!";
        public static final String UNABLE_TO_GENERATE_SESSION =  "Unable to generate session id. The application will close.";
    }
}
