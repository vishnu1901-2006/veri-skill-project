import PyPDF2
from io import BytesIO

def extract_text_from_pdf(file_storage):
    """Extracts text from a PDF file stream."""
    # We read the file stream into memory
    file_stream = file_storage.read()
    
    # We wrap it in BytesIO so PyPDF2 can treat the memory buffer like a file
    reader = PyPDF2.PdfReader(BytesIO(file_stream))
    
    text = ""
    for page in reader.pages:
        text += page.extract_text() or ""
    return text