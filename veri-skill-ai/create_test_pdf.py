from PyPDF2 import PdfWriter
writer = PdfWriter()
writer.add_blank_page(width=72, height=72)
with open("/mnt/d/veri-skill-project/veri-skill-ai/test_resume.pdf", "wb") as fp:
    writer.write(fp)
print("created test_resume.pdf")
