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
            checkBinToCreate()
            console.log('Connected: ' + frame)
            subscribeToMessageBrokers()
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

    createBin(bin) {
        this.stompClient.send('/app/createBin', {clientId: window.clientId}, JSON.stringify(bin))
    }

    deleteBin(id) {
        this.stompClient.send('/app/deleteBin', {clientId: window.clientId}, id)
    }

    getNewBin(message) {
        const body = message.body
        const clientId = message.headers.clientId[0]

        if (message.statusCode === 'OK') {
            createObject(body.id, body.title, body.x, body.y, body.color)
            if (clientId !== window.clientId) return
            showSuccessMessage(body)
        }

        if (clientId !== window.clientId) return

        if (message.statusCode === 'INTERNAL_SERVER_ERROR') {
            toastr.error('Error while deploying the bin')
        } else if (message.statusCode === 'CONFLICT') {
            toastr.error('Bin with selected coordinates already exists')
        } else if (message.statusCode === 'NOT_ACCEPTABLE') {
            toastr.error('Bin was created out of bounds')
        } else if (message.statusCode === 'UNAUTHORIZED') {
            const binJSON = JSON.stringify(message.headers.binToCreate[0])
            redirectTo('/login', 'binToCreate', binJSON)
        }

        function showSuccessMessage(body) {
            const linkIconUrl = $('[data-link-icon-url]').attr('data-link-icon-url')
            const linkIconActiveUrl = $('[data-link-icon-active-url]').attr('data-link-icon-active-url')

            toastr.success(`
                <div class='toast__new-bin'>
                    <span>Bin was successfully created</span> 
                    <span class='toast__link'>
                        <img src='${linkIconUrl}' class="link_icon" height='16px' width='16px' alt='copy link'>
                        <img src='${linkIconActiveUrl}' class='link_icon_active' height='16px' width='16px' alt='copy link' onclick="copyUrl('${body.id}')">
                    </span>
                </div>`, '', {
                onclick: e => onClickSuccessMessage(e)
            })
        }

        function onClickSuccessMessage(e) {
            if (e.target.className === 'link_icon_active') return
            getIntoCenter()
            canvas.setZoom(1)
            setZoomTitle(1)
            shiftCanvas(-body.x * clusterSizeX, body.y * clusterSizeY)
            openPopup('popup-show')
            getAndShowBin(body.id)
        }
    }

    getDeletedBin(message) {
        const body = message.body
        const headers = message.headers

        if (message.statusCode === 'OK') {
            canvas.remove(field.read(body.x, body.y))
            field.delete(body.x, body.y)
            if (headers.clientId && headers.clientId[0] === window.clientId) {
                toastr.success('Bin was successfully deleted')
            }
        }

        if (headers.clientId && headers.clientId[0] !== window.clientId) return

        if (message.statusCode === 'INTERNAL_SERVER_ERROR') {
            toastr.error('Error while deleting the bin')
        } else if (message.statusCode === 'NOT_FOUND') {
            toastr.error('Bin was not found')
        } else if (message.statusCode === 'FORBIDDEN') {
            toastr.error('Bin does not belong to you')
        } else if (message.statusCode === 'UNAUTHORIZED') {
            redirectTo('/register')
        }
    }
}

