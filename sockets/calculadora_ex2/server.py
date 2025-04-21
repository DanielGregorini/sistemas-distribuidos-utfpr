import socket

def calcular(expressao):
    try:
        x, op, y = expressao.split()
        x, y = float(x), float(y)
        if op == '+':
            return x + y
        elif op == '-':
            return x - y
        elif op == '*':
            return x * y
        elif op == '/':
            if y != 0:
                return x / y
            else:
                return "Erro: divisão por zero"
        else:
            return "Operação inválida"
    except Exception as e:
        return f"Erro: {str(e)}"

def main():
    host = 'localhost'
    port = 12345

    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((host, port))
    server_socket.listen(1)
    print("Servidor escutando em {}:{}".format(host, port))

    try:
        while True:
            client_socket, addr = server_socket.accept()
            print('Conectado por', addr)

            while True:
                data = client_socket.recv(1024).decode()
                if not data:
                    break
                result = calcular(data)
                client_socket.send(str(result).encode())
                print("Servidor enviou resultado " + str(result) + " para o cliente")

            client_socket.close()
    except KeyboardInterrupt:
        print("Servidor encerrado")
    finally:
        server_socket.close()

if __name__ == "__main__":
    main()