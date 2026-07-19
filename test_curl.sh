#!/bin/bash
cd /mnt/d/veri-skill-project
echo "Test 1: Using -d with -H Content-Type"
curl -v -X POST http://127.0.0.1:3000/prove \
  -H "Content-Type: application/json" \
  -d '{"candidate_id":"cand-1","private_score":85,"required_threshold":75}' 2>&1 | head -30

echo ""
echo "Test 2: Using --json flag"
curl -v -X POST http://127.0.0.1:3000/prove \
  --json '{"candidate_id":"cand-1","private_score":85,"required_threshold":75}' 2>&1 | head -30
