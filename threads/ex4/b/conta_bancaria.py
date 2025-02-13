import threading
import random
import time

class ContaBancaria:
    def __init__(self, numero, saldo):
        self.numero = numero         # Número da conta
        self.saldo = saldo           # Saldo inicial da conta
        self._lock = threading.Lock()  # Lock para garantir acesso exclusivo durante as operações

    def depositar(self, valor):

        with self._lock:
            if valor > 0:
                self.saldo += valor
                print(f"Conta: {self.numero} - Depósito de {valor:.2f}. Saldo: {self.saldo:.2f}")
            else:
                print(f"Conta: {self.numero} - Valor inválido para depósito: {valor:.2f}")
        # Atraso para simular o tempo de processamento da operação
        time.sleep(random.uniform(0.1, 0.5))

    def sacar(self, valor):

        with self._lock:
            if valor > 0 and self.saldo >= valor:
                self.saldo -= valor
                print(f"Conta: {self.numero} - Saque de {valor:.2f}. Saldo: {self.saldo:.2f}")
            else:
                print(f"Conta: {self.numero} - Saldo insuficiente para saque de {valor:.2f}")
        time.sleep(random.uniform(0.1, 0.5))

    def transferir(self, destino, valor):
        with self._lock:
            if valor > 0 and self.saldo >= valor:
                self.saldo -= valor
                # Bloqueia a conta destino para atualizar seu saldo com segurança
                with destino._lock:
                    destino.saldo += valor
                print(f"Conta: {self.numero} - Transferência enviada de {valor:.2f} para conta {destino.numero}. Saldo: {self.saldo:.2f}")
                print(f"\tConta: {destino.numero} - Transferência recebida de {valor:.2f} da conta {self.numero}. Saldo: {destino.saldo:.2f}")
            else:
                print(f"Conta: {self.numero} - Saldo insuficiente para transferência de {valor:.2f}")
        time.sleep(random.uniform(0.1, 0.5))

    def creditar_juros(self, taxa):
        with self._lock:
            juros = self.saldo * taxa
            self.saldo += juros
            print(f"Conta {self.numero} - Crédito de juros de {juros:.2f}. Saldo: {self.saldo:.2f}")
        time.sleep(random.uniform(0.1, 0.5))


def main():
    # criao de duas contas bancárias com saldo inicial
    conta1 = ContaBancaria(1, 1000)
    conta2 = ContaBancaria(2, 500)

    # criacao de threads para realizar as operações simultaneamente
    threads = [
        threading.Thread(target=conta1.depositar, args=(200,)),
        threading.Thread(target=conta1.sacar, args=(100,)),
        threading.Thread(target=conta1.transferir, args=(conta2, 300)),
        threading.Thread(target=conta1.credititar_juros, args=(0.01,)),  # Atenção: método renomeado para seguir o padrão Python
        threading.Thread(target=conta2.depositar, args=(100,)),
        threading.Thread(target=conta2.sacar, args=(50,)),
        threading.Thread(target=conta2.transferir, args=(conta1, 200)),
        threading.Thread(target=conta2.credititar_juros, args=(0.01,))
    ]
    
    # inicia todas as threads
    for t in threads:
        t.start()

    # aguarda a finalização de todas as threads
    for t in threads:
        t.join()

    print("\n\nSimulação de operações concluída")

ContaBancaria.credititar_juros = ContaBancaria.creditar_juros

if __name__ == '__main__':
    main()
