package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.jar.analyzer.env.Const;

public class Logo {
    public static void print() {
        System.out.println("     ____.                 _____                .__                      \n" +
                "    |    |____ _______    /  _  \\   ____ _____  |  | ___.__. ___________ \n" +
                "    |    \\__  \\\\_  __ \\  /  /_\\  \\ /    \\\\__  \\ |  |<   |  |/ __ \\_  __ \\\n" +
                "/\\__|    |/ __ \\|  | \\/ /    |    \\   |  \\/ __ \\|  |_\\___  \\  ___/|  | \\/\n" +
                "\\________(____  /__|    \\____|__  /___|  (____  /____/ ____|\\___  >__|   \n" +
                "              \\/                \\/     \\/     \\/     \\/         \\/       ");
        System.out.printf("Jar Analyzer %s by 4ra1n (https://github.com/4ra1n)\n", Const.version);
        System.out.printf("Project Address: %s\n\n", Const.projectUrl);
    }
}
