PATHO_WATCHER_CONFIG_PATH='classpath:/,/home/andi/patho/watcher.yml'

PATHO_WATCHER_ENABLED=true
PATHO_WATCHER_DIR_LOCAL='/home/andi/patho/watch'
PATHO_WATCHER_DIR_REMOTE='//ukl-fsgroup2.ad.uniklinik-freiburg.de/Group/AUG-T-Histologie-Untersuchungsschein-vor-Befundung'
PATHO_WATCHER_DIR_USER='aug-mounts'
PATHO_WATCHER_DIR_PASSWORD='mko09ijn'

FILE_WATCHER_EXE='watcher/filewatcher-1.0.jar'
PATHO_EXE='patho/PathoFroh-2.0.jar'

watcher=$(ps -ef | grep "$PATHO_EXE" | tr -s ' ' | cut -d ' ' -f2 2>&1 | head -n 1)
count=$(ps -ef | grep "$PATHO_EXE" | tr -s ' ' | cut -d ' ' -f2 | wc -l)

if [ "$count" -ge 2 ]; then
        echo "PathoFrO already started PID $watcher"
else
        echo "Starting PathoFrO"
        java -jar $PATHO_EXE -Xms512m -Xmx2G -Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider -Dfile.encoding=UTF8 --spring.config.location=$PATHO_CONFIG_PATH > /dev/null 2>&1 &
fi


if [ "$PATHO_WATCHER_ENABLED" = true ]; then
        if grep -qs $PATHO_WATCHER_DIR_LOCAL /proc/mounts; then
                echo "Do nothing watch dir is mounted"
        else
                echo "Mount $PATHO_WATCHER_DIR_REMOTE"
                groupID=$(id -g)
                userID=$(id -u)
                sudo mount -t cifs -o username="$PATHO_WATCHER_DIR_USER",password="$PATHO_WATCHER_DIR_PASSWORD",gid="$groupID",uid="$userID" $PATHO_WATCHER_DIR_REMOTE $PATHO_WATCHER_DIR_LOCAL
        fi

watcher=$(ps -ef | grep "$FILE_WATCHER_EXE" | tr -s ' ' | cut -d ' ' -f2 2>&1 | head -n 1)
count=$(ps -ef | grep "$FILE_WATCHER_EXE" | tr -s ' ' | cut -d ' ' -f2 | wc -l)
        if [ "$count" -ge 2 ]; then
                echo "Filewatcher already started PID $watcher"
        else
                echo "Starting Filewatcher to watch $PATHO_WATCHER_DIR_LOCAL"
                java -jar $FILE_WATCHER_EXE --spring.config.location=$PATHO_WATCHER_CONFIG_PATH > /dev/null 2>&1 &
        fi

fi
