package test.java;

import main.java.nlp.word2vec.core.Word2Vec;
import main.java.nlp.word2vec.core.WordsSplit;
import main.java.nlp.word2vec.domain.WordEntry;

import java.io.IOException;
import java.util.*;

public class TestPhrase2WordSimilarity {

    private static Word2Vec word2VecHe = new Word2Vec("ksgre-core/src/main/resources/Word2VecModel/hanlp-wiki-vec-zh.txt");
    private static Word2Vec word2VecWang = new Word2Vec("ksgre-core/src/main/resources/Word2VecModel/glove_50d.txt");
    //zhwiki_2017_03.sg_50d.txt

    public static void main(String[] args) {
        String phrase = "Log Out ", word = "LogOut";

        //读取模型
        try {
            word2VecHe.loadModel();
            word2VecWang.loadModel();
        }catch (IOException e){
            e.printStackTrace();
        }

        List<String> phraseSplit = WordsSplit.getWords(phrase);
        Set<String> temp = new HashSet<>();
        temp.add(word);

//        List<String> extendWordsHe = extendKeywords(phraseSplit,5,true);
//        System.out.println("extendWordsHe: "+extendWordsHe);
//        System.out.println("Performance of model of He:");
//        computeSimilarity(ListToSet(extendWordsHe),temp,true);

        List<String> extendWordsWang = extendKeywords(phraseSplit,5,false);
        System.out.println("extendWordsWang: "+extendWordsWang);
        System.out.println("Performance of model of Wang:");
        computeSimilarity(ListToSet(extendWordsWang),temp,false);

    }

    // 扩词
    public static List<String> extendKeywords(List<String> words, int number, boolean isHe) {
        List<String> keywordSeq = new ArrayList<>();
        for (String str : words) {
            if (isHe){
                Set<WordEntry> set = word2VecHe.getTopNSimilarWords(str, number);
                for (WordEntry entry : set) {
                    keywordSeq.add(entry.name);
                }
            }else {
                Set<WordEntry> set = word2VecWang.getTopNSimilarWords(str, number);
                for (WordEntry entry : set) {
                    keywordSeq.add(entry.name);
                }
            }
            keywordSeq.add(str);
        }
        return keywordSeq;
    }

    private static double computeSimilarity(Set<String> set, Set<String> set1, boolean isHe) {
        double sumScore = 0.0;
        for (String s1: set) {
            double maxScore = 0.0;
            if (isHe){
                for (String s2: set1) {
                    double score = word2VecHe.getSimWith2Words(s1, s2);
                    System.out.println(s1 + " ==> " + s2 + " : "+ score);
                    maxScore = Math.max(maxScore, score);
                }
            }else {
                for (String s2: set1) {
                    double score = word2VecWang.getSimWith2Words(s1, s2);
                    System.out.println(s1 + " ==> " + s2 + " : "+ score);
                    maxScore = Math.max(maxScore, score);
                }
            }
            sumScore += maxScore;
        }
        System.out.println(sumScore / set.size());
        return sumScore / set.size();
    }

    public static Set<String> ListToSet(List<String> list) {
        // 返回Object数组
        String[] strings1 = new String[list.size()];
        // 将转化后的数组放入已经创建好的对象中
        list.toArray(strings1);
        // 将转化后的数组赋给新对象
        String[] strings2 = list.toArray(new String[0]);
        Set<String> set = new HashSet<>(Arrays.asList(strings2));

        return set;
    }
}
