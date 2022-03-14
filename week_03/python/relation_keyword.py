import os
import re
import sys

from tqdm import tqdm

from collections import defaultdict, OrderedDict

from konlpy.tag import Komoran
from konlpy.utils import pprint


CUR_DIR = os.path.dirname(os.path.realpath(__file__))
PAR_DIR = os.path.dirname(os.path.dirname(CUR_DIR))
NEWS_DATA_PATH = os.path.join(PAR_DIR, 'news')
USER_DIC = os.path.join(PAR_DIR, 'dictionary', 'dic.user')


class RelationKeyword:
    
    def __init__(self):
        
        self.file_list = [
            os.path.join(NEWS_DATA_PATH, f) \
            for f in os.listdir(NEWS_DATA_PATH) \
            if f.split('.')[-1] == 'txt'
        ]
        
        self.new_line = "\n"
        self.komoran = Komoran(userdic=USER_DIC)
    
    def _set_news_data(self):
        
        field_pattern = re.compile('<__([a-z\_]+)__>')
        
        lastField = ""
        rtnMap = defaultdict(dict)
        for f in self.file_list:
            o = open(f, 'r', encoding='UTF-8')
            lines = o.readlines()
            
            item = OrderedDict()
            for line in lines:
                if re.search(field_pattern, line) is not None:
                    field = re.search(field_pattern, line).groups()[0].strip()
                    field_end_point = re.search(field_pattern, line).end()
                    value = line[field_end_point:]

                    if field in item.keys():
                        rtnMap[item.get('section') + "_" + item.get('id')] = item
                        item = OrderedDict()
                    
                    item[field] = value.rstrip()
                    lastField = field
                else:
                    sb = item.get(lastField) + line
                    item[lastField] = sb
            else:
                rtnMap[item.get('section') + "_" + item.get('id')] = item
            
        return rtnMap
    
    def _extract_noun_map(self, newsMap:dict):
        
        rtnMap = defaultdict(dict)
        
        for pk in newsMap.keys():
            item = OrderedDict()
            vo = newsMap[pk]
            
            item['title'] = self._extract_nouns(vo.get('title'))
            item['summary'] = self._extract_nouns(vo.get('summary'))
            item['content'] = self._extract_nouns(vo.get('content'))
            
            rtnMap[pk] = item
        
        return rtnMap
    
    def _extract_nouns(self, doc:str):
        
        nounList = []
        if doc.strip() == "": return nounList
        
        nounList = self.komoran.nouns(doc)
        
        return nounList
    
    def _cal_TF(self, nounsMap:dict, newsMap:dict):
        
        sectKwdCntMap = defaultdict(dict)
        for pk in nounsMap.keys():
            vo = newsMap.get(pk)
            
            kwdCntMap = OrderedDict()
            if vo['section'] in sectKwdCntMap.keys():
                kwdCntMap = sectKwdCntMap.get(vo['section'])
            
            itemNounMap = nounsMap.get(pk)
            for field in itemNounMap.keys():
                nounList = itemNounMap.get(field)
                
                for noun in nounList:
                    if noun in kwdCntMap.keys():
                        kwdCntMap[noun] += 1
                    else:
                        kwdCntMap[noun] = 1
            
            sectKwdCntMap[vo['section']] = kwdCntMap
        
        return sectKwdCntMap
    
    def _sorted_kwd_map(self, sectKwdCntMap:dict, limit:int=500):
        
        rtnMap = defaultdict(set)
        for sect in sectKwdCntMap.keys():
            corpus = set()
            kwdCntMap = sectKwdCntMap.get(sect)
            sortedKwdCntMap = dict(sorted(kwdCntMap.items(), key=lambda x: x[1], reverse=True))
            
            for kwd in sortedKwdCntMap.keys():
                if len(kwd.strip()) < 2: continue
                corpus.add(kwd)
                if len(corpus) >= limit: break
            
            rtnMap[sect] = corpus
            self._corpus_to_file(corpus, out_fn=os.path.join(PAR_DIR, 'corpus', f'{sect}.tsv'))
        
        return rtnMap
    
    def _corpus_to_file(self, corpus:set, out_fn:str=None):
        
        import csv
        with open(out_fn, 'w', newline='') as f:
            writer = csv.writer(f, delimiter='\t')
            writer.writerows(corpus)
        
        return None
    
    def _relation_kwds(self, newsMap:dict, nounsMap:dict, sectCorpus:dict):
        
        relationCntMap = defaultdict(dict)
        for pk in nounsMap.keys():
            fieldKwdList = nounsMap.get(pk)
            corpus = sectCorpus.get(newsMap.get(pk)['section'])
            
            docKwdSet = set()
            for field in fieldKwdList.keys():
                docKwdSet |= set(fieldKwdList.get(field))
            
            for kwd in docKwdSet:
                kwdCntMap = OrderedDict()
                if kwd in relationCntMap.keys():
                    kwdCntMap = relationCntMap.get(kwd)
                
                for otherKwd in docKwdSet:
                    if (kwd == otherKwd) or (otherKwd not in corpus) or (len(otherKwd) < 2):
                        continue
                    
                    if otherKwd in kwdCntMap.keys():
                        kwdCntMap[otherKwd] += 1
                    else:
                        kwdCntMap[otherKwd] = 1
                
                relationCntMap[kwd] = kwdCntMap
        
        return self._calc_relation_kwds(relationCntMap)
    
    def _calc_relation_kwds(self, relationCntMap:dict, limit:int=10):
        
        relationKwdMap = defaultdict(list)
        for kwd in relationCntMap.keys():
            kwdCntMap = relationCntMap.get(kwd)
            sortedKwdCntMap = dict(sorted(kwdCntMap.items(), key=lambda x: x[1], reverse=True))
            
            sortedKwdList = []
            for sortedKwd in sortedKwdCntMap.keys():
                sortedKwdList.append(sortedKwd)
                if len(sortedKwdList) >= limit: break
            
            relationKwdMap[kwd] = sortedKwdList
        
        return relationKwdMap
    
    def _input_kwd(self, relationKwdMap):
        
        while True:
            userInput = input("키워드를 입력하세요 : ")
            
            if userInput.strip() == "끝": sys.exit(1)
            
            if userInput.strip() in relationKwdMap.keys():
                print(relationKwdMap.get(userInput.strip()))
            else:
                print("연관 키워드가 존재하지 않습니다.")
        
        return None
    
    def execute(self):
        
        newsMap = self._set_news_data()
        nounsMap = self._extract_noun_map(newsMap)
        sectKwdCntMap = self._cal_TF(nounsMap, newsMap)
        sectCorpus = self._sorted_kwd_map(sectKwdCntMap)
        relationKwdMap = self._relation_kwds(newsMap, nounsMap, sectCorpus)
        self._input_kwd(relationKwdMap)
        
        return None