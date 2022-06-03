## CNT5106c Project Spring 2021


Peer to Peer File distribution with choking and unchoking mechanism among peers using TCP 

Group Number: 30

Group members:
 
Abhiti Sachdeva,
Daanish Goyal,
Yagya Malik

How to execute the project:
open the terminal (in lin114-00 to lin114-07 lab servers)
change the directory to the project folder 
run the 'make' command 
```bash
make
```
run the below command to start the remote peers
```bash
java RemotePeerInitiator
```
Note: If the above command does not work, i.e., unable to start peers, we need to start the peers manually each in separate terminal as shown below
```bash
java peerProcess PeerID
```
Please find the log files in the 'logs' folder. 

PeerID ranges from 1001 to 1009 in context of PeerInfo.cfg used by us to test the project on our local machines
