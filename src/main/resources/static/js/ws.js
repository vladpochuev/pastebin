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
        if(message.statusCode === 'OK') {
            const linkIconUrl = $('[data-link-icon-url]').attr('data-link-icon-url')
            const linkIconActiveUrl = $('[data-link-icon-active-url]').attr('data-link-icon-active-url')

            const body = message.body

            createObject(body.id, body.title, body.x, body.y, body.color)
            toastr.success(`<div class='toast__new-bin'><span>Bin was successfully created</span> <span class='toast__link'>
                <img src='${linkIconUrl}' height='16px' width='16px' alt='ad'>
                <img src='${linkIconActiveUrl}' class='link-icon-active' height='16px' width='16px' alt='ad' onclick="copyUrl('${body.id}')">
                </span></div>`, '', {onclick: function (e) {
                    if(e.target.className === 'link-icon-active') return
                    getIntoCenter()
                    canvas.setZoom(1)
                    setZoomTitle(1)
                    shiftCanvas(-body.x * clusterSizeX, body.y * clusterSizeY)
                    openPopup('popup-show')
                    getAndShowBin(body.id)
                }})
        } else if (message.statusCode === 'INTERNAL_SERVER_ERROR') {
            toastr.error('Error while deploying the bin')
        } else if (message.statusCode === 'CONFLICT') {
            toastr.error('Bin with selected coordinates already exists')
        }
    }

    getDeletedBin(message) {
        const body = message.body

        if(message.statusCode === 'OK') {
            canvas.remove(field.read(body.x, body.y))
            field.delete(body.x, body.y)
            toastr.success('Bin was successfully deleted')
        } else if (message.statusCode === 'INTERNAL_SERVER_ERROR') {
            toastr.error('Error while deleting the bin')
        } else if (message.statusCode === 'NOT_FOUND') {
            toastr.error('Bin was not found')
        }
        closePopup('#pop-up')
    }
}

