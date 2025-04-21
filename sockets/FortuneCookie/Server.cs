using System.Net;
using System.Net.Sockets;

namespace FortuneCookie;

public class Server
{
    private TcpListener listener;
    private List<string> fortunes =
    [
        "Você será feliz",
        "Você será rico",
        "Você será saudável"
    ];

    public const string Help = "HELP";
    public const string Get = "GET-FORTUNE";
    public const string Add = "ADD-FORTUNE";
    public const string Update = "UPD-FORTUNE";
    public const string Quit = "QUIT";
    public const string List = "LST-FORTUNE";

    public Server(int port = 12345)
    {
        listener = new TcpListener(IPAddress.Any, port);
    }

    public void Start()
    {
        // receive connections and keep connection open
        listener.Start();
        Console.WriteLine("Server started");
        while (true)
        {
            var client = listener.AcceptTcpClient();
            var stream = client.GetStream();
            var reader = new StreamReader(stream);
            var writer = new StreamWriter(stream) { AutoFlush = true };

            Console.WriteLine("Client connected");
            while (true)
            {
                var request = reader.ReadLine();
                if (request == null)
                {
                    break;
                }

                var response = ProcessRequest(request);
                Console.WriteLine($"Request: {request}");

                writer.WriteLine(response);
                Console.WriteLine($"Response: {response}");
            }
            Console.WriteLine("Client disconnected");
        }
    }

    private string ProcessRequest(string request)
    {
        var parts = request.Split(' ');
        var command = parts[0];
        int index;
        string fortune;
        switch (command)
        {
            case Help:
                return $"Commands: {Help}, {Get}, {Add} <fortune>, {Update} <index> <fortune>, {List}";

            case Get:
                index = new Random().Next(fortunes.Count);
                return $"{index} - {fortunes[index]}";

            case Add:
                fortune = string.Join(' ', parts.Skip(1));
                fortunes.Add(fortune);
                return $"Fortune '{fortune}' added";

            case Update:
                if (parts.Length < 3)
                    return "Invalid command";

                int.TryParse(parts[1], out index);
                if (index < 0 || index >= fortunes.Count)
                    return "Invalid index";

                fortune = string.Join(' ', parts.Skip(2));
                string oldFortune = fortunes[index];
                fortunes[index] = fortune;
                return $"Fortune {oldFortune} updated to {fortune}";

            case List:
                return string.Join(", ", fortunes.Select((f, i) => $"{i} - {f}"));

            default:
                return "Unknown command";
        }
    }
}