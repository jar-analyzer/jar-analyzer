package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.jar.analyzer.utils.ColorUtil;
import me.n1ar4.jar.analyzer.utils.IOUtils;

import java.io.InputStream;

public class Logo {
    public static void print() {
        System.out.println("     ____.               _____                .__                              \n" +
                "    |    |____ _______  /  _  \\   ____ _____  |  | ___.__.________ ___________ \n" +
                "    |    \\__  \\\\_  __ \\/  /_\\  \\ /    \\\\__  \\ |  |<   |  |\\___   // __ \\_  __ \\\n" +
                "/\\__|    |/ __ \\|  | \\/    |    \\   |  \\/ __ \\|  |_\\___  | /    /\\  ___/|  | \\/\n" +
                "\\________(____  /__|  \\____|__  /___|  (____  /____/ ____|/_____ \\\\___  >__|   \n" +
                "              \\/              \\/     \\/     \\/     \\/           \\/    \\/       ");
        System.out.printf("Jar Analyzer %s by 4ra1n (https://github.com/4ra1n)\n", Const.version);
        System.out.printf("Project Address: %s\n\n", Const.projectUrl);

        InputStream is = Logo.class.getClassLoader().getResourceAsStream("thanks.txt");
        if (is != null) {
            try {
                byte[] data = IOUtils.readAllBytes(is);
                String a = new String(data);
                String[] splits = a.split("\n");
                if (splits.length > 1) {
                    System.out.println(ColorUtil.green("感谢以下贡献者（按照贡献量排序）"));
                }
                for (String s : splits) {
                    if (s.endsWith("\r")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    String[] temp = s.split(" ");

                    String url = temp[temp.length - 1];
                    StringBuilder name = new StringBuilder();
                    for (int i = 0; i < temp.length - 1; i++) {
                        name.append(temp[i]);
                        name.append(" ");
                    }

                    System.out.println(" -> " + ColorUtil.blue(name.toString().trim()) +
                            " " + ColorUtil.yellow(url));
                }
                System.out.println();
            } catch (Exception ignored) {
            }
        }
    }
}
