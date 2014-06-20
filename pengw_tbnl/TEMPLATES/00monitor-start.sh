#!/bin/bash - 

port_beg=${1:-2012}
port_end=${2:-2025}
output_name_root=${3:-initial}

for port in $(seq ${port_beg} ${port_end}); do
    rm -f ${port}; mkfifo ${port}
    ( ncat --listen --keep-open tego-workhorse ${port} | tee mon-${port}.txt > ${port} ) &
done

( ./mastermind mastermind.jar --cmd=visualize --output-name-root=${output_name_root} --verbose=true $(perl -wl -e "printf join ' ', map { \"--monitor-trace=\$_\" } ${port_beg}..${port_end}") | tee mon.txt ) &

while true; do
    ./00push-viz.sh ${output_name_root};
    sleep 5;
done
