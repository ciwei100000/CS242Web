package hadoop;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.KStemFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class IndexReader {

    private static final long N = 7835146; //for BM25
    private static final double k_1 = 1.2;
    private static final double K = 1.11;
    private static final int k_2 = 100;
    private static final int Max_Length_IndexList_Output = 200;
    private static final long MAX_HASHMAP_SIZE = 100000;

    static public Map<String, Double> doGetIndex(File indexPath, String query) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(indexPath));

        Map<String, Double> twitteridMap = new HashMap<>();
        Map<String, Integer> keywordCountMap = new HashMap<>();

        String[] wordAndIndexes;
        String tempString;
        String[] idsCounts;
        String[] idCount;
        double f_i;
        Double BM25;
        long n_i;

        ArrayList<String> keywordList = doParseQuery(query);

        Integer count;

        for (String keyw : keywordList) {
            count = keywordCountMap.get(keyw);
            if (count == null) {
                keywordCountMap.put(keyw, 1);
            } else {
                keywordCountMap.put(keyw, count+1);
            }
        }

        while ((tempString = bufferedReader.readLine()) != null) {

            if (twitteridMap.size() > MAX_HASHMAP_SIZE) {
                System.out.println("HashMap Exceeds MAX_HASHMAP_SIZE");
                break;
            }

            wordAndIndexes = tempString.split("\t");

            count = keywordCountMap.get(wordAndIndexes[0]);

            if (count != null) {


                idsCounts = wordAndIndexes[1].split(";");
                n_i = idsCounts.length;

                for (int i = 0; i < n_i && i < Max_Length_IndexList_Output; ++i) {

                    idCount = idsCounts[i].split(":");
                    f_i = Long.parseLong(idCount[1]);

                    BM25 = twitteridMap.get(idCount[0]);

                    if (BM25 != null) {
                        BM25 += (Math.log((N - n_i + 0.5) / (n_i + 0.5))) *
                                (((k_1 + 1) * f_i) / (K + f_i)) * ((k_2 + 1) * count / (k_2 + count));
                    } else {
                        BM25 = (Math.log((N - n_i + 0.5) / (n_i + 0.5))) *
                                (((k_1 + 1) * f_i) / (K + f_i)) * ((k_2 + 1) * count / (k_2 + count));
                    }

                    twitteridMap.put(idCount[0], BM25);
                }

            }
        }



        return twitteridMap;
    }

    static public String doGetDocs(File docPath, Map<String, Double> docIdList, long numOfResults) throws IOException {

        if (docIdList.size() == 0){
            return "[]";
        }

        BufferedReader reader2 = new BufferedReader(new FileReader(docPath));

        String tempString2;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        Map<String, String[]> selectedTweets = new HashMap<>();

        SortedSet<Map.Entry<String, Double>> sortedDocIdList = new TreeSet<>(
                new Comparator<Map.Entry<String, Double>>() {
                    @Override
                    public int compare(Map.Entry<String, Double> longDoubleEntry, Map.Entry<String, Double> t1) {
                        int compareValue = t1.getValue().compareTo(longDoubleEntry.getValue()); //descending;
                        if (compareValue == 0){
                            compareValue = longDoubleEntry.getKey().compareTo(t1.getKey());
                        }
                        return compareValue;
                    }
                }
        );

        sortedDocIdList.addAll(docIdList.entrySet());

        while ((tempString2 = reader2.readLine()) != null) {
            String[] fields = tempString2.split("\t");

            if (fields.length != 6) {
                continue;
            }
            //twitterid = fields[0];
            //username = fields[3];
            //Tweets = fields[4];
            if (docIdList.containsKey(fields[0])) {
                String[] tweets = {fields[3],fields[4]};
                selectedTweets.put(fields[0], tweets);
            }
        }

        long countDoc = 0;

        for (Map.Entry<String, Double> docId : sortedDocIdList) {
            if (countDoc > numOfResults){
                break;
            }

            String[] tweets = selectedTweets.get(docId.getKey());
            if (tweets != null) {
                countDoc++;
                jsonArrayBuilder.add(
                        Json.createObjectBuilder()
                                .add("username", tweets[0])
                                .add("tweets", tweets[1])
                );
            }
        }

        return jsonArrayBuilder.build().toString();
    }

    private static ArrayList<String> doParseQuery(String query) throws IOException {

        final ArrayList<String> arrayList = new ArrayList<>();

        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(StandardFilterFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(KStemFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class)
                .build();
        TokenStream tokenStream = analyzer.tokenStream("content", query);
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

        tokenStream.reset();

        while (tokenStream.incrementToken()) {
            arrayList.add(charTermAttribute.toString());
        }

        tokenStream.close();

        return arrayList;
    }
}
