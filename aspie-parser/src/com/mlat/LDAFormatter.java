package com.mlat;

import java.io.*;
import java.util.*;

public class LDAFormatter {
    public static List<String> vocabs;
    /* Generates a map with id associated with each word */
    public static void generateWordMapFromDirectory() {
        vocabs = new ArrayList<String>();
        List<String> stopwords = Parser.getStopwordsFromFile();
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("./data/reuters.ldac"));
            File threadsFolder = new File("/Volumes/YOKO/MLAT/MLATaspiecentral/aspie-parser/output");
            for (final File fileEntry : threadsFolder.listFiles()) {
                // ignore .DS_Store
                if (fileEntry.getName().endsWith(".txt")) {
                    HashMap<String, Integer> map = new HashMap<String, Integer>();
                    System.out.println("Parsing " + fileEntry.getName() + "......");
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith("Title:") && !line.startsWith("URL:")) {
                            String word = line.toLowerCase();
                            if (!vocabs.contains(word) && !stopwords.contains(word)) {
                                vocabs.add(word);
                            } if (!map.containsKey(word)) {
                                map.put(word, 1);
                            } else {
                                int currentCount = map.get(word);
                                map.put(word, currentCount + 1);
                            }
                        }
                    }
                    Iterator it = map.entrySet().iterator();
                    int uniquewordsCount = map.size();
                    bw.write(uniquewordsCount + " ");
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        String word = (String)pair.getKey();
                        int wordId = vocabs.indexOf(word);
                        bw.write(wordId + ":" + pair.getValue() + " ");
                        it.remove();
                    }
                    bw.write("\n");
                    map = null;
                }
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Generates a text file organized into three columns where each row contains
       {document index wordcount} */
    private static void writeVocabsToFile() {
        try {
            System.out.println("Writing words map to filename: reuters.words...");
            BufferedWriter writer = new BufferedWriter(new FileWriter("./data/reuters.words"));
            for (String word : vocabs) {
                writer.write(word + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        generateWordMapFromDirectory();
        writeVocabsToFile();
    }
}

