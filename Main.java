import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.plaf.synth.SynthOptionPaneUI;

 interface TaskExecutor<T> {
	 Future<T> submitTask(Task<T> task);
}

 class MyTaskExecutor<T> implements TaskExecutor{

	 int capacity;
	 WorkerThread thread[];
	 public MyTaskExecutor(int capacity) {
		// TODO Auto-generated constructor stub
		 this.capacity = capacity;
		 thread = new WorkerThread[this.capacity];
		 for(int i=0;i<this.capacity;i++) {
			 thread[i] = new WorkerThread();
			 Thread t = new Thread(thread[i]);
			 t.start();
		 }
	}
	@Override
	public Future<String> submitTask(Task task) {
		FutureTask future = new FutureTask(task);
		Main.queue.add(task);
		return future;
	}
	 
 }
 
 class WorkerThread implements Runnable{

	@Override
	public void run()  {
		while(true) {
			if(Main.queue.size() !=0) {
				Callable task;
				try {
					task =  (Callable) Main.queue.take();
				
						task.call();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
	 
 }
class Task<T> implements Callable {

	String name;
	ReentrantLock lock;
	Semaphore semaphore;

	Task(String name, Semaphore semaphore) {
		this.name = name;
		this.semaphore = semaphore;
	}

	@Override
	public String call(){
		try {
			this.semaphore.acquire();
			System.out.println(Thread.currentThread().getName() + " waiting for lock");
			Thread.sleep(3000);
			System.out.println(Thread.currentThread().getName() + " holds lock and executing task " + this.name);
			this.semaphore.release();
			System.out.println(Thread.currentThread().getName() + " released for lock");
		} catch (Exception e) {
			e.printStackTrace();
			//return null;
		}
		return "Completed";
	}

}

public class Main {
	static int max_thread = 3;
	static int task = 10;
	static List<Future<String>> response;
	static BlockingQueue queue = new LinkedBlockingQueue();
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ReentrantLock lock = new ReentrantLock();
		Semaphore semaphore = new Semaphore(2, true);
//ExecutorService executor = Executors.newFixedThreadPool(max_thread);
//		for (int i = 0; i < task; i++) {
//			Task task = new Task("name " + i, semaphore);
//			Future lastReturnTask = executor.submit(task);
//			response.add(lastReturnTask);
//		}
		
		MyTaskExecutor pool = new MyTaskExecutor(max_thread);
		List<Future> list = new ArrayList<Future>();
		for(int i=0;i<20;i++) {
			Task task = new Task("name "+i,semaphore);
			Future f = pool.submitTask(task);
			list.add(f);
		}
		
		for(Future f : list) {
			System.out.println(f.isDone());
		}
	}

}
