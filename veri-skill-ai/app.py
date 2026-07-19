from flask import Flask, request, jsonify
from ai_scorer import evaluate_resume
from utils import extract_text_from_pdf

app = Flask(__name__)

@app.route('/score', methods=['POST'])
def score_resume():
    # Supports both direct multipart uploads and backend JSON requests.
    payload = request.get_json(silent=True) or {}
    job_description = payload.get('job_description') or request.form.get('job_description')
    resume_text = payload.get('resume_text')

    if not job_description:
        return jsonify({"error": "No job description provided"}), 400

    if not resume_text:
        if 'resume' not in request.files:
            return jsonify({"error": "No resume file or resume text provided"}), 400

        file = request.files['resume']
        resume_text = extract_text_from_pdf(file)

    if not resume_text:
        return jsonify({"error": "Resume text is empty"}), 400

    score = evaluate_resume(job_description, resume_text)

    return jsonify({"score": score})

if __name__ == '__main__':
    # Run on 0.0.0.0 to allow access from Windows/WSL
    app.run(host='0.0.0.0', port=5001, debug=True)
