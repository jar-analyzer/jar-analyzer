package me.n1ar4.rasp.agent.core;

import com.google.gson.Gson;
import me.n1ar4.rasp.agent.ent.UploadMessage;
import me.n1ar4.rasp.agent.utils.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Upload {
    private static void uploadInternal(int type, StackTraceElement[] ste, Object input) {
        Path path = Paths.get("logs");
        try {
            UploadMessage um = getUploadMessage(type, ste, input);
            String json;
            try {
                Gson gson = new Gson();
                json = gson.toJson(um);
            } catch (Exception ex) {
                Log.error("JSON序列化错误: %s", ex.toString());
                return;
            }
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            Path finalPath = path.resolve(Paths.get("rasp.log"));
            if (!Files.exists(finalPath)) {
                Files.createFile(finalPath);
            }

            System.out.println("### rasp found vulnerability ###");
            System.out.println("sink: " + um.getStacks().get(um.getStacks().size() - 1));
            System.out.println("input: " + um.getInput());
            System.out.println("### details are in logs/rasp.log ###");

            Files.write(finalPath, json.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            try {
                if (!Files.exists(path)) {
                    Files.createDirectory(path);
                }
                Path finalPath = path.resolve(Paths.get("rasp-err.log"));
                if (!Files.exists(finalPath)) {
                    Files.createFile(finalPath);
                }
                Files.write(finalPath, e.toString().getBytes());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            Log.warn("上传信息失败: %s", e.toString());
        }
    }

    private static UploadMessage getUploadMessage(int type, StackTraceElement[] ste, Object input) {
        UploadMessage um = new UploadMessage();
        um.setVulType(type);
        switch (type) {
            case 1:
                um.setVulTypeName("RCE");
                break;
            case 2:
                um.setVulTypeName("JNDI");
                break;
            case 3:
                um.setVulTypeName("Deserialization");
                break;
            default:
                um.setVulTypeName("UNKNOWN");
                break;
        }
        um.setInput(input);

        List<String> stacks = new ArrayList<>();
        for (int i = ste.length - 1; i > 0; i--) {
            String className = ste[i].getClassName();
            String methodName = ste[i].getMethodName();
            String out = String.format("%s#%s", className, methodName);
            stacks.add(out);
        }
        um.setStacks(stacks);
        return um;
    }

    public static void uploadObject(int type, StackTraceElement[] ste, Object input) {
        input = input.getClass().getName() + "@" + input.hashCode();
        uploadInternal(type, ste, input);
    }

    public static void uploadString(int type, StackTraceElement[] ste, String input) {
        uploadInternal(type, ste, input);
    }

    public static void uploadStringList(int type, StackTraceElement[] ste, List<String> input) {
        uploadInternal(type, ste, input);
    }
}
