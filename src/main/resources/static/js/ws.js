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
            self.stompClient.subscribe('/topic/createdBinNotifications', function (message) {
                self.getNewBin(JSON.parse(message.body))
            })
            self.stompClient.subscribe('/topic/deletedBinNotifications', function (message) {
                self.getDeletedBin(JSON.parse(message.body))
            })
        })
    }

    createBin(bin) {
        this.stompClient.send('/app/createBin', {}, JSON.stringify(bin))
    }
    
    deleteBin(id) {
        this.stompClient.send('/app/deleteBin', {}, id)
    }

    getNewBin(message) {
        if(message.code === 0) {
            const linkIconUrl = $('[data-link-icon-url]').attr('data-link-icon-url')
            const linkIconActiveUrl = $('[data-link-icon-active-url]').attr('data-link-icon-active-url')

            createObject(message.id, message.title, message.x, message.y)
            toastr.success(`<div class='toast__new-bin'><span>Bin was successfully created</span> <span class='toast__link'>
                <img src='${linkIconUrl}' height='16px' width='16px' alt='ad'>
                <img src='${linkIconActiveUrl}' height='16px' width='16px' alt='ad' onclick="copyUrl('${message.id}')">
                </span></div>`)
        } else if (message.code === 1) {
            toastr.error('Error while deploying the bin')
        }
    }

    getDeletedBin(message) {
        if(message.code === 0) {
            canvas.remove(field.read(message.x, message.y))
            field.delete(message.x, message.y)
            toastr.success('Bin was successfully deleted')
        } else {
            toastr.error('Error while deleting the bin')
        }
        closePopup()
    }
}

