import argparse

from chatbot import ChatBot


def argparser():
    
    p = argparse.ArgumentParser()
    
    p.add_argument(
        '--n', required=True, type=int, default=1, 
        help="n-gram의 window 사이즈를 입력해주세요."
    )
    p.add_argument(
        '--q', required=True, type=str, default="안녕하세요.", 
        help="봇에게 물어볼 질문을 입력해주세요."
    )
    
    config = p.parse_args()
    
    return config


def main(config):
    
    chatBot = ChatBot(config.n)
    
    chatBot.answer(config.q)
    
    return None


if __name__ == '__main__':
    
    config = argparser()
    main(config)
    