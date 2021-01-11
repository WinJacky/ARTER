package test.java;

import main.java.nlp.word2vec.core.WordsSplit;
import org.ansj.domain.Term;

import java.util.List;

public class TestWordSplit {
    public static void main(String[] args) {
        String object = "login name";
        List<Term> list = WordsSplit.Segment(object);
        for (Term term : list) {
            System.out.println(term.getName());
        }
    }
}
