// dotnet run -- server to start as server
// dotnet run to start as client

using Forca;

if (args.Length > 0 && args[0] == "server")
{
    var server = new Server(spoiler: true);
    server.Start();
}
else
{
    var client = new Client();
    client.Start();
}