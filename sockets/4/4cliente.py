import socket

def start_client(host='localhost', port=12345):
    client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client.connect((host, port))

    while True:
        command = input('Digite o comando: ')
        if command == 'exit':
            client.send(command.encode('utf-8'))
            break

        client.send(command.encode('utf-8'))
        response = client.recv(1024).decode('utf-8')
        print(response)

    client.close()

if __name__ == '__main__':
    start_client()
