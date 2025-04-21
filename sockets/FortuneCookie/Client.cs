using System.Net.Sockets;

namespace FortuneCookie;

public class Client(string host = "localhost", int port = 12345)
{
    private string _host = host;
    private int _port = port;

    public void Start()
    {
        using var client = new TcpClient(_host, _port);
        var stream = client.GetStream();
        var reader = new StreamReader(stream);
        var writer = new StreamWriter(stream) { AutoFlush = true };

        Console.WriteLine("Connected to server");
        Console.WriteLine("Type HELP for commands");
        while (true)
        {
            Console.Write("> ");
            var input = Console.ReadLine();
            if (input == null || input.Equals("QUIT", StringComparison.CurrentCultureIgnoreCase))
                break;

            writer.WriteLine(input);
            var response = reader.ReadLine();
            Console.WriteLine(response);
        }
    }
}