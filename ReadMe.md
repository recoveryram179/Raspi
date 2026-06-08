# for generating jar file
./gradlew clean build
# for generating jar file without tests
./gradlew clean build -x test

# copy files to raspi
scp build/libs/Raspi-0.0.1-SNAPSHOT.jar ram@raspberrypi:/home/ram/Raspi5

#from raspi docs
cloudflared tunnel --url http://localhost:8090

#fro AWS docs
java -jar Raspi-0.0.1-SNAPSHOT.jar


curl -X POST http://localhost:8080/bash \
-H "Content-Type: application/json" \
-d '{"cmd":"ls","path":""}'

curl -X POST http://192.168.1.16:8080/bash \
-H "Content-Type: application/json" \
-d '{"cmd":"ssh -p 443 -R0:localhost:5900 tcp@a.pinggy.io  > /tmp/pinggy.log 2>&1","path":"" }'

curl -X POST http://192.168.1.16:8080/bash \
-H "Content-Type: application/json" \
-d '{"cmd":"cat /tmp/pinggy.log","path":"" }'

curl -X POST http://192.168.1.16:8080/bash \
-H "Content-Type: application/json" \
-d '{ "cmd": "echo password | sudo -S cat /etc/passwd" }'

curl -X POST http://192.168.1.16:8080/bash \
-H "Content-Type: application/json" \
-d '{ "cmd": "cd linuxtest;ls " }'




