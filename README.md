# This is the parent project of all learning/studying/re-learning

## Aeron notes
Aeron = API + Media Driver + **Aeron Archive** + **Aeron Cluster**

Media Driver handles everything related to networking (ipc/udp uni/multicast), logbuffers for sender and receiver.

Aeron Archive is for replay, can configure to merge with live stream after catching-up

Aeron Cluster is for multi-node backup

### Basics
- Create a Media Driver, either embedded or standalone. I prefer standalone instance as it fits the design idea of having one instance per server.
- Both publisher and consumer can connect to the same instance as long as they specify the same Aeron directory when creating the Aeron Context.
- When starting up, wait a couple of seconds for the publisher/consumer to connect to the Media Server, otherwise might get a "Sent Failed" return code if publish immediately.

