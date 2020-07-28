# PathoFro

## Install

* Ubuntu 20.04
    * Common
        ```
        sudo apt install net-tools
      
        # set timezone
        sudo timedatectl set-timezone Europe/Berlin
        ```
    * Postgresql
        ```
        sudo apt install postgresql postgresql-plpython3-12
        sudo su - postgres
        psql -c "alter user postgres with password 'password'"
      
        # set postgres network access
        sudo vim /etc/postgresql/12/main/postgresql.conf
        listen_addresses = '*' 
          
        sudo vim /etc/postgresql/12/main/pg_hba.conf
        host    all             all              0.0.0.0/0                       md5
        host    all             all              ::/0                            md5
      
        sudo service postgresql restart
        ```
    * Firewall
        ```
        sudo apt-get install ufw
        sudo ufw default deny incoming
        sudo ufw default allow outgoing
        sudo ufw allow ssh    
        # Postgres
        sudo ufw allow 5432/tcp
        sudo ufw enable
        sudo ufw status
        ```
    * Nginx
        ```
        sudo apt install nginx
      
        #Fierwall 
        sudo ufw allow 'Nginx HTTPS'
        sudo cp /etc/nginx/sites-available/default /etc/nginx/sites-available/default.old
        ```
        ```
        # Insert Config:    
        sudo tee /etc/nginx/sites-available/default > /dev/null <<EOT
        server {
            listen 80;
              server_name augpatho.ukl.uni-freiburg.de;
              return 301 https://augpatho.ukl.uni-freiburg.de\$request_uri;
        }
        server {
            listen 443 ssl http2;
            server_name augpatho.ukl.uni-freiburg.de;
              
            ssl on;
            ssl_certificate /etc/ssl/linux_cert+ca.pem;
            ssl_certificate_key /etc/ssl/private_key.key;
            access_log /var/log/nginx/nginx.vhost.access.log;
            error_log /var/log/nginx/nginx.vhost.error.log;
      
            root /var/www/html;

            # Add index.php to the list if you are using PHP
            index index.html index.htm index.nginx-debian.html;


            location / {
                # First attempt to serve request as file, then
                # as directory, then fall back to displaying a 404.
                try_files \$uri \$uri/ =404;
            }
              
            location / {
                  proxy_set_header X-Forwarded-Host $host;
                  proxy_set_header X-Forwarded-Server $host;
                  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                  proxy_pass http://127.0.0.1:8080/;
            }
        }
        EOT
        ```
        ```
        sudo systemctl reload nginx 
        ```

    * PathoFrO prerequisite
        ```
        sudo apt install openjdk-11-jre-headless
        sudo apt install texlive-latex-extra
        # new user
        sudo useradd -m patho
        sudo chown patho:patho PathoFrO.jar
        sudo chmod 500 PathoFrO.jar
          
        #service
        sudo ln -s /home/patho/PathoFrO.jar /etc/init.d/PathoFrO
        ```
    
  
