#!/bin/bash
exec 3<>/dev/tcp/127.0.0.1/8080 || exit 1
printf 'GET /health/ready HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n' >&3
response=""
while IFS= read -r -t 3 line; do
  response+="$line"
done <&3
printf '%s' "$response" | grep -q '"status":"UP"'
