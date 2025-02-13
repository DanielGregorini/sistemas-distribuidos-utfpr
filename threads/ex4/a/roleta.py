import threading
import time
import random

class Casino:

    def __init__(self, num_roletas):
        self.num_roletas = num_roletas
        # lista que representa o estado das roletas:
        # true indica que a roleta está livre, false ocupada
        self.roletas = [True] * num_roletas
        # lock para proteger a lista de roletas.
        self.lock = threading.Lock()
        # variavel de condição para esperar/liberar roletas.
        self.condition = threading.Condition(self.lock)

    def adquirir_roleta(self, jogador_id):

        with self.condition:
            # Enquanto nenhuma roleta estiver livre, espera.
            while not any(self.roletas):
                print(f"Jogador {jogador_id} está esperando por uma roleta livre.")
                self.condition.wait()
            # Encontrar o índice da primeira roleta livre.
            roleta_index = self.roletas.index(True)
            # Marcar a roleta como ocupada.
            self.roletas[roleta_index] = False
            print(f"Jogador {jogador_id} ocupou a roleta {roleta_index}.")
            return roleta_index

    def liberar_roleta(self, roleta_index, jogador_id):

        with self.condition:
            self.roletas[roleta_index] = True
            print(f"Jogador {jogador_id} liberou a roleta {roleta_index}.")
            # Notifica todas as threads aguardando que uma roleta ficou livre.
            self.condition.notify_all()

def jogador(casino, jogador_id, num_jogadas=3):

    for tentativa in range(1, num_jogadas + 1):
        # adquire uma roleta disponível (pode bloquear se nenhuma estiver livre).
        roleta_index = casino.adquirir_roleta(jogador_id)
        print(f"Jogador {jogador_id} está jogando na roleta {roleta_index} (tentativa {tentativa}).")
        
        # simula o tempo de jogo (entre 1 e 3 segundos).
        tempo_jogo = random.uniform(1, 3)
        time.sleep(tempo_jogo)
        
        print(f"Jogador {jogador_id} terminou de jogar na roleta {roleta_index} (tentativa {tentativa}).")
        # libera a roleta após o jogo.
        casino.liberar_roleta(roleta_index, jogador_id)
        
        # Pausa antes de uma nova tentativa (simula tempo entre jogos).
        time.sleep(random.uniform(0.5, 2))
    
    print(f"Jogador {jogador_id} finalizou suas jogadas.")

def main():
    # numero de roletas disponíveis no cassino (pode ser alterado para qualquer valor)
    num_roletas = 3
    
    #  numero de jogadores (threads) que irão jogar.
    num_jogadores = 10

    # cria a instância do cassino.
    casino = Casino(num_roletas)

    # cria uma lista de threads para os jogadores.
    threads = []
    for jogador_id in range(num_jogadores):
        t = threading.Thread(target=jogador, args=(casino, jogador_id))
        threads.append(t)
        t.start()

    # aguarda todas as threads (jogadores) finalizarem suas jogadas.
    for t in threads:
        t.join()

    print("Simulação de roletas concluída.")

if __name__ == '__main__':
    main()
