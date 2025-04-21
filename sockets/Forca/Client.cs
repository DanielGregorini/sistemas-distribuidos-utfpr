using System.Net.Sockets;
using System.Text;

namespace Forca;

public class Client(string host = "localhost", int port = 12345)
{
    private string _host = host;
    private int _port = port;

    public void Start()
    {
        using var client = new TcpClient(_host, _port);
        using var stream = client.GetStream();
        var writer = new StreamWriter(stream) { AutoFlush = true };

        Console.WriteLine("Conectado ao servidor");
        Console.WriteLine("Digite INICIAR para começar o jogo");
        while (true)
        {
            Console.Write("> ");
            var input = Console.ReadLine();
            if (input == null || input.Equals("SAIR", StringComparison.CurrentCultureIgnoreCase))
                break;

            writer.WriteLine(input);

            byte[] buffer = new byte[1024];
            string response = Encoding.UTF8.GetString(buffer, 0, stream.Read(buffer));
            Console.WriteLine(response);
        }
    }
}