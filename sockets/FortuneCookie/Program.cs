// dotnet run -- server to start as server
// dotnet run to start as client

using FortuneCookie;

if (args.Length > 0 && args[0] == "server")
{
    var server = new Server();
    server.Start();
}
else
{
    var client = new Client();
    client.Start();
}