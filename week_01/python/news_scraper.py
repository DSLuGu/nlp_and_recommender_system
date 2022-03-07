import os
import sys

import argparse

import re
import time
import random

from tqdm import tqdm

from collections import OrderedDict

import requests
from fake_useragent import UserAgent
from bs4 import BeautifulSoup


def argparser():
    
    p = argparse.ArgumentParser()
    
    p.add_argument('--url', required=True)
    
    config = p.parse_args()
    
    return config


def get_news_list(url:str, headers=dict):
    
    response = requests.get(url, headers=headers)
    
    if response.status_code != 200:
        print("Main - Requests error...", response.status_code)
        print(url)
        sys.exit()
    
    soup = BeautifulSoup(response.text, 'lxml')
    
    sub_section = soup.select_one('div.slide.swiper-slide.selected > a').text.strip()
    
    news_list = soup.select('div.news_item_list > ul > li')
    
    return sub_section, news_list


def detail_news(url:str, headers:dict):
    
    response = requests.get(url, headers=headers)
    
    if response.status_code != 200:
        print("Detail - Requests error...", response.status_code)
        print(url)
        sys.exit()
    
    soup = BeautifulSoup(response.text, 'lxml')
    
    if soup.find('div', 'all_content'):
        # content = soup.select_one('div.all_content').text.strip()
        content = ''.join([t.strip() + '\n' for t in soup.select_one('div.all_content').find_all(text=True, recursive=False)])
    elif soup.find('div', 'article_body'):
        # content = soup.select_one('div.article_body').text.strip()
        content = ''.join([t.strip() + '\n' for t in soup.select_one('div#article_body').find_all(text=True, recursive=False)])
    else:
        content = ''.join([t.strip() + '\n' for t in soup.select_one("div[itemprop='articleBody']").find_all(text=True, recursive=False)])
    
    start_point = 0
    if re.search('기사 요약', content):
        start_point = re.search('기사 요약', content).start() + 5
    content = content[start_point:].lstrip()
    
    end_point = len(content)
    if re.search('<저작권자', content):
        end_point = re.search('<저작권자', content).start()
    elif re.search('뉴스자동화 안내 레이어', content):
        end_point = re.search('뉴스자동화 안내 레이어', content).start()
    
    content = content[:end_point].rstrip()
    
    return content


def data_to_fgf(data:dict, print_format='<__{}__>{}'):
    
    result = ""
    for k, v in data.items():
        result += print_format.format(k, v) + '\n'
    
    return result


def main(config):
    '''
    :정치:선거와 투표: https://news.zum.com/issuelist/60055642?
    :사회:한반도 덮친 미세먼지: https://news.zum.com/issuelist/36787591?cm=news_issue_article_list&r=44
    :경제:가상화폐 열풍: https://news.zum.com/issuelist/65406000?cm=news_issue_article_list&r=2
    :IT/과학:메타버스가 온다: https://news.zum.com/issuelist/69911486?cm=news_issue_article_list&r=3
    :국제:러시아, 우크라이나 침공: https://news.zum.com/issuelist/72968640?cm=news_issue_article_list&r=5
    :문화:영화계 소식: https://news.zum.com/issuelist/40552661?cm=news_issue_article_list&r=14
    '''
    
    base_url = 'https://news.zum.com/'
    
    url = config.url
    paging_url = url + 'p={}'
    
    i, result = 1, []
    ua = UserAgent()
    headers = {'User-Agent': ua.random}
    
    while len(result) < 1000:
        try:
            sub_section, news_list = get_news_list(url.format(i), headers)
        except:
            i += 1
            continue
        
        # time.sleep(random.randint(0, 10))
        
        for news in news_list:
            
            if len(result) % 3 == 0:
                headers['User-Agent'] = ua.random
            
            a = news.select_one('a')
            info = news.select_one('ul.info')
            
            data = OrderedDict()
            
            data['id'] = len(result)
            data['url'] = base_url + a['href']
            data['section'] = a['data-select-tab']
            data['sub_section'] = sub_section
            data['title'] = a.select_one('h2.title').text.strip()
            data['summary'] = a.select_one('div.text').text.strip()
            data['dttm'] = info.select_one('li.time').text.strip()
            data['media'] = info.select_one('li.media').text.strip()
            try:
                data['content'] = detail_news(data['url'], headers)
            except:
                time.sleep(random.randint(0, 5))
                continue
            
            
            # 최소 500자 이상의 뉴스만 추출...
            if len(data['content']) < 500: continue
            
            result.append(data)
            if len(result) >= 1000: break
            
            # time.sleep(random.randint(0, 5))
        
        i += 1
        
        if i % 5 == 0: print(i, len(result))
    
    output = open(
        os.path.join('./news', f"{a['data-select-tab']}_{sub_section}.txt"), 
        'w', encoding='UTF-8'
    )
    for d in result:
        output.write(data_to_fgf(d))
    output.close()
    
    return None


if __name__ == '__main__':
    
    config = argparser()
    main(config)
    