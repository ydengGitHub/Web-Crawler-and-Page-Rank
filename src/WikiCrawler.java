import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class build a web graph of the crawled pages and have methods that can
 * be used to do focused crawling on Wiki.
 * 
 * @author YAN DENG
 *
 */
public class WikiCrawler {

	private String seedUrl;
	private HashSet<String> keywordsSet;
	private int max;
	private String fileName;
	private HashSet<String> disallowedSites; // disallowed by robots.txt
	public static final String BASE_URL = "https://en.wikipedia.org";
	private HashSet<String> visitedSet;
	private LinkedList<String> visitedList;
	private Queue<String> waitingQ;
	private HashMap<String, LinkedList<String>> graph;
	private int requestsCount = 0; // waited for 5 seconds after every 100
									// request
	private int numOfEdges = 0;

	/**
	 * 
	 * @param seedUrl
	 *            relative address of the seed url
	 * @param keywords
	 *            contains key words that describe a topic
	 * @param max
	 *            representing Maximum number sites to be crawled
	 * @param fileName
	 *            representing name of a file–The graph will be written to this
	 *            file
	 */
	public WikiCrawler(String seedUrl, String[] keywords, int max, String fileName) {
		this.seedUrl = seedUrl.trim();
		if (keywords == null || keywords.length == 0) {
			throw new IllegalArgumentException("Key words can not be empty.");
		} else {
			keywordsSet = new HashSet<String>();
			for (String s : keywords) {
				this.keywordsSet.add(s.toLowerCase());
			}
		}
		if (max <= 0) {
			throw new IllegalArgumentException("Maximum number sites to be crawled should be greater than 0.");
		} else {
			this.max = max;
		}
		this.fileName = fileName;
		this.disallowedSites = getDisallowedSites();
		visitedSet = new HashSet<String>();
		visitedList = new LinkedList<String>();
		waitingQ = new LinkedList<String>();
		graph = new HashMap<String, LinkedList<String>>();
		if (!(isValidPage(seedUrl) && seedUrl.startsWith("/wiki/"))) {
			throw new IllegalArgumentException(seedUrl + " is not a valid wiki page.");
		} else if (!isAboutTopics(seedUrl)) {
			throw new IllegalArgumentException(seedUrl + " does not contain all keywords.");
		}
	}

	/**
	 * collect max many pages. Every collected page must have all of the words
	 * from keywords. This method construct the web graph of all collected pages
	 * and write the graph to the file fileName. This file will list all edges
	 * of the graph. Each line of this file should have one directed edge,
	 * except the first line. The first line of the graph should indicate number
	 * of vertices. Below is sample contents of the file: 100 /wiki/tennis
	 * /wiki/Tennis_ball
	 */
	public void crawl() {
		System.out.println("WikiCrawler is crawling......");
		visitedSet.add(seedUrl);
		visitedList.add(seedUrl);
		waitingQ.add(seedUrl);
		while (!waitingQ.isEmpty()) {
			String url = waitingQ.poll();
			// System.out.println("Working on " + url + "; WaitingQ size: " +
			// waitingQ.size() + "; Number of request: "
			// + requestsCount + "; visited sites: " + visitedList.size());
			extractLinks(url);
		}
		outputGraph(fileName);
		System.out.println("The graph has " + numOfEdges + " edges and my crawler program sent requests to wiki "
				+ requestsCount + " times.");
	}

	/**
	 * Convert the relative address to absolute html address
	 * 
	 * @param url
	 *            relative address
	 * @return absolute address
	 * @throws MalformedURLException
	 */
	private URL absoluteAddress(String url) throws MalformedURLException {
		return new URL("https://en.wikipedia.org" + url);
	}

	/**
	 * Convert the relative address to text page address
	 * 
	 * @param url
	 *            relative address
	 * @return text page address
	 * @throws MalformedURLException
	 */
	private URL textPageAddress(String url) throws MalformedURLException {
		String title = url.substring(6);
		String s = "https://en.wikipedia.org/w/index.php?title=" + title + "&action=raw";
		return new URL(s);
	}

	/**
	 * Check whether the given link is a valid link (links contains "#" and ":"
	 * are either links to images or links to sections of other pages; and
	 * should not crawl any site that is disallowed by robots.txt).
	 * 
	 * @param url
	 * @return true if it's a valid page, false else
	 */
	private boolean isValidPage(String url) {
		for (int i = 0; i < url.length(); i++) {
			if (url.charAt(i) == '#' || url.charAt(i) == ':') {
				return false;
			}
		}
		if (disallowedSites.contains(url)) {
			// System.out.println(url + " is disallowed by robots.txt.");
			return false;
		}
		return true;
	}

	/**
	 * Read the robots.txt file and add disallowed sites to a hashset
	 * 
	 * @return the hashset
	 */
	private HashSet<String> getDisallowedSites() {
		HashSet<String> sites = new HashSet<String>();
		try {
			URL robotsUrl = absoluteAddress("/robots.txt");
			InputStream is = robotsUrl.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			requestsCount++;
			String line = br.readLine();
			while (line != null) {
				if (line.contains("Disallow: /wiki/")) {
					int index = line.indexOf("/wiki/");
					// System.out.println(line.substring(index).trim());
					sites.add(line.substring(index).trim());
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			System.err.println("Failed to get disallowed sites from robots.txt. Waiting for 3 seconds...");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return getDisallowedSites();
		}
		return sites;
	}

	/**
	 * Output the disallowed sites to the file
	 * 
	 * @param fileName
	 */
	public void outputDisallowedSites(String fileName) {
		File outputFile = new File(fileName);
		System.out.println("Writing file......");
		try {
			PrintWriter writer = new PrintWriter(outputFile);
			for (String s : disallowedSites) {
				writer.append(s + "\n");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Extract the links from actual text component of given url, while
	 * collected pages < “max” number of pages going to collect, add non repeat
	 * valid links that contains all keywords to visitedList and waitingQ, and
	 * update the graph (edges).
	 * 
	 * 
	 * @param url
	 */
	private void extractLinks(String url) {
		HashSet<String> edgeSet = new HashSet<String>();
		LinkedList<String> edgeList = new LinkedList<String>();

		if (requestsCount % 100 == 0) {
			try {
				System.out.println("Waiting 5 seconds after every 100 requests......");
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		URL source = null;
		try {
			source = absoluteAddress(url);
		} catch (MalformedURLException e) {
			System.err.println("Bad url: " + url);
			return;
		}
		try {
			InputStream is = source.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			requestsCount++;
			String line = br.readLine();
			while (!line.contains("<p>")) {
				line = br.readLine();
			}
			while (line != null) {
				String[] links = line.split("href=");
				for (int i = 1; i < links.length; i++) {
					int startIndexOfLink = links[i].indexOf('"') + 1;
					int endIndexOfLink = links[i].indexOf('"', startIndexOfLink + 1);
					String link;
					try {
						link = links[i].substring(startIndexOfLink, endIndexOfLink).trim();
					} catch (StringIndexOutOfBoundsException e) {
						// System.out.println("Could not get link from: " +
						// links[i]);
						continue;
					}
					if (!edgeSet.contains(link) && !link.equals(url)) {
						if (visitedSet.contains(link)) {
							edgeSet.add(link);
							edgeList.add(link);
						} else if (visitedSet.size() < max) {
							if (link.startsWith("/wiki/") && isValidPage(link)) {
								if (isAboutTopics(link)) {
									edgeSet.add(link);
									edgeList.add(link);
									visitedSet.add(link);
									visitedList.add(link);
									waitingQ.add(link);
								}
							}
						}

					}
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			System.err.println("Failed to oepn url stream: " + url);
			return;
		}
		graph.put(url, edgeList);
	}

	/**
	 * Check whether given url contains all key words
	 * 
	 * @param url
	 * @return true if given url contains all key words; false otherwise
	 * @throws FileNotFoundException
	 */
	private boolean isAboutTopics(String url) {

		HashSet<String> topics = (HashSet<String>) keywordsSet.clone();
		URL source = null;
		try {
			source = textPageAddress(url);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			System.err.println("Bad url: " + source);
			return false;
		}
		try {
			if (requestsCount % 100 == 0) {
				try {
					System.out.println("Waiting 5 seconds after every 100 requests......");
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			InputStream is = source.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			requestsCount++;
			String line = br.readLine();
			while (!topics.isEmpty()) {
				if (line == null) {
					// System.out.printf("\n%s is NOT about the topics.\n\n",
					// url);
					return false;
				}
				String lineLowerCase = line.toLowerCase();
				for (Iterator<String> it = topics.iterator(); it.hasNext();) {
					String s = it.next();
					if (lineLowerCase.contains(s)) {
						it.remove();
					}
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			System.err.println("Failed to open url in stream: " + url+". Skipped.");
			return false;
		}
		// System.out.println(url +" is about given Topics.");
		return true;
	}

	/**
	 * Output the graph to the file with given fileName
	 * 
	 * @param fileName
	 */
	private void outputGraph(String fileName) {
		File file = new File(fileName);
		try {
			PrintWriter writer = new PrintWriter(file);
			writer.println(max);
			for (String s1 : visitedList) {
				LinkedList<String> edges = graph.get(s1);
				for (String s2 : edges) {
					writer.println(s1 + " " + s2);
					numOfEdges++;
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
