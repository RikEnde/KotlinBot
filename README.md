# Discord chatbot based on Spring-AI

This bot serves the purpose of trying out Spring-AI, it doesn't offer anything new compared to the thousands of other 
LLM API wrappers out there. If you use your favorite online LLM chatbot only occasionally, this can be cheaper than the 
monthly fee, as you only pay the provider for the actual usage of the API. If you use it heavily, it can be more 
expensive. 

The bot supports the `OpenAI` API and `Anthropic Claude` API for chat and `Dall-e` for image generation. It supports non-persisted 
in memory storage, or postgres or mongodb for local storage. 

## Get your OpenAI API Key

Go to https://platform.openai.com/ and select `API`. You will need to create an account for that. Create an 
organization, and set up billing. Set up `usage limits` in case you make a mistake, or someone steals your API key, and 
you end up with a large surprise bill. With a few dozen calls per day, the cost is in the order of a few dollars 
per month. 

Select `API Keys`. Generate a key. Place this in your environment variables under the key `OPENAI_API_KEY`. 

## get your Anthropic API key

Go to https://console.anthropic.com/settings/keys and select `Create Key`. Select copy key and save that to your 
environment variables under the name `ANTHROPIC_KEY`. You will need to create an account for that and set up billing. 
Don't forget to set `Limits > Spend Limits` or you will end up with a very large surprise bill if your key leaks. 

## Create the Discord Bot

Do the following steps in **Chrome**, the Discord Developers Portal **doesn't work in Safari**. 

Go to the [Discord developer portal](https://discord.com/developers/docs/getting-started) and select `get started`. 
Go to [Applications > New Application](https://discord.com/developers/applications?new_application=true), give the app 
a name and click `Create`

Go to the `Bot` section. You can give the bot a name, that will be its username in the Discord channel. 
here you can select the `permissions` and `intents` the bot has. It will need Message Content Intent.  

On this page you will find the `token`. Copy this down in your environment variables under the key 
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
conversation history of a user. The `/history` command shows the last 10 images generated for the user, 
and `/history` followed by a number will re-post the image. 

Examples:
```
/prompt what is the best way to hit a nail on the head? 

/image a banana in a bikini

/role an extremely pendantic English professor who can't stop talking about anime

/prompt summarize the conversation so far but in Korean

/history 4
```

In direct messages, the /prompt command isn't necessary. The bot can remember conversations on a per-user basis: 

```
Rick - Hi how is it going?

Bot - Hi! I'm here and ready to help. How can I assist you today?

/image a healthy nutritious breakfast

Rick - what's your opinion on the importance of a healthy nutritious breakfast ?

Bot - A healthy, nutritious breakfast is crucial for several reasons. It kick-starts your 
      metabolism, helping you burn calories throughout the day. Consuming a balanced 
      breakfast can improve concentration and performance, particularly in school 
      or work settings. It provides essential nutrients that might be harder to 
      obtain later in the day. Additionally, having a nutritious breakfast can 
      help in maintaining a healthy weight by reducing hunger and preventing 
      unhealthy snacking later on. Overall, it sets a positive tone for making 
      healthy choices throughout the day.
      
Rick - describe the conversation so far

Bot - The conversation began with a greeting, followed by a request to generate 
      an image of a healthy, nutritious breakfast. I then described the 
      components of such a breakfast visually. Next, you asked for my perspective 
      on the importance of a healthy, nutritious breakfast, and I provided 
      several reasons why it's beneficial for health and well-being.
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

The bot supports the OpenAI and Anthropic Claude APIs for chat and Dall-e for image generation. For local storage 
the bot supports Postgres and MongoDB and the in-memory database H2. These will have to be configured by setting the 
environment variables:

```
export DISCORD_TOKEN=your_discord_key_here
export OPENAI_KEY=your_open_ai_api_key_here
export ANTHROPIC_KEY=your_anthropic_api_key_here
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres
export POSTGRES_DB=postgres
export SPRING_PROFILES_ACTIVE=openai,h2,dalle


```

### Docker 

Build the image: 
```
mvn spring-boot:build-image

export SPRING_PROFILES_ACTIVE=openai,h2,dalle 

docker compose up kotlinbot
```

For persisted storage, replace h2 with postgres or mongo, and start the corresponding containers

```
export SPRING_PROFILES_ACTIVE=anthropic,postgres,dalle
docker compose up postgres kotlinbot
```

To run it without docker use spring-boot:run. There are 3 maven profiles defined: `h2, postgres, mongo`, 
all use openai and dall-e. 

```
mvn spring-boot:run -Ph2
```

## TODO

- Upload and analyze images
- Upload documents and answer questions about the content 
- Become sentient and take over the world