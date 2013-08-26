package test.qiwi.throttling.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.testng.annotations.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Semaphore;

import static org.testng.Assert.assertFalse;

@Guice(modules = {ThrottlingExecutorServiceTest.ThrottlingExecutionThreadServiceModule.class})
public class ThrottlingExecutorServiceTest {
	@Inject
	private ThrottlingExecutorService<Ping> executorService;

	@Test
	public void executeNormal() throws InterruptedException {
		final Semaphore semaphore = new Semaphore(0, true);

		TypedRunnable<Ping> ping = new TypedRunnable<Ping>() {
			@Override
			public Ping getType() {
				return defaultPing;
			}

			@Override
			public void run() {
				try {

					Thread.sleep(1000l);
				} catch (InterruptedException e) {
				}
				semaphore.release();
			}
		};

		executorService.execute(ping);

		semaphore.acquire();
	}

	@Test
	public void executeLast() throws InterruptedException {
		final Semaphore semaphore = new Semaphore(0, true);
		final ValueHolder<Boolean> unexpectedExecution = new ValueHolder<Boolean>();
		unexpectedExecution.value = false;

		TypedRunnable<Ping> ping1 = new TypedRunnable<Ping>() {
			@Override
			public Ping getType() {
				return defaultPing;
			}

			@Override
			public void run() {
				unexpectedExecution.value = true;
				try {
					Thread.sleep(1000l);
				} catch (InterruptedException e) {
				}
			}
		};

		TypedRunnable<Ping> ping2 = new TypedRunnable<Ping>() {
			@Override
			public Ping getType() {
				return defaultPing;
			}

			@Override
			public void run() {
				try {
					Thread.sleep(1000l);
				} catch (InterruptedException e) {
				}
				semaphore.release();
			}
		};

		executorService.execute(ping1);
		executorService.execute(ping1);
		executorService.execute(ping1);
		executorService.execute(ping2);

		semaphore.acquire();
		assertFalse(unexpectedExecution.value);
	}

	@AfterClass
	private void stop() {
		executorService.shutdown();
	}

	public static class ThrottlingExecutionThreadServiceModule extends AbstractModule {
		@Override
		protected void configure() {
		}

		@Singleton
		@Provides
		ThrottlingExecutorService<Ping> createThrottlingExecutionThreadService() {
			return new ThrottlingExecutorService<Ping>(
				2,
				new ThreadFactoryBuilder()
					.setNameFormat("throttling-worker-%d")
					.build()
			);
		}
	}

	private static class Ping {
	}

	private final Ping defaultPing = new Ping();

	private class ValueHolder<T> {
		T value;
	}
}
