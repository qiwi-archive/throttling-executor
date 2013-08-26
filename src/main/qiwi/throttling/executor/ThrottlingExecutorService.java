package main.qiwi.throttling.executor;

import java.util.concurrent.*;

public class ThrottlingExecutorService<T> implements TypedExecutorService<T> {
	private final ConcurrentMap<T, TypedRunnable> tasks;
	private final ExecutorService executorService;

	public ThrottlingExecutorService(int poolSize, ThreadFactory threadFactory) {
		tasks = new ConcurrentHashMap<T, TypedRunnable>();
		this.executorService = Executors.newFixedThreadPool(poolSize, threadFactory);
	}

	@Override
	public void execute(TypedRunnable<T> command) {
		final T type = command.getType();
		Runnable oldVal = tasks.put(type, command);
		if (oldVal == null) {
			executorService.execute(
				new Runnable() {
					public void run() {
						try {
							final TypedRunnable task = tasks.remove(type);
							if (task != null) {
								task.run();
							}
						} catch (Throwable e) {
						}
					}
				}
			);
		}
	}

	@Override
	public void shutdown() {
		executorService.shutdown();
		tasks.clear();
	}
}
