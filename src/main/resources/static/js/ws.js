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
        if(message.code === 'OK') {
            const linkIconUrl = $('[data-link-icon-url]').attr('data-link-icon-url')
            const linkIconActiveUrl = $('[data-link-icon-active-url]').attr('data-link-icon-active-url')

            createObject(message.id, message.title, message.x, message.y)
            toastr.success(`<div class='toast__new-bin'><span>Bin was successfully created</span> <span class='toast__link'>
                <img src='${linkIconUrl}' height='16px' width='16px' alt='ad'>
                <img src='${linkIconActiveUrl}' class='link-icon-active' height='16px' width='16px' alt='ad' onclick="copyUrl('${message.id}')">
                </span></div>`, '', {onclick: function (e) {
                    if(e.target.className === 'link-icon-active') return
                    getIntoCenter()
                    let x = (-message.x * clusterSizeX * canvas.getZoom())
                    let y = (message.y * clusterSizeY * canvas.getZoom())
                    shiftCanvas(x, y)
                    openPopup()
                    getAndShowBin(message.id)
                }})
        } else if (message.code === 'SERVER_ERROR') {
            toastr.error('Error while deploying the bin')
        } else if (message.code === 'DUPLICATE') {
            toastr.error('Bin with selected coordinates already exists')
        }
    }

    getDeletedBin(message) {
        if(message.code === 'OK') {
            canvas.remove(field.read(message.x, message.y))
            field.delete(message.x, message.y)
            toastr.success('Bin was successfully deleted')
        } else if (message.code === 'SERVER_ERROR') {
            toastr.error('Error while deleting the bin')
        } else if (message.code === 'NO_SUCH_BIN') {
            toastr.error('Bin was not found')
        }
        closePopup('#pop-up')
    }
}

