package test.java;

import me.midday.FoolNLTK;
import me.midday.lexical.AnalysisResult;
import me.midday.lexical.Entity;
import me.midday.lexical.LexicalAnalyzer;
import me.midday.lexical.Word;

import java.util.ArrayList;
import java.util.List;

public class TestFoolNLTK {
    public static void main(String[] args) {
        // 单文本
        String text = "我爱南京";
        LexicalAnalyzer lexicalAnalyzer = FoolNLTK.getLSTMLexicalAnalyzer();

        // 分词
        List<List<Word>> words = lexicalAnalyzer.cut(text);
        System.out.println("======分词======");
        for(List<Word> ws: words){
            ws.forEach(System.out::println);
        }
        System.out.println();

        // 词性标注
        List<List<Word>> posWords = lexicalAnalyzer.pos(text);
        System.out.println("======词性标注======");
        for(List<Word> ws: posWords){
            ws.forEach(System.out::println);
        }
        System.out.println();

        // 实体识别
        List<List<Entity>>  entities = lexicalAnalyzer.ner(text);
        System.out.println("======实体识别======");
        for(List<Entity> ents :entities){
            ents.forEach(System.out::println);
        }
        System.out.println();

        // 分词，词性，实体识别
        List<AnalysisResult>  results = lexicalAnalyzer.analysis(text);
        System.out.println("======分词，词性，实体识别======");
        results.forEach(System.out::println);
        System.out.println();

        // 多文本
        List<String> docs = new ArrayList<>();
        System.out.println("多文本：");
        docs.add(text);
        text = "爱在黎明破晓时";
        docs.add(text);
        text = "I live in London";
        docs.add(text);

        // 分词
        List<List<Word>> dWords = lexicalAnalyzer.cut(docs);
        System.out.println("======分词======");
        for(List<Word> ws: dWords){
            ws.forEach(System.out::println);
        }
        System.out.println();

        //词性标注
        List<List<Word>> dPosWords = lexicalAnalyzer.pos(docs);
        System.out.println("======词性标注======");
        for(List<Word> ws: dPosWords){
            ws.forEach(System.out::println);
        }
        System.out.println();

        //实体识别
        List<List<Entity>>  dEntities = lexicalAnalyzer.ner(docs);
        System.out.println("======实体识别======");
        for(List<Entity> ents :dEntities){
            ents.forEach(System.out::println);
        }
        System.out.println();

        // 分词, 词性标注，实体识别
        List<AnalysisResult>  dResults = lexicalAnalyzer.analysis(docs);
        System.out.println("======分词，词性，实体识别======");
        dResults.forEach(System.out::println);
        System.out.println();
    }
}
