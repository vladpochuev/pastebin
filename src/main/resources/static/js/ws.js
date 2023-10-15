class WS {
    stompClient = null
    connected = false

    connect() {
        let socket = new SockJS('/stomp-endpoint')
        const self = this
        self.stompClient = Stomp.over(socket)
        self.stompClient.connect({}, function (frame) {
            self.connected = true;
            console.log('Connected: ' + frame)
            self.stompClient.subscribe('/topic/binNotifications', function (greeting) {
                self.getMessage(JSON.parse(greeting.body))
            })
        })
    }

    sendMessage(x, y) {
        this.stompClient.send("/app/newBin", {}, JSON.stringify({x: x, y: y}))
    }

    getMessage(message) {
        createObject(message.x, message.y)
    }
}