import socket
import threading
import signal
import sys

import time

# Classe para representar a conta bancária
class Account:
    def __init__(self, balance=0):
        self.balance = balance
        self.lock = threading.Lock()

    def deposit(self, amount):
        with self.lock:
            self.balance += amount
            time.sleep(8)
            return self.balance

    def withdraw(self, amount):
        with self.lock:
            if self.balance >= amount:
                time.sleep(8)
                self.balance -= amount
                return self.balance
            else:
                return None

    def get_balance(self):
        with self.lock:
            time.sleep(2)
            return self.balance

# Dicionário para armazenar as contas
accounts = {}

# Função para lidar com as conexões dos clientes
def handle_client(client_socket):
    while True:
        try:
            request = client_socket.recv(1024).decode('utf-8')
            if not request:
                break
            
            parts = request.split()
            command = parts[0]
            account_id = parts[1]

            if command == 'criar':
                accounts[account_id] = Account()
                client_socket.send(f'Conta {account_id} criada.\n'.encode('utf-8'))

            elif command == 'depositar':
                amount = float(parts[2])
                balance = accounts[account_id].deposit(amount)
                client_socket.send(f'Depósito de {amount} realizado. Saldo atual: R${balance}.\n'.encode('utf-8'))

            elif command == 'sacar':
                amount = float(parts[2])
                balance = accounts[account_id].withdraw(amount)
                if balance is not None:
                    client_socket.send(f'Saque de {amount} realizado. Saldo atual: R${balance}.\n'.encode('utf-8'))
                else:
                    client_socket.send('Saldo insuficiente.\n'.encode('utf-8'))

            elif command == 'saldo':
                balance = accounts[account_id].get_balance()
                client_socket.send(f'Saldo atual: R${balance}.\n'.encode('utf-8'))

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
    print(f'Servidor iniciado na porta {port}.')

    # Registrar o manipulador de sinal para interrupção do teclado
    signal.signal(signal.SIGINT, signal_handler)

    while True:
        try:
            client_socket, addr = server.accept()
            print(f'Conexão de {addr}.')
            client_handler = threading.Thread(target=handle_client, args=(client_socket,))
            client_handler.start()
        except OSError:
            break

if __name__ == '__main__':
    start_server()
