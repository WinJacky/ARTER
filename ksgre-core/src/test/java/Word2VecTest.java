package test.java;

import main.java.nlp.word2vec.core.Word2Vec;
import java.io.IOException;

public class Word2VecTest {
    public static void main(String[] args) throws IOException {
        Word2Vec w2v = new Word2Vec("ksgre-core/src/main/resources/Word2VecModel/zhwiki_2017_03.sg_50d.txt");
        w2v.loadModel();
        float[] f = w2v.getWordVector("sign");
        for (float ff: f) {
            System.out.println(ff);
        }
    }
}
