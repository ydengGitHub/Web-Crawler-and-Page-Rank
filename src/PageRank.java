import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;

/**
 * This class have methods to compute page rank of nodes/pages of a web graph.
 * 
 * @author YAN DENG
 *
 */
public class PageRank {

	private String graphFileName;
	private double epsilon;
	/* <vertex i, LinkedList<vertex j, such that (i,j) is an edge>> */
	private HashMap<String, LinkedList<String>> graphOut;
	/* <vertex i, LinkedList<vertex j, such that (j,i) is an edge>> */
	private HashMap<String, LinkedList<String>> graphIn;
	/* Store the vertices/pages in an array */
	private String[] verticesArray;
	/* Used to check the index of given page */
	private HashMap<String, Integer> verticesIndex;
	private int numOfVertices;
	private int numOfEdges;
	private double[] rankVector;
	private final double BETA = 0.85;
	private int numberOfSteps = 0;
	private String[] topKRanks;
	private String[] topKInDegrees;
	private String[] topKOutDegrees;

	/**
	 * 
	 * @param fileName
	 *            Name of a file that contains the edges of the graph.
	 * @param epsilon
	 *            Approximation parameter for pagerank.
	 * @throws FileNotFoundException
	 */
	public PageRank(String fileName, double epsilon) throws FileNotFoundException {
		this.graphFileName = fileName;
		this.epsilon = epsilon;
		this.graphOut = new HashMap<String, LinkedList<String>>();
		this.graphIn = new HashMap<String, LinkedList<String>>();
		this.verticesIndex = new HashMap<String, Integer>();
		this.numOfVertices = readGraph();
		this.rankVector = computeRank();
	}

	/**
	 * gets name of vertex of the graph as parameter and returns its page rank
	 * 
	 * @param url
	 * @return
	 */
	public double pageRankOf(String url) {
		double rank = rankVector[verticesIndex.get(url)];
		return rank;
	}

	/**
	 * gets name of vertex of the graph as parameter and returns its out degree
	 * 
	 * @param url
	 * @return out degree of given vertex
	 */
	public int outDegreeOf(String url) {
		int outDegree;
		if (graphOut.get(url) == null) {
			outDegree = 0;
		} else {
			outDegree = graphOut.get(url).size();
		}
		return outDegree;
	}

	/**
	 * gets name of vertex of the graph as parameter and returns its in degree
	 * 
	 * @param url
	 * @return in degree of given vertex
	 */
	public int inDegreeOf(String url) {
		int inDegree;
		if (graphIn.get(url) == null) {
			inDegree = 0;
		} else {
			inDegree = graphIn.get(url).size();
		}
		return inDegree;
	}

	/**
	 * @return the number of edges in the graph
	 */
	public int numEdges() {
		return numOfEdges;
	}

	/**
	 * gets an integer k as parameter and returns an array (of strings) of pages
	 * with top k page ranks.
	 * 
	 * @param k
	 * @return top k rank pages
	 */
	public String[] topKPageRank(int k) {
		topKRanks = topK(rankVector, k);
		return topKRanks;
	}

	/**
	 * gets an integer k as parameter and returns an array (of strings) of pages
	 * with top k in degree.
	 * 
	 * @param k
	 * @return top k in degree pages
	 */
	public String[] topKInDegree(int k) {
		double[] inDegrees = new double[numOfVertices];
		for (int i = 0; i < numOfVertices; i++) {
			inDegrees[i] = inDegreeOf(verticesArray[i]);
		}
		topKInDegrees = topK(inDegrees, k);
		return topKInDegrees;
	}

	/**
	 * gets an integer k as parameter and returns an array (of strings) of pages
	 * with top k out degree.
	 * 
	 * @param k
	 * @return top k out degree pages
	 */
	public String[] topKOutDegree(int k) {
		double[] outDegrees = new double[numOfVertices];
		for (int i = 0; i < numOfVertices; i++) {
			outDegrees[i] = outDegreeOf(verticesArray[i]);
		}
		topKOutDegrees = topK(outDegrees, k);
		return topKOutDegrees;
	}

	/**
	 * Return top k elements' indexes of the given array
	 * 
	 * @param data
	 * @param k
	 * @return String array containing the top k pages
	 */
	private String[] topK(double[] data, int k) {
		String[] result = new String[k];
		Pair[] allPairs = new Pair[numOfVertices];
		for (int i = 0; i < numOfVertices; i++) {
			allPairs[i] = new Pair(i, data[i]);
		}
		Arrays.sort(allPairs);
		for (int j = 0; j < k; j++) {
			result[j] = verticesArray[allPairs[numOfVertices - 1 - j].index];
		}
		return result;
	}

	/**
	 * Inner class to store the index/value pair with override compareTo method
	 * for sorting
	 * 
	 * @author YAN
	 *
	 */
	private class Pair implements Comparable<Pair> {
		private int index;
		private double value;

		public Pair(int index, double value) {
			this.index = index;
			this.value = value;
		}

		@Override
		public int compareTo(Pair otherPair) {
			if (this.value == otherPair.value) {
				return 0;
			} else if (this.value > otherPair.value) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	/**
	 * Read the file that contains the edges of the graph, and store the
	 * information in the HashMap
	 * 
	 * @throws FileNotFoundException
	 */
	private int readGraph() throws FileNotFoundException {
		File graphFile = new File(this.graphFileName);
		Scanner scanner = new Scanner(graphFile);
		String line = scanner.nextLine();
		int numOfVertices = Integer.parseInt(line.trim());
		HashSet<String> vertices = new HashSet<String>();
		while (scanner.hasNextLine()) {
			numOfEdges++;
			String[] links = scanner.nextLine().split(" ");
			vertices.add(links[0]);
			vertices.add(links[1]);
			if (graphOut.containsKey(links[0])) {
				graphOut.get(links[0]).add(links[1]);
			} else {
				LinkedList<String> list = new LinkedList<String>();
				list.add(links[1]);
				graphOut.put(links[0], list);
			}
			if (graphIn.containsKey(links[1])) {
				graphIn.get(links[1]).add(links[0]);
			} else {
				LinkedList<String> list = new LinkedList<String>();
				list.add(links[0]);
				graphIn.put(links[1], list);
			}
		}
		scanner.close();
		if (numOfVertices != vertices.size()) {
			System.err.println("Warning: Number of vertices is not consistant. Given: " + numOfVertices + "; Real: "
					+ vertices.size());
		}
		verticesArray = new String[numOfVertices];
		int index = 0;
		for (String key : vertices) {
			verticesIndex.put(key, index);
			verticesArray[index] = key;
			index++;
		}
		return numOfVertices;
	}

	/**
	 * Simulate one step of the random walk.
	 * 
	 * @param pN
	 * @param beta
	 * @return pN+1
	 */
	private double[] simulateOneStep(double[] pN) {
		double[] pNPlusOne = new double[numOfVertices];
		double defaultValue = (1.0 - BETA) / numOfVertices;
		for (int i = 0; i < numOfVertices; i++) { // Change to numOfVertices
			pNPlusOne[i] = defaultValue;
		}
		for (String keyC : verticesArray) {
			System.out.println("Edge: " + keyC + "-->");
			int index = verticesIndex.get(keyC);

			//Change
			int numOfEdges = 0;
			if (graphOut.get(keyC) != null) {
				numOfEdges = graphOut.get(keyC).size();
			}
			//Change End
			System.out.println("# Outgoing edges: " + numOfEdges);
			if (numOfEdges == 0) {
				double value = BETA * pN[index] / numOfVertices;
				for (int i = 0; i < numOfVertices; i++) {
					pNPlusOne[i] += value;
				}
			} else {
				double value = BETA * pN[index] / numOfEdges;
				for (String keyR : graphOut.get(keyC)) {
					int linkedIndex = verticesIndex.get(keyR);
					pNPlusOne[linkedIndex] = pNPlusOne[linkedIndex] + value;
				}
			}
		}
		double sum = 0;
		for (int j = 0; j < pN.length; j++) {
			sum += pN[j];
			System.out.println(j + ". " + pN[j]);
		}
		System.out.println("Sum: " + sum);
		System.out.println();
		return pNPlusOne;
	}

	/**
	 * Compute the Rank vector
	 * 
	 * @return the Rank vector
	 */
	private double[] computeRank() {
		boolean converged = false;
		double defaultValue = 1.0 / numOfVertices;
		double[] pN = new double[numOfVertices];
		double[] pNPlusOne = null;
		for (int i = 0; i < numOfVertices; i++)
			pN[i] = defaultValue;
		while (!converged) {
			System.out.println("Step " + numberOfSteps);
			pNPlusOne = simulateOneStep(pN);
			if (computeNormDifference(pNPlusOne, pN) <= epsilon) {
				converged = true;
			}
			pN = pNPlusOne;
			numberOfSteps++;
		}
		System.out.println("It takes " + numberOfSteps + " steps to compute the rank vector.");
		return pN;
	}

	/**
	 * Compute the sum absolute values of all entries of pNPlusOne[i]-pN[i]
	 * 
	 * @param pNPlusOne
	 * @param pN
	 * @return the NORM value
	 */
	private double computeNormDifference(double[] pNPlusOne, double[] pN) {
		double norm = 0;
		for (int i = 0; i < pN.length; i++) {
			norm += Math.abs(pNPlusOne[i] - pN[i]);
		}
		return norm;
	}

	/**
	 * Output pages with highest page rank, highest in-degree and highest
	 * out-degree. Output the following sets: Top k pages as per page rank, top
	 * k pages as per in-degree and top k pages as per out degree. For each pair
	 * of the sets, compute Jaccard Similarity. The results will be both printed
	 * on screen and stored in a file "Result_GraphFileName"
	 * 
	 * @param k
	 * @throws FileNotFoundException
	 */
	public void outputTopKResult(int k) throws FileNotFoundException {
		File outputFile = new File("Result_" + this.graphFileName);
		PrintWriter writer = new PrintWriter(outputFile);

		if (topKInDegrees == null || topKInDegrees.length == 0 || topKOutDegrees == null || topKOutDegrees.length == 0
				|| topKRanks == null || topKRanks.length == 0) {
			this.topKPageRank(k);
			this.topKInDegree(k);
			this.topKOutDegree(k);
		}

		String tmpStr = "Page with Highest Rank: " + topKRanks[0] + " with Rank: " + this.pageRankOf(topKRanks[0]);
		output(tmpStr, writer);
		tmpStr = "Page with Highest in-degree: " + topKInDegrees[0] + " with in-degree: "
				+ this.inDegreeOf(topKInDegrees[0]);
		output(tmpStr, writer);
		tmpStr = "Page with Highest out-degree: " + topKOutDegrees[0] + " with out-degree: "
				+ this.outDegreeOf(topKOutDegrees[0]);
		output(tmpStr, writer);

		tmpStr = "Top " + k + " highest rank pages:";
		output(writer);
		output(tmpStr, writer);
		int i = 1;
		for (String s1 : topKRanks) {
			output(i + ".\t" + s1 + " with Rank: " + this.pageRankOf(topKRanks[i - 1]), writer);
			i++;
		}

		double sum = 0;
		for (int j = 0; j < 6; j++) {
			sum += this.pageRankOf(topKRanks[j]);
		}

		tmpStr = "Number of Vertices: " + numOfVertices + "; Number of Edges: " + numOfEdges + "; Sum: " + sum;
		System.out.println();
		output(tmpStr, writer);

		/*
		 * tmpStr = "Top " + k + " highest in-degree pages:"; output(writer);
		 * output(tmpStr, writer); i=1; for (String s2 : topKInDegrees) {
		 * output(i++ +".\t"+s2, writer); }
		 * 
		 * tmpStr = "Top " + k + " highest out-degree pages:"; output(writer);
		 * output(tmpStr, writer); i=1; for (String s3 : topKOutDegrees) {
		 * output(i++ +".\t"+s3, writer); }
		 * 
		 * double jacRankIn = computeJac(topKRanks, topKInDegrees); tmpStr = (
		 * "Jaccard Similarity between Top " + k +
		 * " rank pages and Indegree pages is: " + jacRankIn); output(writer);
		 * output(tmpStr, writer);
		 * 
		 * double jacRankOut = computeJac(topKRanks, topKOutDegrees); tmpStr = (
		 * "Jaccard Similarity between Top " + k +
		 * " rank pages and Outdegree pages is: " + jacRankOut); output(writer);
		 * output(tmpStr, writer);
		 * 
		 * double jacInOut = computeJac(topKInDegrees, topKOutDegrees); tmpStr =
		 * ("Jaccard Similarity between Top " + k +
		 * " Indegree pages and Outdegree pages is: " + jacInOut);
		 * output(writer); output(tmpStr, writer);
		 */

		writer.close();
	}

	/**
	 * Compute the Jaccard similarities between the pages of 2 given array.
	 * 
	 * @param pages1
	 * @param pages2
	 */
	public double computeJac(String[] pages1, String[] pages2) {
		if (pages1 == null || pages1.length == 0 || pages2 == null || pages2.length == 0) {
			throw new IllegalArgumentException("The list is empty.");
		}
		HashMap<String, Integer> terms = new HashMap<String, Integer>();
		Integer index = 0;
		for (String s1 : pages1) {
			if (!terms.containsKey(s1)) {
				terms.put(s1, index++);
			}
		}
		for (String s2 : pages2) {
			if (!terms.containsKey(s2)) {
				terms.put(s2, index++);
			}
		}
		int numOfTerms = terms.size();
		int[] sig1 = new int[numOfTerms];
		int[] sig2 = new int[numOfTerms];
		for (String s1 : pages1) {
			sig1[terms.get(s1)] = 1;
		}
		for (String s2 : pages2) {
			sig2[terms.get(s2)] = 1;
		}
		int intersection = 0;
		for (int i = 0; i < numOfTerms; i++) {
			if (sig1[i] == 1 && sig2[i] == 1) {
				intersection++;
			}
		}
		return ((double) intersection) / numOfTerms;
	}

	/**
	 * Helper method. Output the given string both on screen and to the file.
	 * 
	 * @param s
	 * @param writer
	 */
	private void output(String s, PrintWriter writer) {
		System.out.println(s);
		writer.println(s);
	}

	/**
	 * Helper method. Output an empty line both on screen and to the file.
	 * 
	 * @param s
	 * @param writer
	 */
	private void output(PrintWriter writer) {
		System.out.println();
		writer.println();
	}
}
