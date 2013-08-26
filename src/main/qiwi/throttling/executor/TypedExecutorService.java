package main.qiwi.throttling.executor;

public interface TypedExecutorService<T> {
	public void execute(TypedRunnable<T> typedRunnable);
	public void shutdown();
}
