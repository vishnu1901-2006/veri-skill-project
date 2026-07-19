from dotenv import load_dotenv
from google.genai import Client
load_dotenv(dotenv_path=".env")
client = Client()
print(client.models)
print(dir(client.models)[:50])
