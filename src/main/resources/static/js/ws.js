class WS {
    stompClient = null
    connected = false

    connect() {
        let socket = new SockJS('/api/stomp-endpoint')
        const self = this
        self.stompClient = Stomp.over(socket)
        self.stompClient.connect({}, frame => onConnect(frame))
        socket.onclose = () => onClose()

        const onConnect = frame => {
            self.connected = true
            console.log('Connected: ' + frame)
            subscribeToMessageBrokers()
            sendBinToCreate()
        }

        const subscribeToMessageBrokers = () => {
            this.stompClient.subscribe('/topic/createdBinNotifications', function (message) {
                self.getNewBin(JSON.parse(message.body))
            })
            this.stompClient.subscribe('/topic/deletedBinNotifications', function (message) {
                self.getDeletedBin(JSON.parse(message.body))
            })
        }

        const onClose = () => {
            console.log('connection closed')
            self.connected = false
            setTimeout(() => {
                console.log('Trying to reconnect...')
                ws.connect()
            }, 10000)
        }
    }

    getNewBin(message) {
        const body = message.body
        createObject(body.id, body.title, body.x, body.y, body.color)
    }

    getDeletedBin(message) {
        const body = message.body
        canvas.remove(field.read(body.x, body.y))
        field.delete(body.x, body.y)
    }
}

