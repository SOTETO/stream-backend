########################
# Setup Mariadb Docker #
########################





# default database config 
ip_address="172.2.200.7"
docker_name="stream-database"
db_name="stream"
db_root_password="stream"
db_user="stream"
db_password="stream"



setup_database_docker(){
  echo "setup Stream-Database";
  docker run --net pool-network --ip $ip_address --name $docker_name --restart=unless-stopped \
    -e MYSQL_ROOT_PASSWORD=$db_root_password \
	    	-e MYSQL_DATABASE=$db_name \
    		-e MYSQL_USER=$db_user\
    		-e MYSQL_PASSWORD=$db_password \
    		-e MYSQL_ROOT_PASSWORD=yes \
    		-d mariadb:latest;
}

remove_database_docker(){
  remove=$(docker rm -f $docker_name)
   case $remove in
    $docker_name)
      echo "${docker_name} successfull removed";
    ;;
    *) echo "ERROR: ${docker_name} is maybe not running."
   esac
}

restart_database_docker(){
  echo "restart ${docker_name}" 
  remove=$(docker rm -f $docker_name)
  case $remove in
    $docker_name)
      echo "${docker_name} successfull removed";
      setup_database_docker && echo "${docker_name} successful restarted" || echo "ERROR: ${docker_name} can't start again.";
    ;;
    *) echo "ERROR: ${docker_name} is maybe not running. Try pool run ${docker_name}"
  esac
}

case $1 in
  run)
    shift
    while getopts ":upndi" option; do
      case ${option} in
        u) db_user=${OPTAG};;
        p) db_password=${OPTAG};;
        n) db_name=${OPTAG};;
        d) docker_name=${OPTAG};;
        i) ip_address=${OPTAG};;
      esac
    done
    setup_database_docker
    ;;
  rm) remove_database_docker;;
  restart) restart_database_docker;;
esac
