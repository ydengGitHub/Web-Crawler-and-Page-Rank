/**
 * The seed url is /wiki/Tennis. This program should crawl 1000 pages that
 * contain the keys words tennis and grand slam. Store the graph constructed in
 * a file named WikiTennisGraph.txt
 * 
 * @author YAN DENG
 *
 */
public class WikiTennisCrawler {

	public static void main(String[] args) throws IllegalAccessException {
		long startTime = System.currentTimeMillis();
		System.out.println("WikiTennisCrawler is running......");
		String[] keywords = { "tennis", "grand slam" };
		WikiCrawler tennisCrawler = new WikiCrawler("/wiki/Tennis", keywords, 1000, "WikiTennisGraph.txt");
		tennisCrawler.crawl();
		long endTime = System.currentTimeMillis();
		long usedTime = endTime - startTime;
		System.out.println("My crawler took nearly " + usedTime / 1000 + " seconds.");
	}
}
