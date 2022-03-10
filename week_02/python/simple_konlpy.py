import os
import time

import argparse

import urllib.request

from konlpy.tag import Okt, Komoran, Kkma

import matplotlib.pyplot as plt
from matplotlib import rc


rc('font', family='AppleGothic')
plt.rcParams['axes.unicode_minus'] = False


def argparser():
    
    p = argparse.ArgumentParser()
    
    p.add_argument(
        '--output_fn', type=str, default="./week_02/ratings.txt", 
        help="NSMC 데이터 경로를 입력해주세요."
    )
    
    config = p.parse_args()
    
    return config


def check_exist(fn):
    
    if os.path.exists(fn): return True
    
    return False


def download_nsmc(output_fn):
    
    print("[DOWNLOAD] NSMC DATA\n")
    url = 'https://raw.githubusercontent.com/e9t/nsmc/master/ratings.txt'
    urllib.request.urlretrieve(url, filename=output_fn)
    
    return None


def set_review_data(fn):
    
    with open(fn, 'r', encoding='utf8') as f:
        data = [line.split('\t') for line in f.read().splitlines()]
        return data[1:101]
    
    return None


def main(config):
    
    if not check_exist(config.output_fn):
        download_nsmc(config.output_fn)
    
    s_time = time.time()
    print("[BEGIN] Read review data\n")
    review_data = set_review_data(config.output_fn)
    print("review_data size ->", len(review_data), end="\n\n")
    print("[END] Read view data %.2f sec" % (time.time() - s_time), end="\n\n")
    
    konlpy_performs = {}
    print(review_data[1][1], end="\n\n")
    
    s_time = time.time()
    kkma = Kkma()
    nouns = [kkma.nouns(data[1]) for data in review_data]
    print(nouns[1], end="\n\n")
    konlpy_performs['kkma'] = time.time() - s_time
    print("꼬꼬마 명사 추출 %.2f sec" % konlpy_performs['kkma'], end="\n\n")
    
    s_time = time.time()
    komoran = Komoran()
    nouns = [komoran.nouns(data[1]) for data in review_data]
    print(nouns[1], end="\n\n")
    konlpy_performs['komoran'] = time.time() - s_time
    print("코모란 명사 추출 %.2f sec" % konlpy_performs['komoran'], end="\n\n")
    
    s_time = time.time()
    okt = Okt()
    nouns = [okt.nouns(data[1]) for data in review_data]
    print(nouns[1], end="\n\n")
    konlpy_performs['okt'] = time.time() - s_time
    print("OKT 명사 추출 %.2f sec" % konlpy_performs['okt'], end="\n\n")
    
    plt.bar(*zip(*konlpy_performs.items()))
    plt.title("KONLPY 형태소 분석기 비교(시간)")
    plt.show()
    
    return None


if __name__ == '__main__':
    
    config = argparser()
    main(config)
    