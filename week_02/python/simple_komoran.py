from konlpy.tag import Komoran
from konlpy.utils import pprint

class Simple_Komoran:
    
    def __init__(self):
        
        self.komoran = Komoran()
        self.document = "데이터분석은 참 재미있는 것 같아요."
        
    def action(self):
        
        tokenPosList = self.komoran.pos(self.document)
        pprint(tokenPosList)
        
        nounList = self.komoran.nouns(self.document)
        for n in nounList:
            print(n)
        
        return None
    