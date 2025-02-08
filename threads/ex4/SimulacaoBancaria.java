import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ContaBancaria {

    private double saldo;
    private Lock lock = new ReentrantLock();

    public ContaBancaria(double saldoInicial) {
        this.saldo = saldoInicial;
    }

    public void deposito(double valor) {
        lock.lock();
        try {
            saldo += valor;
            System.out.println(
                "Depósito de " + valor + ", novo saldo: " + saldo
            );
        } finally {
            lock.unlock();
        }
    }

    public void saque(double valor) {
        lock.lock();
        try {
            if (saldo >= valor) {
                saldo -= valor;
                System.out.println(
                    "Saque de " + valor + ", novo saldo: " + saldo
                );
            } else {
                System.out.println("Saldo insuficiente para saque de " + valor);
            }
        } finally {
            lock.unlock();
        }
    }

    public void transferencia(ContaBancaria destino, double valor) {
        lock.lock();
        try {
            if (saldo >= valor) {
                saque(valor);
                destino.deposito(valor);
                System.out.println(
                    "Transferência de " +
                    valor +
                    " para conta destino, novo saldo: " +
                    saldo
                );
            } else {
                System.out.println(
                    "Saldo insuficiente para transferência de " + valor
                );
            }
        } finally {
            lock.unlock();
        }
    }

    public void creditoDeJuros(double taxa) {
        lock.lock();
        try {
            saldo += saldo * taxa;
            System.out.println(
                "Crédito de juros de " +
                (taxa * 100) +
                "%, novo saldo: " +
                saldo
            );
        } finally {
            lock.unlock();
        }
    }
}

public class SimulacaoBancaria {

    public static void main(String[] args) {
        ContaBancaria conta1 = new ContaBancaria(1000);
        ContaBancaria conta2 = new ContaBancaria(2000);

        Runnable deposito = () -> conta1.deposito(100);
        Runnable saque = () -> conta1.saque(50);
        Runnable transferencia = () -> conta1.transferencia(conta2, 200);
        Runnable creditoJuros = () -> conta1.creditoDeJuros(0.05);

        Thread t1 = new Thread(deposito);
        Thread t2 = new Thread(saque);
        Thread t3 = new Thread(transferencia);
        Thread t4 = new Thread(creditoJuros);

        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }
}
