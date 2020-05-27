package main.java.nlp.word2vec.core;

import main.java.nlp.word2vec.domain.WordEntry;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class Word2Vec {

    private HashMap<String, float[]> wordsMap = new HashMap<>();
    // 模型总词数
    private int wordsNumber;
    // 向量维度大小
    private int size;
    private static final int MAX_SIZE = 50;
    private String modelPath;

    public Word2Vec(String modelPath) {
        this.modelPath = modelPath;
    }


    /**
     * 加载词向量模型
     * @throws IOException
     */
    public void loadModel() throws IOException {
        // 加载txt格式的模型
        if (this.modelPath.endsWith("txt")) {
            loadModelTXT();
            return;
        }

        if (this.modelPath.endsWith("bin")) {
            loadModelBIN();
            return;
        }

        // 加载模型
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(this.modelPath)))) {
            wordsNumber = dis.readInt();
            size = dis.readInt();

            float dim = 0;
            String key = null;
            float[] value = null;
            for (int i=0; i<wordsNumber; i++) {
                double len = 0;
                key = dis.readUTF();
                value = new float[size];
                for (int j=0; j<size; j++) {
                    dim = dis.readFloat();
                    len += dim * dim;
                    value[j] = dim;
                }
                len = Math.sqrt(len);
                for (int j=0; j<size; j++) {
                    value[j] /= len;
                }
                wordsMap.put(key, value);
            }
        }
    }

    private void loadModelBIN() throws IOException{
//        File file = new File(modelPath);
//        int i = 0;
//        try {
//            FileReader fr = new FileReader(file);
//            BufferedReader buf = new BufferedReader(fr);
//            String vectorLine = null;
//            while ((vectorLine = buf.readLine()) != null) {
//                String[] strArray = vectorLine.split(" ");
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void loadModelTXT() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.modelPath), Charset.forName("UTF-8")))) {
            String line = br.readLine();
            wordsNumber = Integer.parseInt(line.split("\\s+")[0].trim());
            size = Integer.parseInt(line.split("\\s+")[1].trim());

            float dim = 0;
            String key = null;
            float[] value = null;
            for (int i=0; i<wordsNumber; i++) {
                double len = 0;
                line = br.readLine().trim();
                String[] params = line.split("\\s+");
                if (params.length != size + 1) {
                    System.out.println("词向量有一行格式不规范（可能是单词含有空格）：" + line);
                    --wordsNumber;
                    --i;
                    continue;
                }
                key = params[0];
                value = new float[size];
                for (int j=0; j<size; j++) {
                    dim = Float.parseFloat(params[j + 1]);
                    len += dim * dim;
                    value[j] = dim;
                }
                len = Math.sqrt(len);
                for (int j=0; j<size; j++) {
                    value[j] /= len;
                }
                wordsMap.put(key, value);
            }
        }
    }

    /**
     * 获取某个词的向量
     * @param word
     * @return
     */
    public float[] getWordVector(String word) {
        return wordsMap.get(word);
    }

    /**
     * 计算两个词之间的相似度
     * @param word1
     * @param word2
     * @return
     */
    public double getSimWith2Words(String word1, String word2) {
        if (word1.equals(word2)) {
            return 1.0;
        }

        float[] vector1 = wordsMap.get(word1);
        float[] vector2 = wordsMap.get(word2);
        if (vector1==null || vector2==null) return 0.0;
        double sim = 0;
        for (int i=0; i<vector1.length; i++) {
            sim += vector1[i] * vector2[i];
        }
        return sim;
    }

    public Set<WordEntry> getTopNSimilarWords(String queryWord, int N) {
        float[] srcVector = wordsMap.get(queryWord);
        if (srcVector == null) {
            return Collections.emptySet();
        }

        TreeSet<WordEntry> result = new TreeSet<>();
        double min = Float.MIN_VALUE;
        for (Map.Entry<String, float[]> entry : wordsMap.entrySet()) {
            if (entry.getKey().equals(queryWord)) continue;
            float[] vector = entry.getValue();
            float dist = 0;
            for (int i=0; i<vector.length; i++) {
                dist += srcVector[i] * vector[i];
            }

            if (dist > min) {
                while (N <= result.size()) { result.pollLast();}
                result.add(new WordEntry(entry.getKey(), dist));
                min = result.last().score;
            }
        }

        return result;
    }

    public int getWordsNumber() {
        return wordsNumber;
    }

    public int getSize() {
        return size;
    }
}
