package lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.KStemFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

public class LuceneIndex extends HttpServlet {

    private static final int hitsPerPage = 20;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("/mnt/hgfs/task/IndexOutput")));
        IndexSearcher searcher = new IndexSearcher(reader);

        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(StandardFilterFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(KStemFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class)
                .build();

        String queryString = req.getParameter("query");

        QueryParser parser = new QueryParser("content", analyzer);

        try {
            Query query = parser.parse(queryString);
            TopDocs results = searcher.search(query, 10 * hitsPerPage);
            ScoreDoc[] hits = results.scoreDocs;
            Document doc;
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

            int numTotalHits = Math.toIntExact(results.totalHits);
            System.out.println(numTotalHits + " total matching documents");

            for (ScoreDoc hit:hits){
                doc = searcher.doc(hit.doc);
                jsonArrayBuilder.add(
                        Json.createObjectBuilder()
                                .add("username",doc.get("username"))
                                .add("tweets",doc.get("tweets"))
                );
            }

            JsonArray jsonArray = jsonArrayBuilder.build();

            PrintWriter pw = resp.getWriter();

            pw.write(jsonArray.toString());

        }
        catch (ParseException ex){
            System.out.println("Lucene Query Parser Exception: " + ex.getCause());
        }



    }
}
