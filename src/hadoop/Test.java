package hadoop;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

public class Test {

    public static void main (String[] args) throws Exception{


        File indexFile = new File("/mnt/hgfs/task/Executable/finalresult");
        File docFile = new File("/mnt/hgfs/task/TweetsTest1");
        Map<String,Double> twitteridMap = IndexReader.doGetIndex(indexFile,"trump");
        String jsonString = IndexReader.doGetDocs(docFile,twitteridMap,200);
        System.out.println(jsonString);
    }
}
