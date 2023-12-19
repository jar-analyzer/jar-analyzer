package me.n1ar4.y4lang.core;

import com.beust.jcommander.JCommander;
import me.n1ar4.y4lang.Command;
import me.n1ar4.y4lang.ast.ASTree;
import me.n1ar4.y4lang.ast.NullStmt;
import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.env.ResizableArrayEnv;
import me.n1ar4.y4lang.lexer.Lexer;
import me.n1ar4.y4lang.natives.Natives;
import me.n1ar4.y4lang.token.Token;
import me.n1ar4.y4lang.util.StringUtil;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Core {
    public static void start(String[] args) {
        Command command = new Command();
        JCommander jc = JCommander.newBuilder().addObject(command).build();
        jc.parse(args);
        if (command.help) {
            jc.usage();
            return;
        }
        if (StringUtil.isEmpty(command.filename)) {
            System.out.println("filename is null");
            return;
        }
        String extName = StringUtil.getExtName(command.filename);
        if (StringUtil.isEmpty(extName)) {
            System.out.println("extension name is null");
            return;
        } else {
            if (!extName.equalsIgnoreCase("h")) {
                System.out.println("error extension name");
                return;
            }
        }
        Path path = Paths.get(command.filename);
        if (!Files.exists(path)) {
            System.out.println("file is not exist");
            return;
        }
        try {
            byte[] data = Files.readAllBytes(path);
            byte[] temp = new byte[data.length + 1];
            System.arraycopy(data, 0, temp, 0, data.length);
            temp[data.length] = 10;
            if (data[data.length - 1] != 10) {
                Files.write(path, temp);
            }
            FileReader reader = new FileReader(path.toFile());
            CoreParser fp = new CoreParser();
            Environment env = new Natives().environment(new ResizableArrayEnv());
            Lexer lexer = new Lexer(reader);
            while (lexer.peek(0) != Token.EOF) {
                ASTree t = fp.parse(lexer);
                if (!(t instanceof NullStmt)) {
                    t.lookup(env.symbols());
                    t.eval(env);
                }
            }
            Threads.stopAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
