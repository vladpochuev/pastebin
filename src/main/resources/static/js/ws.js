class WS {
    stompClient = null
    connected = false

    connect() {
        let socket = new SockJS('/stomp-endpoint')
        const self = this
        self.stompClient = Stomp.over(socket)
        self.stompClient.connect({}, function (frame) {
            self.connected = true
            console.log('Connected: ' + frame)
            self.stompClient.subscribe('/topic/binNotifications', function (greeting) {
                self.getMessage(JSON.parse(greeting.body))
            })
        })
    }

    sendMessage(bin) {
        this.stompClient.send("/app/newBin", {}, JSON.stringify(bin))
    }

    getMessage(message) {
        if(message.code === 0) {
            const linkIconUrl = document.querySelector("[data-link-icon-url]").getAttribute("data-link-icon-url")
            const linkIconActiveUrl = document.querySelector("[data-link-icon-active-url]").getAttribute("data-link-icon-active-url")

            createObject(message.id, message.title, message.x, message.y)
            toastr.success(`<div class="toast__new-bin"><span>Bin was successfully created</span> <span class="toast__link">
                <img src="${linkIconUrl}" height="16px" width="16px" alt="ad">
                <img src="${linkIconActiveUrl}" height="16px" width="16px" alt="ad" onclick="copyUrl('${message.id}')">
                </span></div>`)
        } else if (message.code === 1) {
            toastr.error("Error while deploying the bin")
        }
    }
}

