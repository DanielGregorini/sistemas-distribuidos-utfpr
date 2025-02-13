from multiprocessing import Process, Lock, Manager, Value, Array
from multiprocessing import set_start_method
import random
import time
import argparse
import sys

class Filosofo:
    def __init__(self, id):
        self.id = id
        # 0 - Meditando / 1 - Com fome (pronto para comer)
        self.estado = Value('i', 0)

    def prepara_pedido(self, hashis):
      
        while True:
            # Se o filosofo esta meditando (estado 0)
            if self.estado.value == 0:
                print(f'Filósofo {self.id} está meditando.')
                # simula o tempo de meditação com uma pausa aleatoria entre 5 e 7 segundos
                tempo = random.randint(5, 7)
                time.sleep(tempo)
                # apos meditar, o filósofo fica com fome e muda seu estado para 1
                self.estado.value = 1

            # ae o filosofo está com fome (estado 1)
            if self.estado.value == 1:
                print(f'Filósofo {self.id} com fome.')
                i = self.id
                # tenta pegar o primeiro hashi (o da sua posição)
                with hashis[i]:
                    if i == 4:
                        with hashis[0]:
                            print(f'\nFilósofo {i} está comendo com os hashis {i} e 0.\n')
                
                            tempo = random.randint(3, 15)
                            time.sleep(tempo)
                            print(f'\nFilósofo {i} liberou hashis {i} e 0.\n')
                    else:
                      
                        with hashis[i+1]:
                            print(f'\nFilósofo {i} está comendo com os hashis {i} e {i+1}.\n')
                          
                            tempo = random.randint(3, 15)
                            time.sleep(tempo)
                            print(f'\nFilósofo {i} liberou hashis {i} e {i+1}.\n')
                
                # depois de comer
                self.estado.value = 0

if __name__ == '__main__':
    # cria um Manager para gerenciar recursos compartilhados entre processos, se necessário
    with Manager() as manager:
        # cria uma lista de 5 locks que representam os 5 hashis
        # cada lock garante que somente um filósofo possa usar o hashi ao mesmo tempo
        hashis = [Lock() for i in range(5)]

        # cria uma lista de 5 filosofos, cada um identificado por seu indice
        filosofos = [Filosofo(i) for i in range(5)]

        # criacao dos processos dos filosofos
        p_filosofos = []
        for filosofo in filosofos:
            p_filosofo = Process(target=filosofo.prepara_pedido, args=(hashis,))
            p_filosofos.append(p_filosofo)
            p_filosofo.start()

    time.sleep(150)
    print('Finalizando.')

    for p in p_filosofos:
        if p.is_alive():
            p.terminate()
            p.join()
