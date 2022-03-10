package com.example;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

import java.util.*;

public class NER_and_Stopwords {

    Map<String, String> NER_MAP;
    Set<String> STOP_WORDS;

    public void init() {

        NER_MAP = new HashMap<>();
        NER_MAP.put("서울", "LOC");
        NER_MAP.put("데이터분석", "SKL");

        STOP_WORDS = new HashSet<>();
        STOP_WORDS.add("안녕하세요");

    }

    public void action() {

        this.init();

        Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
        String document = "안녕하세요 저는 서울에 사는 철수입니다. 데이터분석은 참 재미있는 것 같아요.";

        KomoranResult analyzeResultList = komoran.analyze(document);
        System.out.println(analyzeResultList.getPlainText());

        List<Token> tokenList = analyzeResultList.getTokenList();
        for (Token token : tokenList) {
            System.out.format("(%2d, %2d) %s/%s\n", token.getBeginIndex(),
                    token.getEndIndex(), token.getMorph(), token.getPos());
        }

        List<String> nounList = getNounList(analyzeResultList.getNouns());
        System.out.println("명사 리스트 -> " + nounList);
        // Map<String, String> nerMap = getNer
        Map<String, String> nerMap = getNerMap(tokenList);
        System.out.println("NER Map -> " + nerMap);

    }

    public List<String> getNounList(List<String> nounList) {

        List<String> rtnList = new ArrayList<>();
        for (String noun : nounList) {
            if (STOP_WORDS.contains(noun))
                continue;
            rtnList.add(noun);
        }

        return rtnList;

    }

    public Map<String, String> getNerMap(List<Token> tokenList) {

        Map<String, String> nerMap = new HashMap<>();
        String beforeStr = "";
        // boolean flags = false;

        for (Token token : tokenList) {
            // 명사가 아닐 경우, 초기화
            if (token.getPos().indexOf("NN") == -1) {
                beforeStr = "";
                continue;
            }

            // 명사일 경우
            String mergeStr = beforeStr + token.getMorph();
            if (NER_MAP.containsKey(mergeStr)) {
                nerMap.put(mergeStr, NER_MAP.get(mergeStr));
            }

            beforeStr = token.getMorph();
        }

        return nerMap;

    }

}
