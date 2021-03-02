package web_crawler;

public class Helpers {
  
  public static int getOptimalPoolSizeForCPUBoundTasks() {
    return Runtime.getRuntime().availableProcessors();
  }
  
  public static int getOptimalPoolSizeForIOBoundTasks(double blockingTimeRatio) {
    return (int) (Runtime.getRuntime().availableProcessors() / (1 - blockingTimeRatio)) + 1;
  }
  
}
