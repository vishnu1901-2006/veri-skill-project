#!/bin/bash
cd /mnt/d/veri-skill-project/veri-skill-zk-bridge
node --version
npm --version
(ss -tulpn 2>/dev/null || netstat -tulpn 2>/dev/null) | grep 3000 || true
printf '%s' '{"private_score":85,"required_threshold":75}' | curl -s -X POST http://127.0.0.1:3000/prove -H 'Content-Type: application/json' --data-binary @- -w '\n--HTTP_STATUS:%{http_code}\n'
