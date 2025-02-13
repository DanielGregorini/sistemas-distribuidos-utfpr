import threading
import time
import random

class BarbeiroDorminhoco:
    def __init__(self, cadeiras):
        self.cadeiras = cadeiras
        self.ocupadas = 0 
        self.lock = threading.Lock()
        self.cliente_cond = threading.Condition(self.lock)
        self.barbeiro_cond = threading.Condition(self.lock)

    def cortar_cabelo(self, cliente_id):

        with self.lock:
            if self.ocupadas == self.cadeiras:
                print(f"Cliente {cliente_id} chegou e NÃO achou cadeira. Vai embora!")
                return
            # cliente encontra uma cadeira e senta:
            self.ocupadas += 1
            print(f"Cliente {cliente_id} entrou e sentou. Cadeiras ocupadas: {self.ocupadas}/{self.cadeiras}")
            # se o barbeiro estiver dormindo (nenhum cliente estava esperando), acorda-o:
            if self.ocupadas == 1:
                print(f"Cliente {cliente_id} acorda o barbeiro.")
                self.barbeiro_cond.notify()
            # o cliente espera ser chamado para o corte (ponto de sincronização com o barbeiro):
            self.cliente_cond.wait()
            # apos ser sinalizado, o cliente deixa a cadeira de espera:
        
            self.ocupadas -= 1

        # Fora da região crítica, o cliente "recebe o corte"
        print(f"Cliente {cliente_id} está sendo atendido (corte em andamento).")

    def proximo_cliente(self):

        with self.lock:
            if self.ocupadas == 0:
                print("Barbeiro dormindo... (não há clientes)")
                self.barbeiro_cond.wait()  # espera um cliente acordá-lo
            # ha pelo menos um cliente esperando; chama-o para o atendimento:
            print("Barbeiro chama o próximo cliente.")
            self.cliente_cond.notify()


def barbeiro_thread(barbearia):
    while True:
        # espera e chama um cliente (se não houver, espera na condição)
        barbearia.proximo_cliente()
        # simula o corte de cabelo (tempo variável)
        print("Barbeiro está cortando o cabelo...")
        tempo_corte = random.uniform(1, 3)
        time.sleep(tempo_corte)
        print("Barbeiro terminou o corte.\n")


def cliente_thread(cliente_id, barbearia):
    #simula a chegada do cliente em momentos aleatórios
    time.sleep(random.uniform(0, 5))
    print(f"Cliente {cliente_id} chegou à barbearia.")
    barbearia.cortar_cabelo(cliente_id)
    print(f"Cliente {cliente_id} saiu da barbearia.\n")


def main():
    num_cadeiras = 3  
    num_clientes = 10 

    barbearia = BarbeiroDorminhoco(num_cadeiras)

    # cria e inicia a thread do barbeiro:
    t_barbeiro = threading.Thread(target=barbeiro_thread, args=(barbearia,))
    t_barbeiro.daemon = True  # thread do barbeiro como daemon para encerrar junto com o programa
    t_barbeiro.start()

    #cria e inicia as threads dos clientes:
    threads_clientes = []
    for cliente_id in range(1, num_clientes + 1):
        t = threading.Thread(target=cliente_thread, args=(cliente_id, barbearia))
        threads_clientes.append(t)
        t.start()

    # espera que todas as threads de clientes terminem:
    for t in threads_clientes:
        t.join()

    print("Simulação finalizada.")


if __name__ == '__main__':
    main()
