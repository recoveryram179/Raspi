# Raspi

# for generating jar file without tests
./gradlew clean build -x test


    curl -X POST \
    -F "file=@/home/user/video.mp4" \
    http://192.168.1.8:8090/files/upload