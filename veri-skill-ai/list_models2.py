from dotenv import load_dotenv
import os
from google.genai import Client
load_dotenv(dotenv_path=".env")
client = Client()
print([a for a in dir(client) if 'model' in a.lower() or 'list' in a.lower()][:50])
