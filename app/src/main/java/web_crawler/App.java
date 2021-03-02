package web_crawler;

import javax.swing.*;

public class App {

    public static void main(String[] args) {

        System.out.println("basics.collections.Main thread name: " + Thread.currentThread().getName());

        SwingUtilities.invokeLater(WebCrawler::new);
        
/*
        `WebCrawler` is a GUI window app that has the [Run/Stop] button.
         When pressed [Run] the event listener bounded to it creates an instance of the `SwingWorker<> SearchTask`
         class named `crawlerWorker`.
         This instance does all the actual work of searching the net and returns the result (as a net of `Node` class
         instances) and "in-process metrics" (as a `Metrics` class instance), using bound properties and standard
         SwingWorker<> methods (such as `process` and `done`)
         The `SearchTask` class is the main and most complicated part of the web crawler application.
         This class is responsible for multithreaded parsing and content analysis of the URL-links tree using
         the breadth-first search principle.
         Multithreading is organized using `ExecutorService executor = Executors.newFixedThreadPool(poolSize)`
         and `Queue<Node> nodesToProceed = new ConcurrentLinkedQueue<>()` inside overridden SwingWorker's method
         `doInBackground()`
*/

    }

}
