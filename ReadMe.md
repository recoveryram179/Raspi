# Raspi

# for generating jar file without tests
./gradlew clean build -x test

# for running without creating jarfile
./gradlew bootRun

nohup ./gradlew bootRun > log.txt 2>&1 &


---------------------------------------------
    curl -X POST \
    -F "file=@/sdcard/t1.png" \
    http://192.168.1.8:8090/files/upload
    
    curl -X POST \
    -F "file=@/sdcard/t1.png" \
    https://raspi-2slp.onrender.com/files/upload


    http://localhost:8090/uploads/t1.png

-----------------------------------------------------------



