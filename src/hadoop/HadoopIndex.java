package hadoop;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class HadoopIndex extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String queryString = req.getParameter("query");
        System.out.println(queryString);
        File indexFile = new File("/mnt/hgfs/task/Executable/finalresult");
        File docFile = new File("/mnt/hgfs/task/TweetsTest1");
        Map<String,Double> twitteridMap = IndexReader.doGetIndex(indexFile,queryString);
        String jsonString = IndexReader.doGetDocs(docFile,twitteridMap,200);
        PrintWriter pw = resp.getWriter();
        pw.write(jsonString);

    }
}
