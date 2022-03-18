package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;

public class LocalSearch {

    Set<String> filePathSet = new LinkedHashSet<>();
    Komoran KOMORAN;

    public LocalSearch() {

        KOMORAN = new Komoran(DEFAULT_MODEL.FULL);
        // KOMORAN.setUserDic("dic.user");

    }

    public void execute() throws Exception {
        // public void execute() {

        String rootDir = "/Users/lugu/Documents/localSearch";
        File[] files = new File(rootDir).listFiles();
        for (File f : files) {
            filePathSet.add(f.getAbsolutePath());
            if (f.isDirectory())
                search(f.getAbsolutePath());
        }

        // Tika tika = new Tika();
        Map<String, String> fileDocMap = new HashMap<>();
        for (String path : filePathSet) {
            int idx = path.lastIndexOf(".");
            if (idx > -1) {
                String ext = path.substring(idx + 1);
                if (ext.indexOf("doc") == 0 || ext.indexOf("pdf") == 0) {
                    fileDocMap.put(path, tika(path));
                }
            }
        }

    }

    public void search(String path) {

        File[] files = new File(path).listFiles();
        for (File f : files) {
            filePathSet.add(f.getAbsolutePath());
            if (f.isDirectory())
                search(f.getAbsolutePath());
        }
    }

    public String tika(String path) throws Exception {

        File file = new File(path);
        BodyContentHandler handler = new BodyContentHandler();
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        try (InputStream stream = new FileInputStream(file)) {
            parser.parse(stream, handler, metadata);
            return handler.toString();
        }
    }

}
