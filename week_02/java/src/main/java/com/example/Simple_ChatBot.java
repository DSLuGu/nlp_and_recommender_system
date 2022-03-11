package com.example;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;

import java.util.*;

public class Simple_ChatBot {

    Komoran komoran;
    Map<String, List<Integer>> WORD_QUES_MAP;
    List<String> ANSWER_LIST;

    public void init() {

        komoran = new Komoran(DEFAULT_MODEL.FULL);
        WORD_QUES_MAP = new HashMap<>();

        ANSWER_LIST = new ArrayList<>();
        ANSWER_LIST.add("저는 데이터 분석을 좋아하지만 잘 못해요.");
        ANSWER_LIST.add("짜장면 먹었습니다. 중식 좋아해요.");
        ANSWER_LIST.add("오늘은 비가 내리지 않습니다.");
        ANSWER_LIST.add("짬뽕 먹었어요. 저는 중식을 좋아해요.");

        // 질문별 응답 시퀀스 세팅
        Map<String, Integer> quesAnsMap = new HashMap<>();
        quesAnsMap.put("데이터 분석을 좋아하세요?", 0);
        quesAnsMap.put("어제 어떤 음식을 먹었어요?", 1);
        quesAnsMap.put("오늘 일기예보는 어떤가요?", 2);
        quesAnsMap.put("오늘은 어떤 음식을 먹었죠?", 3);

        for (String ques : quesAnsMap.keySet()) {
            KomoranResult analyzeResultList = komoran.analyze(ques);
            List<String> nounList = analyzeResultList.getNouns();

            for (String noun : nounList) {
                List<Integer> integerList = new ArrayList<>();
                if (WORD_QUES_MAP.containsKey(noun)) {
                    integerList = WORD_QUES_MAP.get(noun);
                }

                integerList.add(quesAnsMap.get(ques));
                WORD_QUES_MAP.put(noun, integerList);
            }

            System.out.println("WORD_QUES_MAP -> " + WORD_QUES_MAP);
        }

    }

    public void answer(String conv) {

        if (conv.equals("끝"))
            System.exit(1);

        System.out.println("conv -> " + conv);

        // 대화를 형태소 분석하고, 단어가 한가지라도 있는 곳의 문장 리턴, 없을 경우 "잘 모르겠어요"라고 리턴
        KomoranResult analyzResultList = komoran.analyze(conv);
        List<String> nounList = analyzResultList.getNouns();

        System.out.println("nounList -> " + nounList);

        List<Integer> documentList = new ArrayList<>();
        for (String noun : nounList) {
            if (WORD_QUES_MAP.containsKey(noun)) {
                documentList.addAll(WORD_QUES_MAP.get(noun));
            }
        }

        // 한가지라도 있을 경우 리턴
        if (documentList.size() > 0) {
            System.out.println("before -> " + documentList);
            Collections.shuffle(documentList);
            System.out.println("after -> " + documentList);
            System.out.println("[봇]:\t" + ANSWER_LIST.get(documentList.get(0)));
        } else {
            System.out.println("[봇]:\t잘 모르겠어요.");
        }
    }

    public void chatbot() {

        init();

        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("[나]:\t");
            answer(sc.nextLine());

        }

    }

}
