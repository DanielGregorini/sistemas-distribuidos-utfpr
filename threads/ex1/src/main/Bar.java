/**
 * Classe principal que simula um bar, onde clientes e garçons interagem através de
 * semáforos e filas para controlar pedidos e preparação de bebidas.
 */
public class Bar {

    // Parâmetros
    public static final int NUM_CLIENTS = 3;    // Número de clientes presentes no estabelecimento
    public static final int NUM_WAITER = 1;     // Quantidade de garçons trabalhando
    public static final int WAITER_CAPACITY = 2;// Capacidade de atendimento dos garçons
    public static final int NUM_ROUNDS = 1;     // Número de rodadas que serão liberadas no bar

    // Variáveis de semáforos e controle
    public static final java.util.concurrent.Semaphore waitersAvailable = new java.util.concurrent.Semaphore(NUM_WAITER);
    public static final java.util.concurrent.Semaphore preparationBartender = new java.util.concurrent.Semaphore(1);

    // Filas para gerenciar pedidos
    public static final java.util.Queue<Integer> queueOrders = new java.util.LinkedList<>();
    public static final java.util.Queue<Integer> queueOrdersToPrepare = new java.util.LinkedList<>();

    // Objeto de sincronização
    public static final Object Lock = new Object();

    // Estado do bar e contadores
    public static boolean barOpen = true;  // Indica se o bar está aberto
    public static int customersServed = 0; // Quantidade de clientes atendidos

    public static int round = 1; // Controla qual rodada está em andamento

    public static void main(final String[] args) {

        // Criação dos garçons
        for (int i = 0; i < NUM_WAITER; i++) {
            System.err.println("\n  Garçom " + (i + 1) + " está aguardando no salão.");
            new Thread(new WaiterThread(i, WAITER_CAPACITY)).start();
        }

        // Thread que controla as rodadas no bar
        new Thread(new roundController()).start();

        // Criação dos clientes
        for (int i = 0; i < NUM_CLIENTS; i++) {
            new Thread(new ClientThread(i)).start();
        }
    }
}
