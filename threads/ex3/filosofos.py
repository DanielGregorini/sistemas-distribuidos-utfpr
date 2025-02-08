from multiprocessing import Process, Lock, Manager, Value, Array
from multiprocessing import set_start_method
import random

import time
import argparse
import sys


class Filosofo:
    def __init__(self, id):
        self.id = id
        self.estado = Value('i', 0)


    def prepara_pedido(self, hashis):


        while True:
            
            if self.estado.value == 0:
                print(f'Filósofo {self.id} está meditando.')

                tempo = random.randint(5,7)
                time.sleep(tempo)

                self.estado.value = 1

            
            if self.estado.value == 1:
                print(f'Filósofo {self.id} com fome.')

                i = self.id

                with hashis[i]:
                    if i==4:
                        with hashis[0]:
                            print(f'\nFilósofo {i} está comendo com os hashis {i} e 0.\n')
                            tempo = random.randint(3,15)
                            time.sleep(tempo)

                            print(f'\nFilósofo {i} liberou hashis {i} e 0.\n')
                    else:
                        with hashis[i+1]:
                            print(f'\nFilósofo {i} está comendo com os hashis {i} e {i+1}.\n')
                            tempo = random.randint(3,15)
                            time.sleep(tempo)

                            print(f'\nFilósofo {i} liberou hashis {i} e {i+1}.\n')
                
                self.estado.value = 0


if __name__=='__main__':

    with Manager() as manager:

        hashis = [Lock() for i in range(5)]

        #hashis = manager.list()

        filosofos = [Filosofo(i) for i in range(5)]

        '''Threads'''
        

        p_filosofos = []
        for filosofo in filosofos:
            p_filosofo = Process(target=filosofo.prepara_pedido,
                                    args=(
                                        hashis,
                                    )
                                )
            p_filosofos.append(p_filosofo)
            p_filosofo.start()

    time.sleep(450)
    print('Finalizando.')

    for p in p_filosofos:
        if p.is_alive():
            p.terminate()
            p.join()

    