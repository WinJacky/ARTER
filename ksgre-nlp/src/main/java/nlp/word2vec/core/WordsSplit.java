package main.java.nlp.word2vec.core;

import org.ansj.domain.Term;
import org.ansj.recognition.impl.FilterRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.ArrayList;
import java.util.List;

public class WordsSplit {

    /**
     * 分词
     * @param sentence
     * @return
     */
    public static List<Term> Segment(String sentence) {
        FilterRecognition filter = new FilterRecognition();
        filter.insertStopWord(",", " ", ".", "，", "。", ":", "：", "'", "‘", "’", "　", "“", "”", "《", "》",
                "[", "]", "-","=","?","&","\"","<",">","_","/","(",")","；","¥","（","）","@","©",";","#","{","}","【","】",
                "|","￥","+","!","！","的");
        return ToAnalysis.parse(sentence).recognition(filter).getTerms();
    }

    /**
     * 获取分词结果
     * @param sentence
     * @return
     */
    public static List<String> getWords(String sentence) {
        List<Term> termList = Segment(sentence);
        List<String> wordList = new ArrayList<>();
        for (Term wordTerm : termList) {
            wordList.add(wordTerm.getName());
        }
        return wordList;
    }
}
