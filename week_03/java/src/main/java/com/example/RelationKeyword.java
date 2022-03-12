package com.example;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RelationKeyword {

    final String NEW_LINE = System.getProperty("line.separator");
    Komoran KOMORAN;

    public RelationKeyword() {

        KOMORAN = new Komoran(DEFAULT_MODEL.FULL);
        KOMORAN.setUserDic(
                "/Users/lugu/Documents/git-repositories/ds.lminho248@gmail.com/nlp_and_recommender_system/dictionary/dic.user");

    }

    // SHA-256 해싱 메소드
    public String sha256(String msg) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(msg.getBytes());

        return bytesToHex(md.digest());

    }

    // Byte를 Hex 값으로 변환하는 메소드
    public String bytesToHex(byte[] bytes) {

        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();

    }

    // 수집한 뉴스 데이터 세팅
    public Map<String, NewsVO> setNewsData() {

        Map<String, NewsVO> rtnMap = new LinkedHashMap<>();
        File[] newsFiles = new File(
                "/Users/lugu/Documents/git-repositories/ds.lminho248@gmail.com/nlp_and_recommender_system/news")
                .listFiles();
        Set<String> hashedSet = new HashSet<>();

        String lastField = "";
        for (File newsFile : newsFiles) {
            try {
                BufferedReader inFiles = new BufferedReader(new InputStreamReader(
                        new FileInputStream(newsFile.getAbsolutePath()), StandardCharsets.UTF_8));
                Map<String, String> item = new HashMap<>();
                String line = null;
                while ((line = inFiles.readLine()) != null) {
                    // 필드값이 포함된 라인
                    if (line.indexOf("<__") == 0 && line.indexOf("__>") > -1) {
                        String field = line.substring(3, line.indexOf("__>"));
                        String value = line.substring(line.indexOf("__>") + 3);

                        // Map에 키가 중복될 경우, 뉴스 세팅 완료
                        if (item.containsKey(field)) {
                            // 세팅하기 전에 해시값이 중복되었는지 체크 (뉴스 중복 체크)
                            String hashedText = sha256(item.get("url"));
                            if (!hashedSet.contains(hashedText)) {
                                // Map to NewVO를 한 후, 리스트에 세팅 (section_id)를 pk로 설정
                                rtnMap.put(item.get("section") + "_" + item.get("id"), mapToNewsVO(item));
                            }

                            hashedSet.add(hashedText);
                            // Map 초기화
                            item = new HashMap<>();
                        }

                        item.put(field, value);
                        lastField = field;
                    } else {
                        // 필드값을 못 찾는 경우 (value만 존재)
                        StringBuffer sb = new StringBuffer();
                        sb.append(item.get(lastField) + NEW_LINE);
                        sb.append(line);
                        item.put(lastField, sb.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return rtnMap;
    }

    public NewsVO mapToNewsVO(Map<String, String> item) {

        NewsVO vo = new NewsVO();

        vo.setId(item.get("id"));
        vo.setUrl(item.get("url"));
        vo.setSection(item.get("section"));
        vo.setSubSection(item.get("subSection"));
        vo.setTitle(item.get("title"));
        vo.setSummary(item.get("summary"));
        vo.setDttm(item.get("dttm"));
        vo.setMedia(item.get("media"));
        vo.setContent(item.get("content"));

        return vo;

    }

    public Map<String, Map<String, List<String>>> extractNounMap(Map<String, NewsVO> newsMap) {

        Map<String, Map<String, List<String>>> rtnMap = new HashMap<>();
        for (String pk : newsMap.keySet()) {
            Map<String, List<String>> item = new HashMap<>();

            NewsVO vo = newsMap.get(pk);

            item.put("title", extractNouns(vo.getTitle()));
            item.put("summary", extractNouns(vo.getSummary()));
            item.put("content", extractNouns(vo.getContent()));

            rtnMap.put(pk, item);
        }

        return rtnMap;

    }

    public List<String> extractNouns(String document) {

        List<String> nounList = new ArrayList<>();
        if (document.trim().length() == 0)
            return nounList;

        KomoranResult analyzeResultList = KOMORAN.analyze(document);
        nounList = analyzeResultList.getNouns();

        return nounList;

    }

    public Map<String, Map<String, Integer>> calTF(Map<String, Map<String, List<String>>> nounsMap,
            Map<String, NewsVO> newsMap) {

        Map<String, Map<String, Integer>> sectKwdCntMap = new HashMap<>();
        for (String pk : nounsMap.keySet()) {
            NewsVO vo = newsMap.get(pk);

            // 이전 섹션값별 키워드 카운트 값을 가져온다.
            Map<String, Integer> kwdCntMap = new HashMap<>();
            if (sectKwdCntMap.containsKey(vo.getSection())) {
                kwdCntMap = sectKwdCntMap.get(vo.getSection());
            }

            // pk별 nouns 데이터를 읽은 후 카운트 세팅
            Map<String, List<String>> itemNounMap = nounsMap.get(pk);
            for (String field : itemNounMap.keySet()) {
                List<String> nounList = itemNounMap.get(field);

                for (String noun : nounList) {
                    int cnt = 1;

                    if (kwdCntMap.containsKey(noun)) {
                        cnt += kwdCntMap.get(noun);
                    }
                    kwdCntMap.put(noun, cnt);
                }
            }

            sectKwdCntMap.put(vo.getSection(), kwdCntMap);
        }

        return sectKwdCntMap;

    }

    // 카운트 순으로 정렬
    public Map<String, Set<String>> sortedKwdMap(Map<String, Map<String, Integer>> sectKwdCntMap, int limit) {

        Map<String, Set<String>> rtnMap = new HashMap<>();
        for (String sect : sectKwdCntMap.keySet()) {
            Set<String> corpus = new LinkedHashSet<>();
            Map<String, Integer> kwdCntMap = sectKwdCntMap.get(sect);

            // 정렬
            Map<String, Integer> result = kwdCntMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new));

            for (String kwd : result.keySet()) {
                corpus.add(kwd);
                if (corpus.size() >= limit)
                    break;
            }

            rtnMap.put(sect, corpus);
        }

        return rtnMap;
    }

    // 연관 단어 생성
    public Map<String, List<String>> relationKwds(Map<String, NewsVO> newsMap,
            Map<String, Map<String, List<String>>> nounsMap, Map<String, Set<String>> sectCorpus) {

        Map<String, Map<String, Integer>> relationCntMap = new HashMap<>();

        for (String pk : nounsMap.keySet()) {
            Map<String, List<String>> fieldKwdList = nounsMap.get(pk);
            Set<String> corpus = sectCorpus.get(newsMap.get(pk).getSection());

            // 한 문서에 같이 등장하면 연관성이 있는 단어로 인정
            Set<String> docKwdSet = new HashSet<>();
            for (String field : fieldKwdList.keySet()) {
                docKwdSet.addAll(fieldKwdList.get(field));
            }

            // 생성된 docKwdSet을 단어별 연관 키워드로 생성
            for (String kwd : docKwdSet) {
                Map<String, Integer> kwdCntMap = new HashMap<>();

                if (relationCntMap.containsKey(kwd))
                    kwdCntMap = relationCntMap.get(kwd);

                for (String otherKwd : docKwdSet) {
                    if (kwd.equals(otherKwd) || !corpus.contains(otherKwd) || otherKwd.length() <= 1)
                        continue;

                    int cnt = 1;
                    if (kwdCntMap.containsKey(otherKwd)) {
                        cnt += kwdCntMap.get(otherKwd);
                    }

                    kwdCntMap.put(otherKwd, cnt);
                }

                relationCntMap.put(kwd, kwdCntMap);
            }
        }

        return calcRelationKwds(relationCntMap);

    }

    // 카운트 기반 내림차순 정렬 후 상위 연관 키워드를 추출
    public Map<String, List<String>> calcRelationKwds(Map<String, Map<String, Integer>> relationCntMap) {

        Map<String, List<String>> relationKwdMap = new HashMap<>();

        for (String kwd : relationCntMap.keySet()) {
            Map<String, Integer> kwdCntMap = relationCntMap.get(kwd);

            // 정렬
            Map<String, Integer> result = kwdCntMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new));

            List<String> sortedKwdList = new ArrayList<>();
            for (String sortedKwd : result.keySet()) {
                sortedKwdList.add(sortedKwd);
                if (sortedKwdList.size() >= 10)
                    break;
            }

            relationKwdMap.put(kwd, sortedKwdList);
        }

        return relationKwdMap;

    }

    // 연관 키워드 출력
    public void inputKwd(Map<String, List<String>> relationKwdMap) {

        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.print("키워드를 입력하세요. : ");
            String ques = sc.nextLine();

            if (ques.trim().equals("끝"))
                System.exit(1);

            if (relationKwdMap.containsKey(ques.trim())) {
                System.out.println(relationKwdMap.get(ques.trim()));
            } else {
                System.out.println("연관 키워드가 존재하지 않습니다.");
            }
        }

    }

    public void execute() {

        Map<String, NewsVO> newsMap = setNewsData();
        // for (String pk : newsMap.keySet()) {
        // System.out.println(pk + "->>" + newsMap.get(pk));
        // System.exit(1);
        // }

        // 컬럼별 형태소 분석 수행 (pk, 필드, 명사리스트)
        Map<String, Map<String, List<String>>> nounsMap = extractNounMap(newsMap);
        // for (String pk : nounsMap.keySet()) {
        // System.out.println(pk + " -> " + nounsMap.get(pk));
        // System.exit(1);
        // }

        // 뉴스 섹션별(6종) TF 기반 카운트 생성 (파일 저장 및 TF-Corpus 메소드, 20분?)
        Map<String, Map<String, Integer>> sectKwdCntMap = calTF(nounsMap, newsMap);
        // for (String section : sectKwdCntMap.keySet()) {
        // Map<String, Integer> kwdCntMap = sectKwdCntMap.get(section);

        // int count = 0;
        // for (String kwd : kwdCntMap.keySet()) {
        // System.out.println(kwd + " -> " + kwdCntMap.get(kwd));
        // count++;

        // if (count > 100)
        // break;
        // }
        // System.exit(1);
        // }

        // sectKwdCntMap을 내림차순 정렬 후 상위값 가져오기
        Map<String, Set<String>> sectCorpus = sortedKwdMap(sectKwdCntMap, 500);

        // 연관 단어 생성
        Map<String, List<String>> relationKwdMap = relationKwds(newsMap, nounsMap, sectCorpus);

        inputKwd(relationKwdMap);

    }
}
