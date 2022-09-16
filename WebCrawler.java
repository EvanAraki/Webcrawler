package edu.ics211.ec;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Creates a Web crawler.
 * 
 * @author Evan Araki
 *     Worked with Curtis Meares, Tyler Cho, Bao Nguyen
 *     Recieved (absolutely essential) help from Kana'i Golding in Panda discord
 */
public class WebCrawler {

  /**
   * Creates a web crawler.
   * 
   */
  //hash map reccomended by Kanai to get rid of duplicate nodes, 
  //which significantly reduces size of graph.
  public static HashMap<String, Node> graph = new HashMap<>();

  /**
   * Main.
   * 
   * @param args arguments.
   */
  public static void main(String[] args) {

    int depth = Integer.parseInt(args[1]);

    try {
      Node node = new Node(args[0], 0);
      depthFirstSearch(node, depth);
      reset();
      breadthFirstSearch(node, depth);
      
      //DEBUG
      //System.out.println();
      //System.out.println("DEBUG===============");
      //System.out.println("args: " + args[0]);
      //System.out.println("depth: " + depth);
      //System.out.println("node: " + node);

    } catch (IOException e) {
      //DEBUG
      //System.out.println(args[1]);
      //System.out.println("Invalid address, provide valid link: " + e);
    }
  }

  /**
   * Creates DepthFirstSearch method.
   * 
   * @param n the node.
   * @param maxDepth the depth of search.
   */
  
  //Pseudocode from:
  //https://www.hackerearth.com/practice/algorithms/graphs/depth-first-search/tutorial/
  public static void depthFirstSearch(Node n, int maxDepth) {
    Stack<Node> stack = new Stack<>();
    stack.push(n);
    Node currentNode;
    System.out.println();
    System.out.println("Depth First Seach Running");
    System.out.println();

    while (!stack.empty()) {
      currentNode = stack.pop();
      if (!currentNode.visited && currentNode.depth <= maxDepth) {
        currentNode.visited = true;
        graph.put(currentNode.web, currentNode);
        System.out.println("Current Link: " + currentNode.web 
            + " Depth of node: " + currentNode.depth);
        
        //DEBUG
        //System.out.println();
        //System.out.println("Hashmap: " + graph);
        //System.out.println();

        for (Node temp : currentNode.getChildren()) {
          if (temp.depth <= maxDepth) {
            stack.push(temp);
          }
        }
      }
    }
  }

  /**
   * Creates DepthFirstSearch method.
   * MF DFS with queues lmao, overthought this for too long.
   * @param n the node.
   * @param maxDepth the depth of search.
   */
  public static void breadthFirstSearch(Node n, int maxDepth) {
    Queue<Node> queue = new LinkedList<>();
    queue.offer(n);
    Node currentNode;
    System.out.println();
    System.out.println("Breadth First Seach Running");
    System.out.println();

    while (!queue.isEmpty()) {
      currentNode = queue.poll();
      if (!currentNode.visited && currentNode.depth <= maxDepth) {
        currentNode.visited = true;
        graph.put(currentNode.web, currentNode);
        System.out.println("Current Link: " 
              + currentNode.web + " Depth of node: " 
            + currentNode.depth);

        for (Node temp : currentNode.getChildren()) {
          if (temp.depth <= maxDepth) {
            queue.offer(temp);
          }
        }
      }
    }
  }

  //reset method to change visited nodes to unvisited so BFS can run.
  private static void reset() {
    for (Node node : graph.values()) {
      node.visited = false;
    }
  }

  
  
  private static class Node {
    ArrayList<Node> children = null; 
    String web; 
    boolean visited; //to keep track of visited nodes, as reccomended in EC document.
    int depth;
    
    //A mandatory IO exception thrower. 
    Node(String link, int depth) throws IOException {
      if (!urlCheck(link)) {
        throw new IOException();
      }
      this.web = link; 
      this.depth = depth;
      
      //DEBUG
      //System.out.println("NODE OBJECT=========== ");
      //System.out.println("children AList: " + children);
    }
    
    


    private static boolean urlCheck(String url) {
      // regex to verify link from panda discord
      //https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html

      try {
        String regex = "((http|https)://)(www.)[a-z" 
            + "A-Z0-9@:%._\\+~#?&//=]{2,256}\\.(com|edu|mil|gov|org)"
            + "\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)";

        Pattern pattern = Pattern.compile(regex);

        Matcher m = pattern.matcher(url);

        return m.matches();

      } catch (NumberFormatException e) {
        return false;
      }

    }


    private static String getWebsite(String link) throws IOException, InterruptedException {
      //Search website for links
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(link))
          .GET()
          .build();
      
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      
      return response.body();
    }


    // https://stackoverflow.com/questions/5120171/extract-links-from-a-web-page
    private ArrayList<String> getLinks(String url) {

      ArrayList<String> result = new ArrayList<>();

      //DEBUG
      //System.out.println();
      //System.out.println("GETLINKS METHOD RUNNING");
      //System.out.println();
      
      
      String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(url);
      while (m.find()) {
        result.add(m.group());
      }
      
      //DEBUG
      //System.out.println("GETLINKS METHOD=========");
      //System.out.println(result);
      
      return result;
    }


    private ArrayList<Node> getChildren() {

      if (children != null) {
        return children;
      }
      ArrayList<Node> childrenList = new ArrayList<>();
 
      try {
        ArrayList<String> linksList = getLinks(getWebsite(web));
        for (String link : linksList) {
          try {
            //increment depth, this places child under parent. 
            Node child = new Node(link, depth + 1);
            childrenList.add(child);
          } catch (IOException e) {
            // do nothing
          }
        }
      } catch (IOException | InterruptedException a) {
        // do nothing
      }
      children = childrenList;
      
      //DEBUG
      //System.out.println("GET CHILDREN=========");
      //System.out.println(childrenList);

      
      return childrenList;

    }

  }

}
