import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

class Buffer {

    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity;
    private final Semaphore empty;
    private final Semaphore full;
    private final Semaphore mutex;

    public Buffer(int capacity) {
        this.capacity = capacity;
        this.empty = new Semaphore(capacity);
        this.full = new Semaphore(0);
        this.mutex = new Semaphore(1);
    }

    public void produzir(int item) throws InterruptedException {
        empty.acquire();
        mutex.acquire();
        queue.add(item);
        System.out.println("Produziu: " + item);
        mutex.release();
        full.release();
    }

    public int consumir() throws InterruptedException {
        full.acquire();
        mutex.acquire();
        int item = queue.poll();
        System.out.println("Consumiu: " + item);
        mutex.release();
        empty.release();
        return item;
    }
}

public class Semaforo {

    public static void main(String[] args) {
        Buffer buffer = new Buffer(5);

        Runnable produtor = () -> {
            for (int i = 0; i < 10; i++) {
                try {
                    buffer.produzir(i);
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Runnable consumidor = () -> {
            for (int i = 0; i < 10; i++) {
                try {
                    buffer.consumir();
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Thread produtorThread = new Thread(produtor);
        Thread consumidorThread = new Thread(consumidor);

        produtorThread.start();
        consumidorThread.start();
    }
}
