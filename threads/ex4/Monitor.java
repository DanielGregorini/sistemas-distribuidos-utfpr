import java.util.LinkedList;
import java.util.Queue;

/**
 * Classe que implementa um monitor para controle de um buffer compartilhado.
 * Utiliza sincronização para garantir o acesso seguro entre múltiplas threads
 * (produtores e consumidores).
 */
class BufferMonitor {

    private final Queue<Integer> queue = new LinkedList<>(); // Fila que representa o buffer
    private final int capacity; // Capacidade máxima do buffer

    /**
     * Construtor do BufferMonitor.
     *
     * @param capacity Capacidade máxima do buffer.
     */
    public BufferMonitor(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Método sincronizado para produção de itens.
     * O produtor deve aguardar se o buffer estiver cheio.
     *
     * @param item Item a ser produzido.
     * @throws InterruptedException Se a thread for interrompida enquanto aguarda.
     */
    public synchronized void produzir(int item) throws InterruptedException {
        while (queue.size() == capacity) { // Aguarda se o buffer estiver cheio
            wait();
        }
        queue.add(item); // Adiciona o item ao buffer
        System.out.println("Produziu: " + item);
        notifyAll(); // Notifica as threads consumidoras de que há um novo item disponível
    }

    /**
     * Método sincronizado para consumo de itens.
     * O consumidor deve aguardar se o buffer estiver vazio.
     *
     * @return Item consumido.
     * @throws InterruptedException Se a thread for interrompida enquanto aguarda.
     */
    public synchronized int consumir() throws InterruptedException {
        while (queue.isEmpty()) { // Aguarda se o buffer estiver vazio
            wait();
        }
        int item = queue.poll(); // Remove um item do buffer
        System.out.println("Consumiu: " + item);
        notifyAll(); // Notifica as threads produtoras de que há espaço disponível no buffer
        return item;
    }
}

/**
 * Classe principal que simula a execução de produtores e consumidores.
 */
public class Monitor {

    public static void main(String[] args) {
        BufferMonitor buffer = new BufferMonitor(5); // Criação do buffer com capacidade 5

        // Definição da lógica do produtor
        Runnable produtor = () -> {
            for (int i = 0; i < 10; i++) {
                try {
                    buffer.produzir(i); // Produzindo itens
                    Thread.sleep((long) (Math.random() * 1000)); // Simula um tempo variável de produção
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Definição da lógica do consumidor
        Runnable consumidor = () -> {
            for (int i = 0; i < 10; i++) {
                try {
                    buffer.consumir(); // Consumindo itens
                    Thread.sleep((long) (Math.random() * 1000)); // Simula um tempo variável de consumo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Criação das threads produtor e consumidor
        Thread produtorThread = new Thread(produtor);
        Thread consumidorThread = new Thread(consumidor);

        // Início da execução das threads
        produtorThread.start();
        consumidorThread.start();
    }
}
