package net.katch0420.macebot.main.settings;

public enum Env {
    CLIENT,
    SERVER,
    COMMON;

    public static Env environment = Env.CLIENT;

    public static void updateEnv(Env env) {
        environment = env;
    }

    public static Env getEnv() {
        return environment;
    }
}
