#!/bin/bash - 

port_beg=${1:2012}
port_end=${2:2025}

output_name_root=${3:initial}

pkill -9 -f 'ncat.*tego-workhorse'
pkill -9 -f 'tee.*mon.*txt'
pkill -9 -f 'mastermind.*--cmd=visualize'
