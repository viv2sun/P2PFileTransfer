#!/bin/bash

# usage: ./test.sh {number of peers} "{list of peers that have file}"
#
# default: ./test.sh 2 "1"
# launch 2 peers, and peer 1 has the file
#
# example: ./test.sh 10 "4 7 9"
# launch 10 peers, and peers 4, 7, and 9 have the file

count=${1-2}
has=${2-1}

echo $count
echo $has

for a in $(seq 1 $count); do
	echo $has | grep -qv "\<$a\>"
	echo $(( 1000 + a )) 127.0.0.1 $(( 5000 + a )) $?
done > PeerInfo.cfg

# clean up from the last test
# rm -rf peer_*/ log_peer_*.log

# launch the peers
for a in `cat PeerInfo.cfg | awk {'print $1'}`; do
	# mkdir -p peer_$a
	# echo $has | grep -q "\<$a\>" && cp "$(sed -n '/^FileName /{s/^FileName //;a}')" peer_$a/
	java -cp bin/ PeerProcess $a &
done

# wait for everyone to finish
for a in `cat PeerInfo.cfg | awk {'print $1'}`; do
	wait -n
done
