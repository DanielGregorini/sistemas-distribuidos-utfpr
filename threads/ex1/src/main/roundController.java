/**
 * Classe responsável por controlar as rodadas do bar.
 * Define o tempo de duração de cada rodada e encerra o bar após a última rodada.
 */
public class roundController extends Thread {

    @Override
    public void run() {

        // Controla o número de rodadas que o bar permanecerá aberto
        for (int i = 0; i < Bar.NUM_ROUNDS; i++) {
            try {
                // Inicia uma nova rodada e informa no console
                System.err.println("\n\n_________Rodada " + (i + 1) + "__________");
                
                // Simula o tempo de duração da rodada (20 segundos)
                Thread.sleep(20000);
                
                // Finaliza a rodada atual e exibe a mensagem de encerramento
                System.err.println("\n\n_________Rodada " + (i + 1) + " Finalizada__________");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Incrementa o contador de rodadas
            Bar.round++;
        }

        // Define que o bar está fechado após o término das rodadas
        Bar.barOpen = false;

        // Exibe o número de pedidos entregues durante o funcionamento do bar
        System.out.println("\n Pedidos entregues: " + Bar.customersServed);

        // Exibe mensagem indicando o encerramento do serviço
        System.out.println("\n Bar fechado, finalizando o serviço");

        // Finaliza a execução do programa
        System.exit(0);
    }

}
