package test.java;

import main.java.nlp.word2vec.domain.WordEntry;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class TestSimilarWords {
    public static Word2Vec word2VecModel = null;
    public static void main(String[] args) throws IOException {
//        File directory = new File("");
//        System.out.println(directory.getPath());
//        System.out.println(directory.getAbsolutePath());
//        Word2Vec word2Vec = new Word2Vec("ksgre-core/src/main/resources/Word2VecModel/wiki.en.text.vector.bin");
////        Word2Vec word2Vec = new Word2Vec("ksgre-core/src/main/resources/javaVector.model");
//        word2Vec.loadModel();
//        Set<WordEntry> similarWords = word2Vec.getTopNSimilarWords("提交",  5);
//        for (WordEntry entry: similarWords) {
//            System.out.println(entry.name);
//        }
        word2VecModel = WordVectorSerializer.readWord2VecModel(new File("ksgre-core/src/main/resources/Word2VecModel/wiki.en.text.vector.bin"));
        System.out.println(word2VecModel.getVocab().numWords());
        System.out.println(word2VecModel.getWordVector("man").length);
//        System.out.println(word2Vec.getSize());
//        System.out.println(word2Vec.getSimWith2Words("招聘", "雇佣"));
    }
}
