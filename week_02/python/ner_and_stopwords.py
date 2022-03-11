from konlpy.tag import Komoran
from konlpy.utils import pprint


class NER_and_Stopwords:
    
    def __init__(self):
        
        self.NER_MAP = dict()
        self.NER_MAP["서울"] = "LOC"
        self.NER_MAP["데이터분석"] = "SKL"
        
        self.STOPWORDS = set()
        self.STOPWORDS.add("안녕하세요")
        self.STOPWORDS.add("것")
    
    def action(self):
        
        komoran = Komoran()
        document = "안녕하세요. 저는 서울에 사는 철수입니다. 데이터분석은 참 재미있는 것 같아요."
        
        tokenPosList = komoran.pos(document)
        pprint(tokenPosList)
        
        nounList = self.get_nouns(komoran.nouns(document))
        print("NOUN LIST -> ", nounList)
        
        nerMap = self.get_ner_map(tokenPosList)
        print("NER MAP -> ", nerMap)
        
        return None
    
    def get_nouns(self, nounList: list):
        
        rtnList = []
        for n in nounList:
            if n in self.STOPWORDS:
                continue
            rtnList.append(n)
        
        return rtnList
    
    def get_ner_map(self, tokenPosList):
        
        nerMap = dict()
        beforeStr = ""
        for token, pos in tokenPosList:
            if "NN" not in pos: # 명사가 아닐 경우
                beforeStr = ""
                continue
            
            mergeStr = beforeStr + token
            if mergeStr in self.NER_MAP.keys():
                nerMap[mergeStr] = self.NER_MAP[mergeStr]
            
            beforeStr = token
        
        return nerMap
    