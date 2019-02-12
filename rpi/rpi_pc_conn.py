from tcpcom import TCPServer, TCPClient
import time

# for establishing a bidirectional TCP/IP Connection with the PC (algorithm)

IP_ADDRESS = "192.168.0.17"
IP_PORT = 54321


def onStateChanged(state, msg):
    if state == "LISTENING":
        print
        "RPi - PC || Listening..."
    elif state == "CONNECTED":
        print
        "RPi - PC || Connected to", msg
    elif state == "MESSAGE":
        print
        "RPi - PC || Message received:", msg


client = TCPClient(IP_ADDRESS, IP_PORT, stateChanged=onStateChanged)
pc = client.connect()
if pc:
    isConnected = True
    while isConnected:
        message = "test message to pc"
        print
        "RPi - PC || Sending command: %s", message
        # TODO - prepare to forward the messages from Arduino and PC to the respective components
        client.sendMessage(message)
        time.sleep(1)
else:
    print
    "RPi - PC || Connection failed"

server = TCPServer(IP_PORT, stateChanged=onStateChanged)
