package com.mlat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class MatLabTextFormatter {
    /* Generates a map with id associated with each word */
    public static HashMap<String, Integer> generateWordMapFromDirectory() {
        HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
        Parser parser = new Parser();
        List<String> stopwords = parser.getStopwordsFromFile();
        BufferedWriter bw = null;
        int id = 0;
        try {
            bw = new BufferedWriter(new FileWriter("./data/wordOccurence.txt"));
            File threadsFolder = new File("/Volumes/YOKO/MLAT/MLATaspiecentral/aspie-parser/output");
            for (final File fileEntry : threadsFolder.listFiles()) {
                // ignore .DS_Store
                if (fileEntry.getName().endsWith(".txt")) {
                    System.out.println("Parsing filename:" + fileEntry.getName() + "......");
                    HashMap<Integer, Integer> occurenceMap = new HashMap<Integer, Integer>();
                    Integer docNum = Integer.parseInt(fileEntry.getName().replaceAll("[^\\d]", "" ));
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith("Title:") && !line.startsWith("URL:")) {
                            if (wordMap.containsKey(line.toLowerCase())) {
                                int wordId = wordMap.get(line.toLowerCase());
                                if (occurenceMap.containsKey(wordId)) {
                                    int count = occurenceMap.get(wordId);
                                    occurenceMap.put(wordId, ++count);
                                } else {
                                    occurenceMap.put(wordId, 1);
                                }
                            }
                            if (!wordMap.containsKey(line.toLowerCase())) {
                                if (!stopwords.contains(line.toLowerCase())) {
                                    wordMap.put(line.toLowerCase(), id);
                                    occurenceMap.put(id++, 1);
                                }
                                }
                            }
                    }
                    Iterator it = occurenceMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        bw.write(docNum + "," + pair.getKey() + "," + pair.getValue() + "\n");
                        it.remove();
                    }
                    occurenceMap = null;
                }
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wordMap;
    }

    /* Generates a text file organized into three columns where each row contains
       {document index wordcount} */
    private static void writeMapToFile(HashMap<String, Integer> map) {
        try {
            System.out.println("Writing words map to filename: wordsMap.txt...");
            BufferedWriter writer = new BufferedWriter(new FileWriter("./data/wordsMap.txt"));
            Iterator it = map.entrySet().iterator();
            writer.write("Id : Words\n");
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                writer.write(pair.getValue() + " : " + pair.getKey() + "\n");
                it.remove();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    public static void main(String[] args) {
//        HashMap<String, Integer> map = generateWordMapFromDirectory();
//        writeMapToFile(map);
//    }
}

