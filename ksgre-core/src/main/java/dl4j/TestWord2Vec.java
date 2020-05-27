package main.java.dl4j;

import main.java.config.Settings;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;
import java.util.Vector;

public class TestWord2Vec {

    public static void main(String[] args) {
        Word2Vec model = WordVectorSerializer.readWord2VecModel( new File("E:\\learn\\paper-tools\\rtbwarte\\src/main/resources/Word2VecModel/wiki.en.text.vector.bin"));

//        System.out.println(model.wordsNearest("username", 10));

        double[] s = model.getWordVector("man");
        System.out.println(s);
        System.out.println(model.getWordVector("man"));
        System.out.println(model.similarity("password", "pwd"));
    }


}
