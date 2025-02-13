import multiprocessing
import time
import random

def producer(lock, buffer):
 
    # Função responsável por produzir itens e inseri-los no buffer (fila).
    while True:  # Loop infinito para produção contínua
        item = random.randint(1, 100)
        time.sleep(random.random() * 3)
        # garante acesso exclusivo ao buffer enquanto verifica e insere o item
        with lock:
            # Verifica se o buffer está cheio; se estiver, pula a iteração e tenta novamente
            if buffer.full():
                continue
            # insere o item no buffer
            buffer.put(item)
            # Imprime uma mensagem indicando que o item foi produzido
            print(f'Produziu {item}')



def consumer(lock, buffer):
    # funcao responsável por consumir itens presentes no buffer.

    while True:  # Loop infinito para consumo contínuo
        # aguarda um tempo aleatório entre 0 e 3 segundos para simular variabilidade no consumo
        time.sleep(random.random() * 3)
        # garante acesso exclusivo ao buffer enquanto verifica e retira um item
        with lock:
            # se o buffer estiver vazio, não há item para consumir; pula para a próxima iteração
            if buffer.empty():
                continue

            item = buffer.get()
            print(f'Consumiu {item}')

if __name__ == '__main__':
    # ccria um objeto Lock para sincronizar o acesso ao buffer entre os processos
    lock = multiprocessing.Lock()
    
    # cria uma fila (buffer) com capacidade máxima de 10 itens
    buffer = multiprocessing.Queue(10)

    producer_process = multiprocessing.Process(target=producer, args=(lock, buffer))
    consumer_process = multiprocessing.Process(target=consumer, args=(lock, buffer))

    # inicia o processo produtor
    producer_process.start()
    # inicia o processo consumidor
    consumer_process.start()

    # permite que os processos executem por 450 segundos (7,5 minutos)
    time.sleep(450)

    producer_process.join()
    consumer_process.join()