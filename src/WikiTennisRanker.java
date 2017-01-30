import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Construct a wiki tennis graph. Compute the page ranks with ε = 0.01 as
 * approximation factor. Output pages with highest page rank, highest in-degree
 * and highest out-degree. Compute the following sets: Top 15 pages as per page
 * rank, top 15 pages as per in-degree and top 15 pages as per out degree. For
 * each pair of the sets, compute Jaccard Similarity. Repeat the same with ε =
 * 0.005 as approximation factor. The results will be both printed on screen and
 * stored in a file "Result_GraphFileName"
 * 
 * @author YAN DENG
 *
 */
public class WikiTennisRanker {
	public static void main(String[] args) throws FileNotFoundException {
		String[] keywords = { "tennis", "grand slam" };
		//String fileName = "WikiTennisGraph.txt";
		//String fileName = "PavanWikiTennis.txt";
		String fileName="test.txt";
		double epsilon = 0.01;
		//WikiCrawler tennisCrawler = new WikiCrawler("/wiki/Tennis", keywords, 1000, fileName);
		//tennisCrawler.crawl();
		PageRank ranker = new PageRank(fileName, epsilon);
		ranker.outputTopKResult(6);
	}
}
