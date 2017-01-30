/**
 * Pick basketball and NBA as key words that describe the topic. Form a graph
 * by collecting at least 1000 pages. Store the graph constructed in a file
 * named MyWikiGraph.txt.
 * 
 * @author YAN DENG
 *
 */
public class MyWikiCrawler {
	public static void main(String[] args) throws IllegalAccessException {
		System.out.println("MyWikiCrawler is running......");
		long startTime = System.currentTimeMillis();
		String[] keywords = { "basketball", "NBA" };
		WikiCrawler tennisCrawler = new WikiCrawler("/wiki/Basketball", keywords, 2000, "MyWikiGraph.txt");
		tennisCrawler.crawl();
		long endTime = System.currentTimeMillis();
		long usedTime = endTime - startTime;
		System.out.println("My crawler took nearly "+ usedTime/1000+" seconds.");
	}
}
