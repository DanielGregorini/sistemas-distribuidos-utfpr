import java.util.Random;
import java.util.concurrent.locks.*;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Classe que implementa o problema do Barbeiro Dorminhoco usando monitores (locks e conditions).
 * Simula um barbeiro que atende clientes conforme chegam na barbearia.
 */
public class BarbeiroMonitor {
    private int cadeirasLivres; // Número de cadeiras disponíveis na sala de espera
    private ReentrantLock lock = new ReentrantLock(); // Lock para garantir exclusão mútua
    private Condition clientesEsperando = lock.newCondition(); // Condição para esperar clientes
    private final int cadeiras; // Número total de cadeiras na barbearia
    private Queue<Integer> filaClientes = new LinkedList<>(); // Fila de clientes esperando atendimento

    /**
     * Construtor da classe BarbeiroMonitor.
     * 
     * @param cadeiras Número total de cadeiras disponíveis para clientes na barbearia.
     */
    public BarbeiroMonitor(int cadeiras) {
        cadeirasLivres = cadeiras;
        this.cadeiras = cadeiras;
    }

    /**
     * Método chamado pelos clientes ao chegarem na barbearia.
     * Caso não haja cadeiras livres, o cliente sai da barbearia.
     * Caso contrário, ele aguarda atendimento na fila.
     * 
     * @param clienteId ID do cliente que deseja cortar o cabelo.
     * @return true se o cliente conseguiu ser atendido, false caso contrário.
     * @throws InterruptedException Se a thread for interrompida durante a espera.
     */
    public boolean cortarCabelo(int clienteId) throws InterruptedException {
        lock.lock(); // Obtém o lock para garantir exclusão mútua

        try {
            // Se não há cadeiras livres, o cliente sai da barbearia
            if (cadeirasLivres == 0) {
                System.out.println("Cliente " + clienteId + " não encontrou cadeira livre. Saindo da barbearia...");
                return false;
            }

            // Cliente ocupa uma cadeira
            cadeirasLivres--;
            filaClientes.add(clienteId);

            // Acorda o barbeiro caso ele esteja dormindo
            clientesEsperando.signal();

            System.out.println("Cliente " + clienteId + " sentou na cadeira. Existem " + cadeirasLivres + " cadeiras livres.");

            // Cliente aguarda seu corte de cabelo ser iniciado
            clientesEsperando.await();

            // Após o corte, o cliente libera a cadeira
            cadeirasLivres++;

            System.out.println("Cliente " + clienteId + " saiu da barbearia.");
            return true;
        } finally {
            lock.unlock(); // Libera o lock
        }
    }

    /**
     * Método chamado pelo barbeiro para atender clientes.
     * O barbeiro fica dormindo enquanto não houver clientes na fila.
     * Quando há um cliente, ele corta o cabelo e o libera após o serviço.
     * 
     * @throws InterruptedException Se a thread for interrompida enquanto espera por clientes.
     */
    public void atenderCliente() throws InterruptedException {
        lock.lock(); // Obtém o lock para garantir exclusão mútua

        try {
            // Se não há clientes esperando, o barbeiro dorme até ser acordado
            while (cadeirasLivres == cadeiras) {
                System.out.println("Barbeiro dormindo...");
                clientesEsperando.await();
            }

            // Remove um cliente da fila para atendimento
            Integer clienteId = filaClientes.poll();

            if (clienteId != null) {
                System.out.println("Barbeiro acordou e está cortando cabelo do cliente " + clienteId + "...");

                // Simula o tempo necessário para cortar o cabelo
                Thread.sleep(1000);

                // Sinaliza para o cliente que o corte foi finalizado
                clientesEsperando.signal();

                System.out.println("Barbeiro terminou de cortar cabelo do cliente " + clienteId + ".");
            }
        } finally {
            lock.unlock(); // Libera o lock
        }
    }

    /**
     * Método principal para simular a barbearia.
     * Cria múltiplos clientes e um barbeiro que atende a todos conforme chegam.
     * 
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        int cadeiras = 3; // Número de cadeiras disponíveis na sala de espera
        int clientes = 10; // Número total de clientes que chegarão na barbearia
        Random random = new Random();

        BarbeiroMonitor barbearia = new BarbeiroMonitor(cadeiras);

        // Criando threads para os clientes
        for (int i = 0; i < clientes; i++) {
            int finalI = i;
            Thread cliente = new Thread(() -> {
                try {
                    // Simula o tempo aleatório de chegada do cliente
                    Thread.sleep(random.nextInt(3000));
                    System.out.println("Cliente " + finalI + " chegou na barbearia.");
                    barbearia.cortarCabelo(finalI);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            cliente.start();
        }

        // Criando a thread do barbeiro
        Thread barbeiro = new Thread(() -> {
            while (true) { // O barbeiro trabalha indefinidamente
                try {
                    barbearia.atenderCliente();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        barbeiro.start();
    }
}
