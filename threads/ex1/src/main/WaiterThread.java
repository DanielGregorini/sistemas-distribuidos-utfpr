import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um garçom no bar.
 * O garçom é responsável por pegar pedidos dos clientes, levar até o bartender e entregar as bebidas.
 */
class WaiterThread extends Thread {
    private int idGarcom; // Identificação do garçom
    private int capacity; // Capacidade máxima de pedidos que o garçom pode atender de uma vez
    private int currentCapacity = 0; // Contador de pedidos em andamento pelo garçom

    /**
     * Construtor da classe WaiterThread.
     * 
     * @param id              Identificação do garçom
     * @param WAITER_CAPACITY Capacidade máxima de atendimento do garçom
     */
    public WaiterThread(int id, int WAITER_CAPACITY) {
        this.idGarcom = id;
        this.capacity = WAITER_CAPACITY;
    }

    @Override
    public void run() {

        List<Integer> idCliente = new ArrayList<>(); // Lista de clientes atendidos pelo garçom nesta rodada

        while (Bar.barOpen) { // O garçom trabalha enquanto o bar estiver aberto
            try {
                Random random = new Random();

                // Pegar pedidos dos clientes
                synchronized (Bar.Lock) {
                    if (!Bar.queueOrders.isEmpty()) { // Verifica se há pedidos na fila
                        int id = Bar.queueOrders.poll(); // Retira o pedido da fila
                        idCliente.add(id); // Adiciona o cliente à lista de pedidos em andamento
                        Bar.queueOrdersToPrepare.add(id); // Adiciona o pedido à fila de preparação
                        System.out.println("\n\n  Garçom " + (idGarcom + 1) + " retirou pedido do cliente "
                                + (id + 1));
                        Thread.sleep(1000); // Pequeno tempo de espera para registrar o pedido
                        currentCapacity++; // Aumenta o número de pedidos em andamento

                        // Se atingir a capacidade máxima, bloqueia a disponibilidade do garçom
                        if (currentCapacity == capacity) {
                            Bar.waitersAvailable.acquire();
                        }
                    }
                }

                // Quando o garçom atingiu a capacidade ou não há mais pedidos na fila
                if (idCliente.size() != 0 && (idCliente.size() == capacity || Bar.queueOrders.isEmpty())) {

                    Thread.sleep(2000); // Pausa antes de levar os pedidos para a copa

                    for (Integer id : idCliente) {
                        System.out.println("\n\n  Garçom " + (idGarcom + 1) + " foi até a copa.");
                        
                        // Entregar pedido ao bartender para preparação
                        Thread.sleep(2000); // Tempo de deslocamento até o bartender
                        boolean prepared = false;
                        while (!prepared) {
                            try {
                                // Aguarda a liberação do bartender para preparar a bebida
                                Bar.preparationBartender.acquire();
                            } finally {
                                new Thread(new BartenderThread(id)).start(); // Inicia a preparação da bebida
                                Thread.sleep(3000); // Aguarda a preparação da bebida
                                prepared = true;
                                Bar.preparationBartender.release(); // Libera o bartender para o próximo pedido
                            }
                        }

                        // O garçom leva o pedido pronto até o cliente
                        System.out.println("\n\n  Garçom " + (idGarcom + 1) + " está indo até o cliente " + (id + 1));

                        Thread.sleep((long) (random.nextInt(3) * 1000)); // Tempo aleatório de deslocamento

                        // O garçom entrega a bebida ao cliente
                        System.out.println("\n\n  Garçom " + (idGarcom + 1) + " entregou pedido ao cliente " + (id + 1));

                        synchronized (Bar.Lock) {
                            Bar.queueOrdersToPrepare.remove(id); // Remove o pedido da fila de preparação
                            Bar.customersServed++; // Incrementa o contador de clientes servidos
                        }

                    }

                    Thread.sleep(3000); // Pausa entre atendimentos para simular descanso do garçom
                    
                    // Libera o garçom para atender novos pedidos
                    synchronized (Bar.Lock) {
                        Bar.waitersAvailable.release(); // Indica que um garçom está disponível novamente
                        currentCapacity = 0; // Reseta a capacidade atual
                        idCliente.clear(); // Limpa a lista de clientes atendidos
                    }
                    System.out.println("\n\n  Garçom " + (idGarcom + 1) + " está disponível");

                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Garante que a interrupção da thread seja tratada corretamente
            }
        }

    }
}
