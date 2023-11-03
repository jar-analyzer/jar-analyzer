package me.n1ar4.jar.analyzer.gui.update;

import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.gui.MainForm;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

public class UpdateChecker {
    public static void checkUpdate() {
        MainForm instance = MainForm.getInstance();
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Const.checkUpdateUrl)
                    .addHeader("Connection", "close")
                    .build();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    // ignored
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    try {
                        if (response.body() == null) {
                            return;
                        }
                        String body = response.body().string();
                        String ver = body.split("\"tag_name\":")[1].split(",")[0];
                        ver = ver.substring(1, ver.length() - 1);
                        if (!ver.equals(Const.version)) {
                            String output;
                            output = String.format("New Version!\n%s: %s\n%s: %s\n%s",
                                    "Current Version", Const.version,
                                    "Latest Version", ver,
                                    "https://github.com/jar-analyzer/jar-analyzer");
                            JOptionPane.showMessageDialog(instance.getMasterPanel(), output);

                        }
                    } catch (Exception ignored) {
                    }
                }
            });
        }).start();
    }
}
