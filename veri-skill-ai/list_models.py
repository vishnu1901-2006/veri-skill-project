from dotenv import load_dotenv
import os
from google.genai import Client
load_dotenv(dotenv_path='.env')
print('GOOGLE_API_KEY', bool(os.getenv('GOOGLE_API_KEY')))
client = Client()
models = client.list_models()
print('models count', len(models))
for m in models[:30]:
    print(m.name)
