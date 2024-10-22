## Get your OpenAI API Key

Go to https://platform.openai.com/ and select `API`. You will need to create an account for that. Create an 
organization, and set up billing. Set up `usage limits` in case you make a mistake, or someone steals your API key, and 
you end up with a large surprise bill. With a few dozen calls per day, the cost is in the order of a few dollars 
per month. 

Select `API Keys`. Generate a key. Place this in your environment variables or `.env` under the key `OPENAI_API_KEY`. 

## Create the Discord Bot

Do the following steps in **Chrome**, the Discord Developers Portal **doesn't work in Safari**. 

Go to the [Discord developer portal](https://discord.com/developers/docs/getting-started) and select `get started`. 
Go to [Applications > New Application](https://discord.com/developers/applications?new_application=true), give the app 
a name and click `Create`

Go to the `Bot` section. You can give the bot a name, that will be its username in the Discord channel. 
here you can select the `permissions` and `intents` the bot has. It will need Message Content Intent.  

On this page you will find the `token`. Copy this down in your environment variables or your `.env` under the key 
`DISCORD_TOKEN`. You won't be able to view or edit this token, if you lose it you have to generate a new one. 

## Invite the bot to your discord server

Generate an invite-link: Go to `OAuth2>URL generator`. 
Under `Scopes`, select `bot`.
Then, under `Bot Permissions`, choose the permissions this bot needs. Choose `Read messages/View Channels` and 
`Send messages`. 
This will generate a URL which you can use to invite the bot to your Discord server.

Invite the Bot: Use the generated URL to add the bot to a server. You must have the necessary permissions 
on the server to add a bot.

## Using the bot

The bot responds to direct messages, or on a channel to commands like `/prompt` and `/image`. You can set the bot's `/role` to make it 
role play. `/role` without an argument will set a random role. `/summary` returns a summary of the current conversation as far as the bot remembers. 
This is not persisted between restarts. `/temp` sets the `temperature`, in a range between 0.0 and 1.0. The higher 
this number, the more random, or creative the response becomes. Above a certain temperature, the output becomes 
nonsense. `/tokens` sets the maximum number of tokens of the response. What tokens are is a bit fuzzy, it's more 
than letters but less than words. The maximum is 4096 at this time. Shorter responses are faster. `/forget` clears the 
conversation history of a user. 

Examples:
```
/prompt what is the best way to hit a nail on the head? 

/image a banana in a bikini

/role an extremely pendantic English professor who can't stop talking about anime

/prompt summarize the conversation so far but in Korean
```

## Abuse

The bot has no content filter, but the OpenAI API does, and they will ban you if you attempt too 
conspicuously to circumvent it  

Examples:
```
/image a banana with boobs

Error code: 400 - {'error': {'code': 'content_policy_violation', 'message': 'Your request was rejected as a result of 
our safety system. Your prompt may contain text that is not allowed by our safety system.', 'param': None, 'type': 
'invalid_request_error'}}
```

## Development

GraalVM build: 

Gathering reflection information:
```
mvn clean package 
java -agentlib:native-image-agent=config-output-dir=. -jar target/SpringBot-1.0-SNAPSHOT.jar
```
Run the bot for a while and test all the features, then exit. 

update `proxy-cofig.json` and `reflect-config.json` with the files generated in the previous step. 

Build the native image:
```
mvn package -Pnative
./target/javabot
```

### Docker 

Use the spring boot native-maven-plugin:
```
mvn spring-boot:build-image -Pnative
```

### Choose local database 

Postgres
```
   mvn clean spring-boot:run -Ppostgres 
```

MongoDB
```
   mvn clean spring-boot:run -Pmongo 
```