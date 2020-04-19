package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;
import org.junit.Before;
import sun.jvm.hotspot.jdi.ArrayReferenceImpl;

public class WikiPhilosophy {

	final static List<String> visited = new ArrayList<>();
	final static List<String> badUrls = new ArrayList<>();
	static int redirects = 0;
	final static int redirectLimit = 50;
	final static WikiFetcher wf = new WikiFetcher();
	final static String philosophyPage = "/wiki/Philosophy";
	final static String BASE_URL = "https://en.wikipedia.org";
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
        // some example code to get you started

		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		Elements paragraphs = wf.fetchWikipedia(url);
		Element secondPara = paragraphs.get(1);
		boolean success = false;

		Iterable<Node> iter = new WikiNodeIterable(secondPara);
		try {
			for (Node node: iter) {
				if (isValid(node)) {
					visit(node);
					throw new Exception( "we did it");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			System.out.println("success: "+  philosophyPage + "found" + "\n" + "pages visited: " + visited.toString());
		}
	}

	private static boolean visit(Node node) throws Exception {
		// if element is wikipedia page return true
		Node bestNode = findBestNode(node);
		// make a list of nodes and check if one has philosophy
		final String url = bestNode.attr("href");

		if (url.equals(philosophyPage)) {
			return true;
		} else if (visited.contains(url)) {
			throw new Exception("Page already visited");
		} else {
			visited.add(url);
			Elements paragraphs = wf.fetchWikipedia(BASE_URL + url);
			Element firstPara = paragraphs.get(0);

			Iterable<Node> iter = new WikiNodeIterable(firstPara);
			for (Node childNode: iter) {
				if(isValid(childNode)) {
					return visit(childNode);
				}
			}
			if (redirects > redirectLimit) {
				throw new Exception("Philosophy page not found");
			}
			redirects++;
			badUrls.add(url);
			visit(node);
		}
		return false;
	}

	private static boolean isValid(Node node) {
		if (node.childNodes().size() > 0) {
			final Optional<Node> url = node.childNodes().stream().filter(childNode -> childNode.hasAttr("href")).findFirst();
			return url.isPresent();
		}
		return false;
	}

	private static Node findBestNode(Node node) {
		final List<Node> nodeList = node.childNodes().stream().filter(childNode -> childNode.hasAttr("href")).collect(Collectors.toList());
		nodeList.removeIf(removeable -> badUrls.contains(removeable.attr("href")));
		final Optional<Node> philosophyNode = nodeList.stream().filter(childNode -> childNode.attr("href").equals("/wiki/Philosophy")).findFirst();
		return philosophyNode.orElseGet(() -> nodeList.get(0));
	}
}
