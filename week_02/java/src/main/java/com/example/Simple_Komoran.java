package com.example;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

import java.util.*;

public class Simple_Komoran {

    public void action() {

        Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
        String document = "데이터분석은 참 재미있는 것 같아요";

        KomoranResult analyzeResultList = komoran.analyze(document);
        System.out.println(analyzeResultList.getPlainText());

        List<Token> tokenList = analyzeResultList.getTokenList();
        for (Token token : tokenList) {
            System.out.format("(%2d, %2d) %s/%s\n", token.getBeginIndex(),
                    token.getEndIndex(), token.getMorph(), token.getPos());
        }

        List<String> nounList = analyzeResultList.getNouns();
        for (String noun : nounList) {
            System.out.println(noun);
        }
    }

}
