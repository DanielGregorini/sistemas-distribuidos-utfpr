import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe que representa uma roleta que pode girar em um ambiente multithread.
 * Cada roleta é executada em uma thread separada dentro de um pool de threads.
 */
public class Roleta implements Runnable {

    private final int id; // Identificação única da roleta

    /**
     * Construtor da classe Roleta.
     *
     * @param id Identificação da roleta.
     */
    public Roleta(int id) {
        this.id = id;
    }

    /**
     * Método que executa a roleta, simulando seu giro por um tempo aleatório.
     */
    @Override
    public void run() {
        System.out.println("Roleta " + id + " está girando.");
        try {
            // Simula o tempo de giro da roleta com um tempo aleatório
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Garante que a interrupção seja tratada corretamente
        }
        System.out.println("Roleta " + id + " parou.");
    }

    /**
     * Método principal que cria e executa múltiplas roletas em um pool de threads.
     *
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        int numRoletas = 5; // Define o número de roletas a serem executadas
        ExecutorService executorService = Executors.newFixedThreadPool(numRoletas); // Cria um pool de threads com capacidade fixa

        // Inicia múltiplas roletas em threads separadas
        for (int i = 1; i <= numRoletas; i++) {
            executorService.execute(new Roleta(i));
        }

        executorService.shutdown(); // Finaliza o executor após a conclusão das tarefas
    }
}
