class ChatBot:
    
    def __init__(self, n):
        
        self.n = n
        
        self.chatBotMap = dict()
        
        self.chatBotMap["안녕"] = "어 안녕..."
        self.chatBotMap["잘가"] = "너도 조심히 가~!"
    
    def n_gram(self, document: str):
        
        return [
            document[i:i + self.n] for i in range(len(document)) \
            if len(document) >= i + self.n
        ]
    
    def answer(self, question: str):
        
        print("[나]:\t", question)
        
        nGram = self.n_gram(question)
        
        ans = "[봇]:\t무슨 말인지 못 알아 듣겠습니다."
        
        for w in nGram:
            if w in self.chatBotMap.keys():
                ans = "[봇]:\t%s" % self.chatBotMap.get(w)
        
        print(ans)
        
        return ans
    