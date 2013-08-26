package main.qiwi.throttling.executor;

public interface TypedRunnable<T> extends Runnable {
	public T getType();
}
