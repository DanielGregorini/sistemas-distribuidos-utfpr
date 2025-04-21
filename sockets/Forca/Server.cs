using System.Net;
using System.Net.Sockets;

namespace Forca;

public class Server
{
    private TcpListener listener;

    private List<string> words =
    [
        "algoritmo",
        "binario",
        "processador",
        "memoria",
        "compilador",
        "sockets",
        "frontend",
        "backend",
        "servidor",
        "cliente"
    ];

    private string chosenWord;
    private List<char> guessedLetters = [];
    private bool spoiler;
    private bool gameStarted;

    public Server(int port = 12345, bool spoiler = false)
    {
        listener = new TcpListener(IPAddress.Any, port);
        this.spoiler = spoiler;
    }

    public void Start()
    {
        listener.Start();
        Console.WriteLine("Servidor iniciado");
        while (true)
        {
            var client = listener.AcceptTcpClient();
            var stream = client.GetStream();
            var reader = new StreamReader(stream);
            var writer = new StreamWriter(stream) { AutoFlush = true };

            Console.WriteLine("Cliente conectado");
            while (true)
            {
                var request = reader.ReadLine();
                if (request == null)
                    break;

                var response = ProcessRequest(request);
                writer.WriteLine(response);
            }
            Console.WriteLine("Cliente desconectado");
        }
    }

    private string ProcessRequest(string request)
    {
        if (request.StartsWith("INICIAR"))
        {
            ChooseWord();
            if (spoiler) Console.WriteLine(chosenWord);
            guessedLetters = [];
            gameStarted = true;
            return FormatOutput("Jogo iniciado");
        }

        if (!gameStarted)
            return "Jogo não iniciado";

        if (request.StartsWith("TENTAR"))
        {
            var word = request.Split(' ')[1].ToLower();
            if (word == chosenWord)
            {
                gameStarted = false;
                return FormatOutput("Parabéns! Você acertou!", correct: true);
            }

            return FormatOutput("Que pena! Você errou!");
        }

        if (char.IsLetter(request[0]) && request.Length == 1)
        {
            var letter = request[0];
            guessedLetters.Add(letter);
            if (CorrectGuesses())
            {
                gameStarted = false;
                return FormatOutput("Parabéns! Você acertou!", correct: true);
            }

            if (WrongGuesses() == 6)
            {
                gameStarted = false;
                return FormatOutput("Que pena! Você perdeu! :(");
            }

            return FormatOutput(chosenWord.Contains(letter)
                ? "Letra correta!" : "Letra incorreta!");
        }

        return "Comando inválido";
    }

    private string FormatOutput(string message, bool correct = false)
    {
        string output = string.Empty;

        // Draw hangman
        output += "  +---+\n";
        output += $"  |   {(WrongGuesses() > 0 ? "O" : "")}\n";
        output += $"  |  {(WrongGuesses() > 2 ? "/" : " ")}{(WrongGuesses() > 1 ? "|" : "")}{(WrongGuesses() > 3 ? "\\" : "")}\n";
        output += $"  |  {(WrongGuesses() > 4 ? "/" : "")} {(WrongGuesses() > 5 ? "\\" : "")}\n";
        output += "  |\n";
        output += "=========\n";

        string word = "Palavra: ";
        foreach (var letter in chosenWord)
        {
            char s;
            if (correct)
                s = letter;
            else
                s = guessedLetters.Contains(letter) ? letter : '_';

            word += $"{s} ";
        }

        output += word + Environment.NewLine + Environment.NewLine;

        string guessed = "Letras erradas: ";
        foreach (var letter in guessedLetters)
        {
            if (!chosenWord.Contains(letter))
                guessed += $"{letter} ";
        }
        output += guessed;

        output += Environment.NewLine + Environment.NewLine;
        output += $"\"{message}\"";

        return output;
    }

    private void ChooseWord()
    {
        var random = new Random();
        var index = random.Next(words.Count);
        chosenWord = words[index];
    }

    private bool CorrectGuesses() => chosenWord.All(letter => guessedLetters.Contains(letter));

    private int WrongGuesses() => guessedLetters.Count(letter => !chosenWord.Contains(letter));
}