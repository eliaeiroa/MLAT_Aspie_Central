package com.mlat;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;


public class Parser {

    final static String HTMLSTRING = "AspiesCentral.com Log in or Sign up AspiesCentral.com Home Forums > Announcements & Feedback > Site Questions, Suggestions & Feedback > Welcome to Aspies Central, a friendly forum to discuss Aspergers Syndrome, Autism, High Functioning Autism and related conditions. Your voice is missing! You will need to register to get access to the following site features: Reply to discussions and create your own threads. Our modern chat room. No add-ons or extensions required, just login and start chatting! Private Member only forums for more serious discussions that you may wish to not have guests or search engines access to. Your very own blog. Write about anything you like on your own individual blog. We hope to see you as a part of our community soon! Please also check us out @ https:\\/\\/www.twitter.com\\/aspiescentral";
    final static String STOP_WORDS_FILE = "stop-words_english.txt";
    final static String THREADS_FILE = "/Volumes/YOKO/MLAT/MLATaspiecentral/aspie-parser/output";
    /*
    *  getStopwordsFromFile() reads stop words from files and store them in List.
    * */
    public static List<String> getStopwordsFromFile() {
        String line = "";
        List<String> stopwords = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(STOP_WORDS_FILE));
            while ((line = br.readLine()) != null) {
                stopwords.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopwords;
    }

    /*
    *  This method sorts the given HashMap by values assuming that values are integer type.
    * */
    public static LinkedHashMap sortHashMapByValues(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap sortedMap = new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String)key, (Integer)val);
                    break;
                }

            }
        }
        return sortedMap;
    }

    /*
    *  This method prints out the most common words appeared in the threads.
    *  Assumption is made that most common words may contain stop words.
    * */
    public static void printMostCommonWords(int offset) {
        List<String> stopwords = getStopwordsFromFile();
        File threadsFolder = new File(THREADS_FILE);
        // map stores word as a key, count as a value
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (final File fileEntry : threadsFolder.listFiles()) {
            if (fileEntry.getName().endsWith(".txt")) {  // ignore .DS_Store
                try {
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    for (String line; (line = br.readLine()) != null; ) {
                        if (!line.startsWith("Title:") && !line.startsWith("URL:")) {
                            String word = line.toLowerCase();
                            if (!stopwords.contains(line.toLowerCase())) {
                                if (map.containsKey(word)) {
                                    int count = map.get(word);
                                    map.put(word, count + 1);
                                } else {
                                    map.put(word, 1);
                                }
                            }
                        }
                    }
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        LinkedHashMap linkedHashMap = sortHashMapByValues(map);
        List mapKeys = new ArrayList(linkedHashMap.keySet());
        int map_size = mapKeys.size() - 1;
        if (offset >= map_size) {
            offset = 0;
        } else {
            offset = map_size - offset;
        }
        for (int i = map_size; i >= offset; i--) {
            System.out.println(mapKeys.get(i));

        }
    }
    /*
        parse() parses the threads
        TODO: Write Documentation
    * */
    public static void parse() {
        JSONParser parser = new JSONParser();
        List<String> stopwords = getStopwordsFromFile();
        BufferedWriter writer = null;
        try {
            int size = 0;
            JSONObject obj = (JSONObject) parser.parse(new FileReader("data/sample.txt"));
            JSONObject response = (JSONObject) obj.get("response");
            JSONArray docs = (JSONArray) response.get("docs");
            while (docs.size() > size && docs.get(size) != null) {
                JSONObject doc = (JSONObject) docs.get(size);
                String title = doc.get("title").toString().replace("[", "").replace("]","").replace("\"", "");
                String url =  (doc.get("url").toString()).replace("\\/", "/").replace("[", "").replace("]","").replace("\"", "");
                if (!url.endsWith("index.rss")) {
                    writer = new BufferedWriter(new FileWriter("output/thread" + size + ".txt"));
                    // Replacing Html tags for every thread
                    String content = doc.get("content").toString().replace(HTMLSTRING, "");

                    System.out.println("Title: " + title);
                    System.out.println("URL: " + url);

                    // Replacing all non-alpha characters with empty strings
                    String filteredContent = content.replaceAll("[^A-Za-z ]", "");
                    // Separating each string by space and store them into array
                    String textStr[] = filteredContent.split("\\s+");

                    writer.write("Title: " + title + "\n");
                    writer.write("URL: " + url + "\n");

                    for (int i=0; i<textStr.length; ++i) {
                        // Don't include string that has length 1
                        // Don't include string that are stop words
                        if (textStr[i].length() > 1 && !stopwords.contains(textStr[i].toLowerCase())) {
                            writer.write(textStr[i] + "\n");
                        }
                    }

                    System.out.println("Successfully wrote to file >" + "thread" + size + ".txt");
                    System.out.println("----------------------------------------");
                    writer.close();
                } else {
                    // avoid duplicate threads
                    System.out.println("Skipping " + title + "....");
                    System.out.println("----------------------------------------");

                }
                size++;
            }
        } catch (ParseException pe) {
            System.out.println("position: " + pe.getPosition());
            System.out.println(pe);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//        printMostCommonWords(100);
//    }
}
