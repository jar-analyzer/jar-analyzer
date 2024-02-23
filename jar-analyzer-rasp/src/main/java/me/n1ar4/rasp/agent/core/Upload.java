package me.n1ar4.rasp.agent.core;

import com.google.gson.Gson;
import me.n1ar4.rasp.agent.ent.UploadMessage;
import me.n1ar4.rasp.agent.ent.Vul;
import me.n1ar4.rasp.agent.utils.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Upload {
    private static void uploadInternal(int type, StackTraceElement[] ste, Object input) {
        try {
            UploadMessage um = new UploadMessage();
            um.setVulType(type);

            Field[] fields = Vul.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.get(null).equals(type)) {
                    um.setVulTypeName(field.getName());
                }
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

            String json;
            try {
                Gson gson = new Gson();
                json = gson.toJson(um);
            } catch (Exception ex) {
                Log.error("JSON序列化错误: %s", ex.toString());
                return;
            }

            Path log = Paths.get("logs");
            if (!Files.exists(log)) {
                Files.createDirectory(log);
            }
            Path finalPath = log.resolve(Paths.get("rasp.log"));
            if (!Files.exists(finalPath)) {
                Files.createFile(finalPath);
            }

            System.out.println("jar-analyzer-rasp found vulnerability");
            int i = 0;
            for (String s : um.getStacks()) {
                i++;
                for (int j = 0; j < i; j++) {
                    System.out.print(" ");
                }
                System.out.println(s);
            }

            Files.write(finalPath, json.getBytes(), StandardOpenOption.APPEND);

        } catch (Exception e) {
            try {
                Files.write(Paths.get("err.log"), e.toString().getBytes());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            Log.warn("上传信息失败: %s", e.toString());
        }
    }

    public static void uploadString(int type, StackTraceElement[] ste, String input) {
        uploadInternal(type, ste, input);
    }

    public static void uploadStringList(int type, StackTraceElement[] ste, List<String> input) {
        uploadInternal(type, ste, input);
    }
}
