# for generating jar file without tests
./gradlew clean build -x test

# copy files to raspi
scp build/libs/Raspi-0.0.1-SNAPSHOT.jar ram@raspberrypi:/home/ram/Raspi5

scp build/libs/Raspi-0.0.1-SNAPSHOT.jar ram@192.168.1.16:/home/ram/Raspi5

# to enter in a file
Ctrl + O
Enter
Ctrl + X

---------------------------------------------
sudo nano /etc/systemd/system/raspi.service
---------------------------------------------------------
    [Unit]
    Description=Spring Boot App
    After=network.target

    [Service]
    User=ram
    WorkingDirectory=/home/ram/Raspi5
    ExecStart=/usr/bin/java -jar /home/ram/Raspi5/Raspi-0.0.1-SNAPSHOT.jar
    SuccessExitStatus=143
    Restart=always
    RestartSec=5
    
    
    [Install]
    WantedBy=multi-user.target
------------------------------------------
# to add service
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl enable raspi


# start service
sudo systemctl start raspi

# restart service
sudo systemctl restart raspi

# stop service
sudo systemctl stop raspi

# Check status
sudo systemctl status raspi

# View live logs
sudo journalctl -u raspi -f
or
journalctl -u raspi -f -b



////////////////////////////////////////////////////////////////

sudo nano /etc/systemd/system/raspi-cloudflared.service
-------------------------------------------
    [Unit]
    Description=Spring Boot App
    After=network.target
    
    
    [Service]
    User=ram
    WorkingDirectory=/home/ram
    ExecStart=/usr/bin/cloudflared tunnel --url http://localhost:8086
    SuccessExitStatus=143
    Restart=always
    RestartSec=10
    
    
    [Install]
    WantedBy=multi-user.target

------------------------------------------

# to add service
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl enable proto-cloudflared


# to run service
sudo systemctl start proto-cloudflared

journalctl -u proto-cloudflared --no-pager | grep trycloudflare
#or
journalctl -u proto-cloudflared -n 200 --no-pager


# Check status
sudo systemctl status proto-cloudflared

\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


nano /home/ram/get_proto_url.sh
---------------------
    #!/bin/bash

    while true; do
    URL=$(journalctl -u proto-cloudflared -b --no-pager \
    | grep trycloudflare.com \
    | grep -v api.trycloudflare.com \
    | grep -o 'https://[a-zA-Z0-9.-]*trycloudflare.com' \
    | tail -n1)
    
    
    if [ -n "$URL" ]; then
    echo "Found URL: $URL"
    
    
        sleep 15
    
    
        curl -v "$URL/raspi"
        break
    fi

    sleep 2
    done

----------------------
chmod +x /home/ram/get_proto_url.sh

///////////////////////////////////////////////////////////////

sudo nano /etc/systemd/system/proto-cloudflare-auto.service
--------------------------------
    [Unit]
    Description=Auto Curl Cloudflare URL
    After=proto_cloudflared.service
    
    
    [Service]
    ExecStart=/bin/bash -c "sleep 15 && /home/ram/get_proto_url.sh"
    Restart=on-failure
    
    
    [Install]
    WantedBy=multi-user.target

-------------------------------------------

# to add service
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl enable proto-cloudflare-auto


# to run service
sudo systemctl start proto-cloudflare-auto

sudo systemctl restart proto-cloudflare-auto

# status
sudo systemctl status proto-cloudflare-auto

////////////////////////////////////




