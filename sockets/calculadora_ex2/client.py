import socket

def main():
    host = 'localhost'
    port = 12345

    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((host, port))

    try:
        while True:
            message = input("Digite a expressão (x op y) ou 'sair' para encerrar: ")
            if message.lower() == 'sair':
                break
            client_socket.send(message.encode())
            result = client_socket.recv(1024).decode()
            print("Resultado: ", result)
    finally:
        client_socket.close()

if __name__ == "__main__":
    main()