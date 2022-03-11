import sys
import random

from collections import defaultdict

from konlpy.tag import Komoran
from konlpy.utils import pprint


class Simple_ChatBot:
    
    def __init__(self):
        
        self.komoran = Komoran()
        self.WORD_QUES_MAP = defaultdict(list)
        
        # 답변 세팅
        self.ANSWER_LIST = []
        self.ANSWER_LIST.append("저는 데이터 분석을 좋아하지만 잘 못해요.")
        self.ANSWER_LIST.append("짜장면 먹었습니다. 중식 좋아해요.")
        self.ANSWER_LIST.append("오늘은 비가 내리지 않습니다.")
        self.ANSWER_LIST.append("짬뽕 먹었어요. 저는 중식을 좋아해요.")
        
        # 질문별 답변 시퀀스 세팅
        self.quesAnsMap = dict()
        self.quesAnsMap["데이터 분석을 좋아하세요?"] = 0
        self.quesAnsMap["어제 어떤 음식을 먹었어요?"] = 1
        self.quesAnsMap["오늘 일기예보는 어떤가요?"] = 2
        self.quesAnsMap["오늘은 어떤 음식을 먹었죠?"] = 3
        
        for ques in self.quesAnsMap.keys():
            nounList = self.komoran.nouns(ques)
            
            for n in nounList:
                intList = []
                if n in self.WORD_QUES_MAP.keys():
                    intList = self.WORD_QUES_MAP.get(n)
                
                intList.append(self.quesAnsMap.get(ques))
                self.WORD_QUES_MAP[n] = intList
            
            print("WORD_QUES_MAP - >", self.WORD_QUES_MAP)
    
    def answer(self, conversation):
        
        if conversation == "끝":
            sys.exit(1)
        
        print("[나]:\t", end="")
        print("Conversation ->", conversation)
        
        # 대화를 형태소 분석하고 단어가 한가지라도 포함하는 문장 리턴, 없을 경우 "잘 모르겠어요"라고 답변
        nounList = self.komoran.nouns(conversation)
        
        docList = []
        for n in nounList:
            if n in self.WORD_QUES_MAP.keys():
                docList.extend(self.WORD_QUES_MAP.get(n))
        
        if len(docList) > 0:
            print("Before ->", docList)
            random.shuffle(docList)
            print("After ->", docList)
            print("[봇]:\t", self.ANSWER_LIST[docList[0]])
        else:
            print("[봇]:\t잘 모르겠어요.")
        
        return None
    
    def chatbot(self):
        
        while True:
            conversation = input("질문을 입력해주세요:\t")
            self.answer(conversation)
        
        return None