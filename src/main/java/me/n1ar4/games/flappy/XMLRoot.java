package me.n1ar4.games.flappy;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;

public class XMLRoot {
    private XMLRoot() {
    }

    public static Element getRootElement(InputStream inputStream) {
        Element root = null;
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(inputStream);
        } catch (DocumentException e) {
            document = null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
        if (null != document) {
            root = document.getRootElement();
        }
        return root;
    }

    public static Element getConfigRootElement() {
        return getRootElement(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("game/flappy/Config.xml"));
    }
}
