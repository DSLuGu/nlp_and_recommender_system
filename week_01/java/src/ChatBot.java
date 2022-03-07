import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatBot {

    Map<String, String> chatBotMap;

    public void init() {

        chatBotMap = new HashMap<>();

        chatBotMap.put("안녕", "어 안녕...");
        chatBotMap.put("잘가", "너도 조심히 가~!");

    }

    public List<String> nGram(int n, String document) {

        List<String> rtnList = new ArrayList<>();

        for (int i = 0; i < document.length(); i++) {
            if (document.length() >= i + n) {
                rtnList.add(document.substring(i, i + n));
            }
        }

        return rtnList;

    }

    public String answer(String question) {

        System.out.println("[나]:\t" + question);

        String rtnStr = "";
        if (chatBotMap == null || chatBotMap.size() == 0)
            init();

        List<String> nGramList = nGram(2, question);
        // System.out.println(nGramList);

        String answerStr = "[봇]:\t무슨 말인지 못 알아 듣겠습니다.";
        for (String w : nGramList) {
            if (chatBotMap.containsKey(w)) {
                answerStr = "[봇]:\t" + chatBotMap.get(w);
            }
        }

        System.out.println(answerStr);

        return rtnStr;

    }
}
