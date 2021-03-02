package web_crawler;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "UseDPIAwareInsets"})
public class WebCrawler extends JFrame {
  
  // Web crawler Class fields & properties
  private final Set<Node> foundNodes = new LinkedHashSet<>(); // linked to preserve order
  SwingWorker<Void, Node> crawlerWorker;
  private final int defaultThreads = Helpers.getOptimalPoolSizeForIOBoundTasks(0.9);
  
  // Components
  final private JLabel labelUrl = new JLabel("Start URL: ");
  final private JTextField textFieldUrl = new JTextField(30);
  final private JToggleButton toggleButtonRun = new JToggleButton("Run");
  final private JLabel labelWorkers = new JLabel("Workers: ");
  final private JTextField textFieldWorkersNumber = new JTextField();
  final private JCheckBox checkBoxEnableAutoWorkersNumber = new JCheckBox("Auto");
  final private JLabel labelDepth = new JLabel("Maximum depth: ");
  final private JTextField textFieldDepth = new JTextField();
  final private JLabel labelTimeLimit = new JLabel("Time limit: ");
  final private JTextField textFieldTimeLimit = new JTextField();
  final private JLabel labelSeconds = new JLabel(" seconds ");
  final private JLabel labelElapsed = new JLabel("Elapsed time: ");
  final private JLabel labelElapsedValue = new JLabel("00:00");
  final private JLabel labelParsed = new JLabel("Parsed pages: ");
  final private JLabel labelParsedValue = new JLabel("0");
  final private JLabel labelNodesToProceed = new JLabel("Nodes to proceed: ");
  final private JLabel labelNodesToProceedValue = new JLabel("0");
  final private JLabel labelActiveWorkers = new JLabel("Active workers: ");
  final private JLabel labelActiveWorkersValue = new JLabel("0");
  final private JLabel labelFoundNodes = new JLabel("Nodes found: ");
  final private JLabel labelFoundNodesValue = new JLabel("0");
  final private JLabel labelReachedLevel = new JLabel("Reached level: ");
  final private JLabel labelReachedLevelValue = new JLabel("0");
  final private JLabel labelLastParsedLevel = new JLabel("Last parsed page level: ");
  final private JLabel labelLastParsedLevelValue = new JLabel("0");
  final private JLabel labelNodesInProgress = new JLabel("Nodes in progress: ");
  final private JLabel labelNodesInProgressValue = new JLabel("0");
  final private JCheckBox checkBoxEnableMaxDepth = new JCheckBox("Enabled");
  final private JLabel labelParsedExport = new JLabel("Save to file: ");
  final private JTextField textFieldExportFile = new JTextField();
  final private JButton buttonSave = new JButton("Save");
  final private JCheckBox checkBoxEnableTimeLimit = new JCheckBox("Enabled");
  
  // File chooser
  final JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
  
  WebCrawler() {
    
    super("Web Crawler Control Tool");
    
    System.out.println("JFrame thread name: " + Thread.currentThread().getName()
      + (SwingUtilities.isEventDispatchThread() ? ". This is the event dispatch thread " : ""));
    
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension windowSize = new Dimension(screenSize.width / 3, screenSize.height / 3);
    setSize(windowSize);
    setLocationRelativeTo(null);
    
    // Components names
    textFieldUrl.setName("UrlTextField");
    toggleButtonRun.setName("RunButton");
    textFieldDepth.setName("DepthTextField");
    checkBoxEnableMaxDepth.setName("DepthCheckBox");
    labelParsed.setName("ParsedLabel");
    textFieldExportFile.setName("ExportUrlTextField");
    buttonSave.setName("ExportButton");
    
    createGridBagLayoutForm();
    addListeners();
    
    pack();
    setVisible(true);
    
  }
  
  @SuppressWarnings("DuplicatedCode")
  private void createGridBagLayoutForm() {
    // Creating form layout
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    
    // Adding components to the grid
    c.ipadx = 3;
    c.ipady = 3;
    c.weighty = 0;
    
    // Start URL row
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    c.insets = new Insets(15, 15, 5, 5);
    add(labelUrl, c);
    c.gridx = 1;
    c.gridwidth = 2;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(15, 5, 5, 5);
    add(textFieldUrl, c);
    c.weightx = 0;
    c.gridwidth = 1;
    c.gridx = 3;
    c.insets = new Insets(15, 5, 5, 15);
    add(toggleButtonRun, c);
    
    // Workers row
    c.gridx = 0;
    c.gridy = 1;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelWorkers, c);
    c.gridx = 1;
    c.gridwidth = 2;
    c.weightx = 1;
    c.insets = new Insets(5, 5, 5, 5);
    textFieldWorkersNumber.setText(String.valueOf(defaultThreads));
    add(textFieldWorkersNumber, c);
    c.weightx = 0;
    c.gridwidth = 1;
    c.gridx = 3;
    c.insets = new Insets(5, 5, 5, 15);
    checkBoxEnableAutoWorkersNumber.setSelected(false);
    add(checkBoxEnableAutoWorkersNumber, c);
    
    // Depth row
    c.gridx = 0;
    c.gridy = 2;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelDepth, c);
    c.gridx = 1;
    c.gridwidth = 2;
    c.weightx = 1;
    c.insets = new Insets(5, 5, 5, 5);
    textFieldDepth.setText("2");
    textFieldDepth.setEnabled(false);
    add(textFieldDepth, c);
    c.weightx = 0;
    c.gridwidth = 1;
    c.gridx = 3;
    c.insets = new Insets(5, 5, 5, 15);
    checkBoxEnableMaxDepth.setSelected(false);
    add(checkBoxEnableMaxDepth, c);
    
    // Time limit row
    c.weightx = 0;
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 3;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelTimeLimit, c);
    c.gridx = 1;
    c.weightx = 1;
    c.insets = new Insets(5, 5, 5, 5);
    textFieldTimeLimit.setText("0");
    textFieldTimeLimit.setEnabled(false);
    add(textFieldTimeLimit, c);
    c.weightx = 0;
    c.gridwidth = 1;
    c.gridx = 2;
    add(labelSeconds, c);
    c.gridx = 3;
    c.insets = new Insets(5, 5, 5, 15);
    add(checkBoxEnableTimeLimit, c);
    
    // Metrics: elapsed time
    c.gridx = 0;
    c.gridy = 4;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelElapsed, c);
    c.gridx = 1;
    add(labelElapsedValue, c);
    
    // Metrics: parsed pages
    c.gridx = 0;
    c.gridy = 5;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelParsed, c);
    c.gridx = 1;
    add(labelParsedValue, c);
    
    // Metrics: nodes to proceed
    c.gridx = 0;
    c.gridy = 6;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelNodesToProceed, c);
    c.gridx = 1;
    add(labelNodesToProceedValue, c);
    
    // Metrics: active workers
    c.gridx = 0;
    c.gridy = 7;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelActiveWorkers, c);
    c.gridx = 1;
    add(labelActiveWorkersValue, c);
    
    // Metrics: found nodes
    c.gridx = 0;
    c.gridy = 8;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelFoundNodes, c);
    c.gridx = 1;
    add(labelFoundNodesValue, c);
    
    // Metrics: level reached
    c.gridx = 0;
    c.gridy = 9;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelReachedLevel, c);
    c.gridx = 1;
    add(labelReachedLevelValue, c);
    
    // Metrics: last parsed page level
    c.gridx = 0;
    c.gridy = 10;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelLastParsedLevel, c);
    c.gridx = 1;
    add(labelLastParsedLevelValue, c);
    
    // Metrics: nodes in progress
    c.gridx = 0;
    c.gridy = 11;
    c.insets = new Insets(5, 15, 5, 5);
    add(labelNodesInProgress, c);
    c.gridx = 1;
    add(labelNodesInProgressValue, c);
    
    // Export results
    c.gridx = 0;
    c.gridy = 12;
    c.weightx = 0;
    c.insets = new Insets(5, 15, 15, 5);
    add(labelParsedExport, c);
    c.gridx = 1;
    c.gridwidth = 2;
    c.weightx = 1;
    c.insets = new Insets(5, 5, 15, 5);
    add(textFieldExportFile, c);
    c.weightx = 0;
    c.gridwidth = 1;
    c.gridx = 3;
    c.insets = new Insets(5, 5, 15, 15);
    add(buttonSave, c);
    
  }
  
  private void addListeners() {
    
    // Property-change listeners
    PropertyChangeListener boundPropertyChangeListener = event -> {
      switch (event.getPropertyName()) {
        case "state":
          SwingWorker.StateValue state = (SwingWorker.StateValue) event.getNewValue();
          if (state == SwingWorker.StateValue.DONE) {
            setCursor(null);
            textFieldUrl.setEnabled(true);
            toggleButtonRun.setSelected(false);
            toggleButtonRun.setText("Run");
            JOptionPane.showMessageDialog(null, "Done!");
          }
          break;
//                case "progress":
//                    break;
        case "metrics":
          Metrics metrics = (Metrics) event.getNewValue();
          labelParsedValue.setText(String.valueOf(metrics.getPagesParsed()));
          int elapsed = metrics.getElapsedTime();
          int minutes = elapsed / 60;
          int seconds = elapsed % 60;
          labelElapsedValue.setText(String.format("%02d:%02d", minutes, seconds));
          labelActiveWorkersValue.setText(String.valueOf(metrics.getActiveWorkers()));
          labelFoundNodesValue.setText(String.valueOf(metrics.getFoundNodes()));
          labelNodesToProceedValue.setText(String.valueOf(metrics.getNodesToProceed()));
          labelReachedLevelValue.setText(String.valueOf(metrics.getReachedLevel()));
          labelLastParsedLevelValue.setText(String.valueOf(metrics.getLastParsedLevel()));
          labelNodesInProgressValue.setText(String.valueOf(metrics.getNodesInProgress()));
          break;
        default:
          break;
      }
    };
    
    // Action listeners
    ActionListener onToggleCrawlerWorking = event -> {
      
      boolean autoWorkersNumber = checkBoxEnableAutoWorkersNumber.isSelected();
      int workersNumber = autoWorkersNumber ? defaultThreads : Integer.parseInt(textFieldWorkersNumber.getText());
      boolean isDepthLimited = checkBoxEnableMaxDepth.isSelected();
      int depth = isDepthLimited ? Integer.parseInt(textFieldDepth.getText()) : 0;
      boolean isTimeLimited = checkBoxEnableTimeLimit.isSelected();
      int timeLimitInSeconds = isTimeLimited ? Integer.parseInt(textFieldTimeLimit.getText()) : 0;
      
      if (toggleButtonRun.isSelected()) {
        
        toggleButtonRun.setText("Stop");
        textFieldUrl.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        foundNodes.clear(); // New search starts from scratch, discarding all previous results
        crawlerWorker = new SearchTask(textFieldUrl.getText(), foundNodes,
          workersNumber, depth, timeLimitInSeconds);
        
        crawlerWorker.addPropertyChangeListener(boundPropertyChangeListener);
        crawlerWorker.execute();
        
      } else {
        toggleButtonRun.setText("Run");
        setCursor(null);
        textFieldUrl.setEnabled(true);
        crawlerWorker.cancel(false);
        crawlerWorker.removePropertyChangeListener(boundPropertyChangeListener);
        loudNotification("CANCELED IN ACTION LISTENER.", 2);
      }
      
    };
    
    ActionListener onAutoWorkersSwitched = event -> {
      if (checkBoxEnableAutoWorkersNumber.isSelected()) {
        textFieldWorkersNumber.setText(String.valueOf(defaultThreads));
        textFieldWorkersNumber.setEnabled(false);
      } else {
        textFieldWorkersNumber.setEnabled(true);
      }
    };
    
    ActionListener onMaximumDepthSwitched = event -> {
      if (checkBoxEnableMaxDepth.isSelected()) {
        textFieldDepth.setText("2");
        textFieldDepth.setEnabled(true);
      } else {
        textFieldDepth.setText("0");
        textFieldDepth.setEnabled(false);
      }
    };
    
    ActionListener onTimeLimitSwitched = event -> {
      if (checkBoxEnableTimeLimit.isSelected()) {
        textFieldTimeLimit.setText("60");
        textFieldTimeLimit.setEnabled(true);
      } else {
        textFieldTimeLimit.setText("0");
        textFieldTimeLimit.setEnabled(false);
      }
    };
    
    ActionListener onSaveAction = event -> {//On "Save file" command
/*
      int returnVal = fileChooser.showSaveDialog(null);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        try {
          fileName = fileChooser.getSelectedFile().getCanonicalPath();
        } catch (IOException ignored) {
        }
        try (FileWriter writer = new FileWriter(fileName, false)) {
          for (Node node : foundNodes) {
            writer.write(node.url + "\n");
            writer.write(node.title + "\n");
          }
        } catch (IOException ignored) {
        }
      }
*/
      String fileName = textFieldExportFile.getText();
      try (FileWriter writer = new FileWriter(fileName, false)) {
        for (Node node : foundNodes) {
          writer.write(node.toString());
//          writer.write(node.url + "\n");
//          writer.write(node.title + "\n");
        }
      } catch (IOException ignored) {
      }
      
    };
    
    toggleButtonRun.addActionListener(onToggleCrawlerWorking);
    checkBoxEnableAutoWorkersNumber.addActionListener(onAutoWorkersSwitched);
    checkBoxEnableMaxDepth.addActionListener(onMaximumDepthSwitched);
    checkBoxEnableTimeLimit.addActionListener(onTimeLimitSwitched);
    buttonSave.addActionListener(onSaveAction);
    
  }
  
  public static void loudNotification(String message, int repeats) {
    
    final String msg = " " + message + " ";
    final String line = "*******" + "*".repeat(msg.length()).repeat(repeats) + "*******";
    
    System.out.println();
    System.out.println(line);
    System.out.println(line);
    System.out.println(line);
    System.out.println("****** " + msg.repeat(repeats) + " ******");
    System.out.println(line);
    System.out.println(line);
    System.out.println(line);
    System.out.println();
    
  }
  
}

@SuppressWarnings({"unused", "RedundantExplicitVariableType"})
class SearchTask extends SwingWorker<Void, Node> {
  
  private final String LINE_SEPARATOR = System.getProperty("line.separator");
  
  private final Queue<Node> nodesToProceed = new ConcurrentLinkedQueue<>();
  private ExecutorService executor;
  private final Set<Node> foundNodes;
  private final int depthLimit;
  private final int timeLimitInSeconds;
  
  // SwingWorker already has a property change support instance.
  // Public method getPropertyChangeSupport returns a reference on it.
  // We will use this.
  // If we want make bound property for class that doesn't extend the SwingWorker<> class we could use
  // new PropertyChangeSupport(), like this:
  //private final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
  
  volatile Metrics metrics = new Metrics(); // custom bound property
  
  public SearchTask(String urlString, Set<Node> foundNodes,
                    int poolSize, int depth, int timeLimitInSeconds) {
    super();
    this.foundNodes = foundNodes;
    this.depthLimit = depth;
    this.timeLimitInSeconds = timeLimitInSeconds;
    
    if (executor != null) {
      System.out.println("Warning!!! The thread pool executor wasn't deleted after SearchTask was canceled");
      System.out.println("trying to stop execution... please wait for 10 sec");
      executor.shutdownNow();
      try {
        executor.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    executor = Executors.newFixedThreadPool(poolSize);
    
    nodesToProceed.add(new Node(urlString, 0));
    metrics.addNodesToProceed(1);
    
    System.out.println("SwingWorker created in thread name: " + Thread.currentThread().getName());
    
  }
  
  private String getContent(InputStream inputStream) throws IOException {
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      StringBuilder contentBuilder = new StringBuilder();
      metrics.incrementPagesParsed();
/*
      String nextLine;
      while ((nextLine = reader.readLine()) != null) {
        contentBuilder.append(nextLine);
        contentBuilder.append(LINE_SEPARATOR);
      }
      return contentBuilder.toString();
*/ //todo?? which is better/faster ??
      return reader.lines()
        .reduce((a, l) -> contentBuilder.append(l).append(LINE_SEPARATOR).toString())
        .orElse("");
    }
  }
  
  private String getPageTitle(String content) {
    //todo>> use Jsoup instead of regex
    Matcher titleMatcher = Pattern.compile("<title>([\\w\\W]*)</title>", Pattern.CASE_INSENSITIVE).matcher(content);
    if (titleMatcher.find()) {//get a title from the content
      return titleMatcher.group(1);
    }
    return "no title - text/html content type";
  }
  
  @SuppressWarnings("DuplicatedCode")
  private List<String> getHttpLinks(String content, String protocol, URL url)
    throws URISyntaxException, MalformedURLException {
    List<String> result = new ArrayList<>();
    Matcher linkMatcher = Pattern.compile("(href=\"([^\"]*)\")|(href='([^\"]*)')").matcher(content);
    
    while (linkMatcher.find()) {
      String matcherResult = linkMatcher.group(2) != null ?
        linkMatcher.group(2)
        :
        linkMatcher.group(4) != null ?
          linkMatcher.group(4)
          :
          "";
      //if matcherResult is //en.wikipedia.org/ or en.wikipedia.org/ add current protocol
      if (matcherResult.matches("(\\w*\\.)+(\\w)+(/.*)*")) {
        matcherResult = protocol + "://" + matcherResult;
      } else if (matcherResult.matches("(//)?(\\w*\\.)+(\\w)+(/.*)*")) {
        matcherResult = protocol + ":" + matcherResult;
      }
      URI nextUri = new URI(matcherResult);
      URL nextUrl = null;
      if (nextUri.isAbsolute()) {
        String proto = nextUri.getScheme();
        if (proto.equals("http") || proto.equals("https")) {
          nextUrl = new URL(matcherResult);
        }
      } else {
        nextUrl = new URL(url, matcherResult);
      }
      if (nextUrl != null) {
        String nextLink = nextUrl.toString();
        result.add(nextLink);
      }
    }
    
    return result;
  }
  
  private Node getNode(Node node) {
    
    try {
      
      URL url = new URL(node.url);
      URLConnection connection = url.openConnection();
      String protocol = url.getProtocol();
      String contentType = connection.getContentType();
      
      if (protocol != null && (protocol.equals("http") || protocol.equals("https"))
        && contentType != null && contentType.contains("text/html")) {

        if (!foundNodes.contains(node)) { // get the content if haven't been found yet
          String content = getContent(connection.getInputStream());//use https://bravia.ru/try.html for testing
          node.title = getPageTitle(content);
          node.links = getHttpLinks(content, protocol, url);
        }

        metrics.setLastParsedLevel(node.level);
        
        return node;
        
      }
    } catch (IOException | URISyntaxException ignored) {
    }
    
    return null;
  }
  
  private void fireMetricsBoundPropertyUpdateEvent() {
    Metrics previousMetrics = metrics.copy();
    metrics.setNodesToProceed(nodesToProceed.size());
    metrics.setFoundNodes(foundNodes.size());
    metrics.setActiveWorkers(((ThreadPoolExecutor) executor).getActiveCount());
    
    // SwingWorker already has a property change support instance and
    // firePropertyChangeMethod overridden.
    // Public method getPropertyChangeSupport returns a reference on PropertyChangeSupport instance.
    // Outside SwingWorker class descendants use new PropertyChangeSupport() and its methods instead.
    firePropertyChange("metrics", previousMetrics, metrics.copy());
  }
  
  @Override
  protected Void doInBackground() {
    
    System.out.println("SwingWorker background work started in the thread: " + Thread.currentThread().getName());
    Thread currentThread = Thread.currentThread();
    
    //time limit check predicate
    Predicate<Integer> timeLimitExceeded = t -> t != 0 && metrics.getElapsedTime() > t;
    
    //level depth check predicate
    Predicate<Integer> levelDepthReachedAndFinished = level -> depthLimit != 0
      && level >= depthLimit && metrics.getNodesInProgress() == 0;
    
    //level depth check exceeded
    Predicate<Integer> levelDepthExceeded = level -> depthLimit != 0
      && level > depthLimit;
    
    while (!Thread.interrupted() && !isCancelled()
      && !timeLimitExceeded.test(timeLimitInSeconds)
      && !levelDepthReachedAndFinished.test(metrics.getReachedLevel())) {
      
      try {
        TimeUnit.MILLISECONDS.sleep(10); //todo>> instead timeouts it's better to use a scheduled executor
        fireMetricsBoundPropertyUpdateEvent();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
      
      final Node nextNode = nodesToProceed.poll();
      if (nextNode != null) {
        metrics.removeNodesToProceed(1);
        if (nextNode.level > metrics.getReachedLevel()) {
          metrics.setReachedLevel(nextNode.level);
        }
      }
      
      if (nextNode != null) {
        metrics.increaseNodesInProgress();
        executor.submit(() -> {
          try {
            Node node = getNode(nextNode);
            if (node != null) {
              publish(node);
              if (!timeLimitExceeded.test(timeLimitInSeconds) && !currentThread.isInterrupted()) {
/*
                // Collect nodes to proceed. Option #1
                node.links.forEach(url -> {
                  if (node.level < depthLimit) {
                    final Node n = new Node(url, node.level + 1);
                    if (!nodesToProceed.contains(n)) {
                      nodesToProceed.add(n);
                    }
                  }
                });//todo?? which is faster/better #1 or #2
*/
                // Collect nodes to proceed. Option #2
                Set<Node> nextNodes = node.links.stream()
                  .map(l -> new Node(l, node.level + 1))
                  .filter(n -> !levelDepthExceeded.test(n.level)) // max level condition check (if present)
                  .filter(n -> !foundNodes.contains(n)) // only those that haven't found yet
  //                .sorted(Comparator.comparingInt(n -> n.level))
                  .collect(Collectors.toCollection(LinkedHashSet::new));//todo?? ordered ??
                nodesToProceed.addAll(nextNodes);
                metrics.addNodesToProceed(nextNodes.size());
                String currentThreadName = currentThread.getName();
                nextNodes.forEach(n -> System.out.println("Executor's pool background work for " +
                  n.url + " started in the thread: " + currentThreadName));
              }
            }
          } catch (Exception e) {
            WebCrawler.loudNotification("Exception inside worker \"" + Thread.currentThread().getName() + "\" occurred", 1);
            e.printStackTrace();
          } finally {
            metrics.decreaseNodesInProgress();
            fireMetricsBoundPropertyUpdateEvent();
          }
        });
      }
      if (metrics.nodesInProgress == 0) {
        break;
      }
    }
    
    try {
      if (isCancelled()) {
        WebCrawler.loudNotification("CANCELED IN DoInBackground method.", 1);
        executor.shutdownNow();
      }
      if (currentThread.isInterrupted()) {
        WebCrawler.loudNotification("INTERRUPTED IN DoInBackground method.", 1);
        executor.shutdownNow();
      }
      if (timeLimitExceeded.test(timeLimitInSeconds)) {
        WebCrawler.loudNotification("TIME LIMIT EXCEEDED IN DoInBackground method.", 1);
        executor.shutdownNow();
      }
      if (levelDepthReachedAndFinished.test(metrics.getReachedLevel())) {
        WebCrawler.loudNotification("LEVEL DEPTH REACHED IN DoInBackground method.", 1);
        System.out.println("Wait for finishing tasks in queue ... ");
        executor.shutdown();
        executor.awaitTermination(/*Long.MAX_VALUE, TimeUnit.MILLISECONDS*/5, TimeUnit.MINUTES);
        System.out.println("Done");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    
    return null;
  }
  
  @Override
  protected void process(List<Node> nodes) {
    foundNodes.addAll(nodes);
    metrics.setFoundNodes(nodes.size());
    System.out.println("SwingWorker.process() has invoked:");
    nodes.forEach(n -> System.out.println(n.title + ": " + n.url));
  }
  
  @Override
  protected void done() {
  }
  
}

class Node {

  private final String LINE_SEPARATOR = System.getProperty("line.separator");

  String url;
  String title = "";
  List<String> links = new ArrayList<>();
  int level;
  
  Node(String url, int level) {
    this.url = url;
    this.level = level;
  }
  
  @Override
  public boolean equals(Object obj) {
    
    if (!(obj instanceof Node)) {
      return false;
    }
    return ((Node) obj).url.equals(url);
    
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(url);
  }
  
  @Override
  public String toString() {
    return "L" + level + ": " + title + " url: " + url + LINE_SEPARATOR;
  }
  
}

class Metrics { // Metrics to display
  
  private long startTimeInSeconds;
  int nodesToProceed = 0;
  int activeWorkers = 0;
  int elapsedTime = 0;
  int pagesParsed = 0;
  int foundNodes = 0;
  int reachedLevel = 0;
  int lastParsedLevel = 0;
  int nodesInProgress = 0;
  
  public Metrics() {
    startTimeInSeconds = System.currentTimeMillis() / 1000;
  }
  
  public synchronized Metrics copy() {
    Metrics m = new Metrics();
    m.startTimeInSeconds = this.startTimeInSeconds;
    m.nodesToProceed = this.nodesToProceed;
    m.activeWorkers = this.activeWorkers;
    m.elapsedTime = this.elapsedTime;
    m.pagesParsed = this.pagesParsed;
    m.foundNodes = this.foundNodes;
    m.reachedLevel = this.reachedLevel;
    m.lastParsedLevel = this.lastParsedLevel;
    m.nodesInProgress = this.nodesInProgress;
    return m;
  }
  
  public synchronized void setNodesToProceed(int nodesToProceed) {
    this.nodesToProceed = nodesToProceed;
  }
  
  public synchronized void addNodesToProceed(int number) {
    this.nodesToProceed += number;
  }
  
  public synchronized void removeNodesToProceed(int number) {
    this.nodesToProceed -= number;
  }
  
  public synchronized void setActiveWorkers(int activeWorkers) {
    this.activeWorkers = activeWorkers;
  }
  
  public synchronized void incrementPagesParsed() {
    this.pagesParsed++;
  }
  
  public synchronized void setFoundNodes(int foundNodes) {
    this.foundNodes = foundNodes;
  }
  
  public synchronized void setReachedLevel(int reachedLevel) {
    this.reachedLevel = reachedLevel;
  }
  
  public synchronized void setLastParsedLevel(int lastParsedLevel) {
    this.lastParsedLevel = lastParsedLevel;
  }
  
  public synchronized void increaseNodesInProgress() {
    nodesInProgress++;
  }
  
  public synchronized void decreaseNodesInProgress() {
    nodesInProgress--;
  }
  
  public int getNodesToProceed() {
    return nodesToProceed;
  }
  
  public int getActiveWorkers() {
    return activeWorkers;
  }
  
  public int getElapsedTime() {
    return (int) (System.currentTimeMillis() / 1000 - startTimeInSeconds);
  }
  
  public int getPagesParsed() {
    return pagesParsed;
  }
  
  public int getFoundNodes() {
    return foundNodes;
  }
  
  public int getReachedLevel() {
    return reachedLevel;
  }
  
  public int getLastParsedLevel() {
    return lastParsedLevel;
  }
  
  public int getNodesInProgress() {
    return nodesInProgress;
  }
  
}
