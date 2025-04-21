import socket
import threading
import signal
import sys

import random
import time

import argparse


class Filial:
    def __init__(self, id):
        
        self.id = id

        print(f"Filial {id} criada.")


    def comprar(self):
        preco = random.randint(100,2000)

        print(f"Filial {self.id}:\n   Compra de R${preco},00.\n")

        return preco

    def vender(self):
        preco = random.randint(200,2300)
        

        print(f"Filial {self.id}:\n   Venda realizada no valor de R${preco},00.")

        return preco






def start_client(host, port, args):

    client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client.connect((host, port))


    filial = Filial(0)

    sleep_ratio = (60*60*24 / 1500) * args.day_ratio


    while True:


        command = random.choice(['sacar', 'depositar'])

        if command=='sacar':
            preco = filial.comprar()

        if command=='depositar':
            preco = filial.vender()


    
        time.sleep(random.random()*sleep_ratio)



        command = f'{command} {preco}'
        print(f"Enviando: {command}")

        client.send(command.encode('utf-8'))

        response = client.recv(1024).decode('utf-8')
        
        print(f'{response}\n\n')

    client.close()

if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--day_ratio', default=1, type=float)
    args = parser.parse_args()



    start_client(host='localhost', port=12345, args=args)
