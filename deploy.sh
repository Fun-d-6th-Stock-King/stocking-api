kill $(ps -ef | pgrep -f "stocking-api")
nohup java -Dspring.profiles.active=production -jar stocking-api-1.0.jar 1> /dev/null 2>&1 &
tail -f /home/ec2-user/api/logs/*
