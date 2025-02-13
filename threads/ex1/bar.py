import threading
import time
import random
import queue
import argparse

NUM_CLIENTES = None 
NUM_GARCONS = None
CAPACIDADE = None  
NUM_RODADAS = None 

bar = None           
bartender_queue = None
orders_sem = None

class Order:
    def __init__(self, client_id, round_number):
        self.client_id = client_id
        self.round_number = round_number
        self.start_order_event = threading.Event()
        self.order_placed_event = threading.Event()
        self.order_delivered_event = threading.Event()

class BartenderRequest:
    def __init__(self, waiter_id, orders):
        self.waiter_id = waiter_id
        self.orders = orders  # lista de objetos Order
        self.order_ready_event = threading.Event()

# classe Bar
class Bar:
    def __init__(self):
        global NUM_CLIENTES, NUM_GARCONS, NUM_RODADAS

        self.lock = threading.Lock()
        self.waiting_clients = [] 
        self.total_clients = NUM_CLIENTES
        self.pending_clients = NUM_CLIENTES  
        self.total_rounds = NUM_RODADAS
        self.current_round = 1

        # barreiras para sincronizar início e fim de rodada.
        parties = NUM_CLIENTES + NUM_GARCONS
        self.start_barrier = threading.Barrier(parties, action=self.start_round)
        self.end_barrier = threading.Barrier(parties, action=self.end_round)

    def start_round(self):
        print(f"\n=== Iniciando rodada {self.current_round} ===\n")
        with self.lock:
            self.pending_clients = self.total_clients
            self.waiting_clients = []

    def end_round(self):
        print(f"\n=== Finalizando rodada {self.current_round} ===\n")
        self.current_round += 1

# thread de Cliente
class ClientThread(threading.Thread):
    def __init__(self, client_id):
        super().__init__()
        self.client_id = client_id

    def run(self):
        global bar, orders_sem
        for _ in range(bar.total_rounds):
            #sincroniza o início da rodada
            bar.start_barrier.wait()

            #cada cliente decide aleatoriamente se fará um pedido (50% de chance)
            want_order = random.choice([True, False])
            if want_order:
                #cria o pedido e registra-o na lista compartilhada
                order = Order(self.client_id, bar.current_round)
                with bar.lock:
                    bar.waiting_clients.append(order)
                    bar.pending_clients -= 1
                #libera o semáforo para sinalizar que há um pedido disponível
                orders_sem.release()
                print(f"Cliente {self.client_id} (Rodada {bar.current_round}): solicitou atendimento.")

                #aguarda o garçom sinalizar para iniciar o pedido
                order.start_order_event.wait()

                #simula o tempo para efetuar o pedido
                time.sleep(random.uniform(0.1, 0.5))
                print(f"Cliente {self.client_id} (Rodada {bar.current_round}): fez o pedido.")
                order.order_placed_event.set()  # Sinaliza que o pedido foi feito

                # Aguarda a entrega do pedido
                order.order_delivered_event.wait()
                print(f"Cliente {self.client_id} (Rodada {bar.current_round}): recebeu o pedido.")

                #simula o consumo do pedido
                time.sleep(random.uniform(0.5, 1.5))
            else:
                #se não fizer o pedido, atualiza o contador de clientes pendentes
                with bar.lock:
                    bar.pending_clients -= 1
                print(f"Cliente {self.client_id} (Rodada {bar.current_round}): não solicitou atendimento.")
                time.sleep(random.uniform(0.1, 0.3))

            #sincroniza o fim da rodada
            bar.end_barrier.wait()

        print(f"Cliente {self.client_id}: saiu do bar.")



# thread de Garçom
class WaiterThread(threading.Thread):
    def __init__(self, waiter_id, capacity):
        super().__init__()
        self.waiter_id = waiter_id
        self.capacity = capacity  # Capacidade de atendimento

    def run(self):
        global bar, bartender_queue, orders_sem
        for _ in range(bar.total_rounds):
            bar.start_barrier.wait()
            #enquanto houver pedidos pendentes ou clientes que ainda não decidiram
            while True:
                group = []
                #tenta coletar até 'capacity' pedidos disponíveis
                while len(group) < self.capacity:
                    # Tenta adquirir o semáforo por um curto período (para não travar indefinidamente)
                    acquired = orders_sem.acquire(timeout=0.1)
                    if acquired:
                        with bar.lock:
                            if bar.waiting_clients:
                                order = bar.waiting_clients.pop(0)
                                group.append(order)
                    else:
                        #se nao conseguiu adquirir, verifica se ainda há clientes que possam fazer pedido
                        with bar.lock:
                            if bar.pending_clients == 0:
                                break  # Não há mais pedidos chegando nesta rodada
                # se nenhum pedido foi coletado e não há mais clientes pendentes, encerra a coleta nesta rodada
                with bar.lock:
                    no_more_clients = (bar.pending_clients == 0)
                if not group and no_more_clients:
                    break

                # processa o grupo de pedidos coletados
                for order in group:
                    order.start_order_event.set()  # Chama o cliente para efetuar o pedido
                # aguarda que todos os clientes do grupo efetuem seus pedidos
                for order in group:
                    order.order_placed_event.wait()
                print(f"Garçom {self.waiter_id} (Rodada {bar.current_round}): recebeu os pedidos de um grupo com {len(group)} pedido(s).")

                # cria uma requisição para o bartender e coloca na fila
                request = BartenderRequest(self.waiter_id, group)
                bartender_queue.put(request)
                # aguarda a confirmação do bartender de que o pedido foi processado
                request.order_ready_event.wait()
                print(f"Garçom {self.waiter_id} (Rodada {bar.current_round}): recebeu confirmação do bartender.")

                # entrega o pedido para cada cliente do grupo
                for order in group:
                    order.order_delivered_event.set()
                print(f"Garçom {self.waiter_id} (Rodada {bar.current_round}): entregou os pedidos do grupo.")
            bar.end_barrier.wait()
        print(f"Garçom {self.waiter_id}: encerrou o turno.")

# thread do Bartender
class BartenderThread(threading.Thread):
    def __init__(self, bartender_id):
        super().__init__()
        self.bartender_id = bartender_id

    def run(self):
        global bartender_queue
        while True:
            request = bartender_queue.get()
            if request is None:
                #sinal de termino da simulação
                break
            round_num = request.orders[0].round_number if request.orders else "?"
            print(f"Bartender: processando pedido do Garçom {request.waiter_id} (Rodada {round_num}).")
            #simula o tempo de preparo do pedido
            time.sleep(random.uniform(0.8, 1.4))
            print(f"Bartender: finalizou pedido do Garçom {request.waiter_id}.")
            request.order_ready_event.set()
        print("Bartender: encerrando o turno.")

#principal
def main():
    global NUM_CLIENTES, NUM_GARCONS, CAPACIDADE, NUM_RODADAS, bar, bartender_queue, orders_sem

    # atribui as variaveis globais
    NUM_CLIENTES = 10
    NUM_GARCONS = 3
    CAPACIDADE = 4
    NUM_RODADAS = 1

    #inicializa os objetos compartilhados globais
    bar = Bar()
    bartender_queue = queue.Queue()
    orders_sem = threading.Semaphore(0)

    # cria e inicia a thread do bartender
    bartender = BartenderThread(1)
    bartender.start()

    # cria e inicia as threads dos garçons
    waiters = []
    for i in range(1, NUM_GARCONS + 1):
        waiter = WaiterThread(i, CAPACIDADE)
        waiter.start()
        waiters.append(waiter)

    # cria e inicia as threads dos clientes
    clients = []
    for i in range(1, NUM_CLIENTES + 1):
        client = ClientThread(i)
        client.start()
        clients.append(client)

    # aguarda o termino de todas as threads dos clientes
    for client in clients:
        client.join()

    # aguarda o termino das threads dos garçons
    for waiter in waiters:
        waiter.join()

    # encerra o bartender
    bartender_queue.put(None)
    bartender.join()

if __name__ == "__main__":
    main()




    