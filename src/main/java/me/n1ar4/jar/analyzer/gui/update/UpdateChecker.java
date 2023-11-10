package me.n1ar4.jar.analyzer.gui.update;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.gui.MainForm;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

public class UpdateChecker {
    public static final String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36";

    public static void checkUpdate() {
        MainForm instance = MainForm.getInstance();
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Const.checkUpdateUrl)
                    .addHeader("User-Agent", ua)
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

                        Object obj = JSON.parse(body);
                        if (!(obj instanceof JSONObject)) {
                            return;
                        }
                        JSONObject jsonObject = (JSONObject) obj;
                        String tagName = (String) jsonObject.get("tag_name");
                        String name = (String) jsonObject.get("name");
                        String ver;
                        if (tagName != null && !tagName.isEmpty()) {
                            ver = tagName;
                        } else if (name != null && !name.isEmpty()) {
                            ver = name;
                        } else {
                            LogUtil.log("check update api fail");
                            return;
                        }
                        LogUtil.log("latest: " + ver);
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
