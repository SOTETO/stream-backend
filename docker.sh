#!/bin/bash
setup_docker(){  

}

remove_docker(){
  docker rm -f stream-backend-docker;
}

logs_docker(){
  docker logs stream-backend-docker;
}

update_docker(){
  docker pull vivaconagua/stream-backend:latest
  docker rm -f stream-backend-docker
  setup_docker;
}

emoto_release(){
  ./make-release.sh
}

## ToDo: Add DB Scripts
emoto_db_setup_docker (){
   	docker run --net pool-network --ip $emoto_db_ip --name emoto-mariadb --restart=unless-stopped \
		-e MYSQL_ROOT_PASSWORD=emoto \
	    	-e MYSQL_DATABASE=emoto \
    		-e MYSQL_USER=emoto \
    		-e MYSQL_PASSWORD=emoto \
    		-e MYSQL_ROOT_PASSWORD=yes \
    		-d mariadb:latest;


}

emoto_db_remove_docker (){
  echo "not implemented"
}

emoto_db_logs_docker (){
  echo "not implemented"
}

emoto_db_update_docker (){
  echo "not implemented"
}
