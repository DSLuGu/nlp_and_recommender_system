from simple_komoran import Simple_Komoran
from ner_and_stopwords import NER_and_Stopwords
from simple_chatbot import Simple_ChatBot

def main():
    
    # Simple Komoran
    simple_komoran = Simple_Komoran()
    simple_komoran.action()
    
    # NER and Stopwords
    ner_and_sw = NER_and_Stopwords()
    ner_and_sw.action()
    
    # Simple ChatBot
    simple_chatbot = Simple_ChatBot()
    simple_chatbot.chatbot()
    
    return None


if __name__ == '__main__':
    
    main()
    