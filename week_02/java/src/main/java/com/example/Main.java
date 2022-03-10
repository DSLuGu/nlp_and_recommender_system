package com.example;

/**
 * Hello world!
 *
 */
public class Main {
    public static void main(String[] args) {

        // Simple Komoran
        System.out.println("[Simple Komoran]########################################");
        Simple_Komoran komoran = new Simple_Komoran();
        komoran.action();

        System.out.println("##########################################################");

        // Simple NER and Stopwords
        System.out.println("[Simple NER and Stopwords]########################################");
        NER_and_Stopwords ner_and_sw = new NER_and_Stopwords();
        ner_and_sw.action();

        System.out.println("##########################################################");

        // Simple ChatBot
        System.out.println("[Simple ChatBot]########################################");
        Simple_ChatBot bot = new Simple_ChatBot();
        bot.chatbot();

    }
}
