import java.util.Random;

/**
 * Classe que representa um cliente no bar. Cada cliente chega ao bar,
 * faz pedidos e aguarda o atendimento dos garçons.
 */
class ClientThread extends Thread {
    private int idCliente; // Identificação do cliente
    Random random = new Random(); // Gerador de números aleatórios para simular tempos variáveis

    /**
     * Construtor que inicializa o cliente com um identificador único.
     *
     * @param id Identificação do cliente
     */
    public ClientThread(int id) {
        this.idCliente = id;
    }

    @Override
    public void run() {

        try {
            // Simula o tempo de chegada do cliente ao bar
            Thread.sleep((long) (random.nextInt(6) * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.err.println("\nCliente " + (idCliente + 1) + " entrou no restaurante.");

        while (Bar.barOpen) {
            Random random2 = new Random();
            try {

                // Simula um tempo de espera antes de fazer um pedido
                Thread.sleep((long) (random2.nextInt(6) * 1000));

                // Verifica se há garçons disponíveis
                int availablePermits = Bar.waitersAvailable.availablePermits();
                if (availablePermits != 0) {
                    // Verifica se o cliente já está na fila de pedidos
                    boolean exist = Bar.queueOrders.contains(idCliente);
                    if (!exist) {
                        synchronized (Bar.Lock) {
                            // Cliente adiciona seu pedido na fila
                            Bar.queueOrders.add(idCliente);
                            System.out.println(
                                    "\n\nCliente " + (idCliente + 1) + " chamou um garçom para realizar um pedido.");
                        }
                        System.out.println("\n\nCliente " + (idCliente + 1) + " está esperando por garçons");

                        // Aguarda enquanto seu pedido está na fila
                        while (Bar.queueOrders.contains(idCliente)) {
                            if (!Bar.queueOrders.contains(idCliente)) {
                                break;
                            }
                            Thread.sleep(1000);
                        }

                        System.out.println("\n\nCliente " + (idCliente + 1) + " está esperando seu pedido.");

                        // Aguarda enquanto seu pedido está na fila de preparação
                        while (Bar.queueOrdersToPrepare.contains(idCliente)) {
                            if (!Bar.queueOrdersToPrepare.contains(idCliente)) {
                                break;
                            }
                            Thread.sleep(1000);
                        }

                        // Simula o tempo de consumo da bebida
                        Thread.sleep(1000);
                        System.out.println("\n\nCliente " + (idCliente + 1) + " está bebendo o drink.");
                        Thread.sleep((long) (random2.nextInt(6) * 1000)); // Tempo variável para consumir o pedido
                        System.out.println("\n\nCliente " + (idCliente + 1) + " terminou de beber.");
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
