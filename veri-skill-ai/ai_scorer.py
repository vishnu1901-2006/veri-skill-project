import os
import json

from dotenv import load_dotenv

load_dotenv()

from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.prompts import PromptTemplate
from langchain_core.output_parsers import StrOutputParser

MODEL_NAME = os.getenv("GOOGLE_MODEL") or os.getenv("GEMINI_MODEL") or "gemini-3.5-flash"

# Initialize the Gemini model
# It will automatically pick up GOOGLE_API_KEY from your environment
llm = ChatGoogleGenerativeAI(model=MODEL_NAME, temperature=0)

prompt = PromptTemplate(
    input_variables=["job_description", "resume_text"],
    template="""
    You are an expert technical recruiter. Evaluate the candidate's resume against the provided job description.
    
    Job Description:
    {job_description}
    
    Candidate Resume:
    {resume_text}
    
    Calculate a match score from 0 to 100 based on skills, experience, and relevance.
    You MUST output ONLY a valid JSON object with a single key "score" containing the integer. 
    Do not include markdown formatting, backticks, or any other text.
    
    Example output:
    {{ "score": 85 }}
    """
)

chain = prompt | llm | StrOutputParser()

def evaluate_resume(job_description: str, resume_text: str) -> int:
    """Runs the LangChain pipeline using Gemini and extracts the integer score."""
    raw_output = ""
    try:
        raw_output = chain.invoke({
            "job_description": job_description,
            "resume_text": resume_text,
        })

        # Clean the output just in case Gemini includes markdown code blocks (```json ... ```) 
        clean_output = raw_output.strip().removeprefix("```json").removeprefix("```").removesuffix("```").strip()

        # Parse the JSON string to extract the integer
        result = json.loads(clean_output)
        score = int(result.get("score", 0))

        # Ensure it fits within a human-friendly percentage range
        return min(max(score, 0), 100)

    except Exception as e:
        print(f"Failed to parse LLM output: {e}")
        print(f"Raw Output was: {raw_output}")
        return 0  # Default to 0 on failure