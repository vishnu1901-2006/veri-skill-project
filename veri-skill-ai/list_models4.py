from dotenv import load_dotenv
from google.genai import Client
load_dotenv(dotenv_path=".env")
client = Client()
models = client.models.list()
print("models count", len(models))
for m in models:
    print(m.name)
