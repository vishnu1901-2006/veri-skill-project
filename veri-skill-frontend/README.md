# Veri-Skill Frontend

React/TypeScript interface for the Veri-Skill resume verification workflow.

## Local Setup

```bash
cd veri-skill-frontend
npm install
npm run dev
```

The app runs at `http://localhost:5173` and proxies `/api/v1` to the Spring Boot backend at `http://localhost:8080`.

## Required Services

Start these before using the full workflow:

```bash
# MySQL container
docker start veriskill-mysql

# Flask AI service
cd ../veri-skill-ai
source venv/bin/activate
python3 app.py

# ZK Bridge
cd ../veri-skill-zk-bridge
npm run dev

# Spring Boot backend
cd ../veri-skill-backend
DB_URL='jdbc:mysql://localhost:3307/veriskill?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true' \
DB_USERNAME=root \
DB_PASSWORD=root \
mvn spring-boot:run
```

## Screens

- `/` role selection and system status
- `/candidate` resume upload flow
- `/recruiter` job creation and candidate verification dashboard

## Privacy Rule

Recruiter views show verification status only. Candidate upload responses may contain an internal AI score from the backend, but recruiter dashboard components intentionally do not render scores.
