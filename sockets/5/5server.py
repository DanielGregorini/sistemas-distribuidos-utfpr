import socket

import signal
import sys

from multiprocessing import Process, Lock, Manager, Value, Array, set_start_method

import random
import time



class Central:
    def __init__(self):
        self.saldo = Value('d', 0)
        self.lock = Lock()


    def saque(self, preco):
        
        with self.lock:
            if preco > self.saldo.value:
                return f'\n\n\n\nDepósito de {preco} falhou. Saldo R$: {self.saldo.value}\n\n\n'
            self.saldo.value -= preco
            return f'Saque relizado com sucesso.'

    def deposito(self, preco):
        with self.lock:
            
            self.saldo.value += preco
            return f'Depósito relizado com sucesso.'
        
    def _loop(self):
        print(f"\nCentral iniciada.")
        while True:
            time.sleep(15)
            print(f"\nDinheiro da central: R${self.saldo.value}.")




def handle_client(client_socket, central):
    #central = Central()
    while True:
        try:
            request = client_socket.recv(1024).decode('utf-8')

            if not request:
                break
            
            parts = request.split()
            command = parts[0]
            preco = float(parts[1])

            #print(f"Recebido: {parts}")


            if command == 'sacar':
                ret = central.saque(preco)
                client_socket.send(ret.encode('utf-8'))
                
            if command == 'depositar':
                ret = central.deposito(preco)
                client_socket.send(ret.encode('utf-8'))

            elif command == 'exit':
                client_socket.send('Desconectado.\n'.encode('utf-8'))
                break

        except (KeyError, IndexError, ValueError) as e:
            client_socket.send(f'Erro: {e}\n'.encode('utf-8'))

    client_socket.close()





# Função para lidar com a interrupção do teclado
def signal_handler(sig, frame):
    print('Encerrando o servidor...')
    server.close()
    sys.exit(0)

# Função principal para iniciar o servidor
def start_server(port=12345):
    global server
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind(('0.0.0.0', port))
    server.listen(5)
    print(f'\nServidor iniciado na porta {port}.')



    with Manager() as manager:


        central = Central()

        p_central = Process(target=central._loop,
                                args=(
                            ))
                              
        p_central.start()

        # Registrar o manipulador de sinal para interrupção do teclado
        signal.signal(signal.SIGINT, signal_handler)

        i=0
        

        while True:
            try:
                client_socket, addr = server.accept()
                print(f'Conexão de {addr}.')

                client_handler = Process(target=handle_client, args=(client_socket, central))
                client_handler.start()

                print(f"Filial {i} criada.\n")

                i+=1
            except OSError:
                break

if __name__ == '__main__':
    start_server()
