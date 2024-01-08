let mousePressed = false
let clusterSizeX = window.innerWidth / 7
let clusterSizeY = window.innerWidth / 7

const amountOfCellsX = 100
const amountOfCellsY = 100
field = new Field(amountOfCellsX, amountOfCellsY)
let ws = new WS()
window.clientId = crypto.randomUUID()
const binToCreate = new URLSearchParams(window.location.search).get('binToCreate')

const camera = {
    left: 0,
    top: 0,
    width: window.innerWidth,
    height: window.innerHeight
}

const canvas = createCanvas('canvas')
initCanvas()
setFormEvents()
createBinsFromJSON(decodeBins())
ws.connect()
fillTimeOptions()
adjustUpperMenuWidth()

function createBinsFromJSON(json) {
    let jsonObject = JSON.parse(json)
    jsonObject.forEach(o => createObject(o.id, o.title, o.x, o.y, o.color))
}

function decodeBins() {
    let byteChars = atob(bins)
    let byteArray = new Uint8Array(byteChars.length)
    for (let i = 0; i < byteChars.length; i++) {
        byteArray[i] = byteChars.charCodeAt(i)
    }
    return new TextDecoder().decode(byteArray)
}

const extractData = (form) => {
    form = '#' + form
    if (!validateForm(form)) return

    const bin = composeObject(form)
    if (ws.connected) {
        ws.createBin(bin)
        closePopup(form)
        clearForm(form)
    } else {
        toastr.error('Server is not responding')
    }
}

const composeObject = form => {
    let title = extractFormFieldValue(form, '.title')
    let message = extractFormFieldValue(form, '.message')
    let amountOfTime = extractFormFieldValue(form, '.amount_of_time')
    let color = extractFormFieldValue(form, '.form-color input')
    let x, y
    if (form === '#drop-down' && $('#coords__input:checked').val() === 'on') {
        x = extractFormFieldValue(form, '#coords__x')
        y = extractFormFieldValue(form, '#coords__y')
    } else if (form === '#pop-up') {
        x = window.lastClickX
        y = -window.lastClickY
    }

    return {
        title: title,
        message: message,
        amountOfTime: amountOfTime,
        color: color,
        x: x, y: y
    }
}

const extractFormFieldValue = (form, field) => {
    return $(form + ' ' + field).val().trim()
}

const deleteBin = id => {
    if (ws.connected) {
        ws.deleteBin(id)
        closePopup('#pop-up')
    } else {
        toastr.error('Server is not responding')
    }
}

const getAndShowBin = (id) => {
    $.ajax({
        url: '/api/bin',
        method: 'get',
        dataType: 'json',
        data: {id: id},
        success: function (data) {
            showBin(data)
        },
        error: function (data) {
            if (data.code === 'NOT_FOUND') {
                toastr.error('Bin was not found')
            } else if (data.code === 'INTERNAL_SERVER_ERROR') {
                toastr.error('Error while getting the bin')
            } else {
                toastr.error('Server is not responding')
            }
        }
    })
}

if (urlBin !== null) {
    if (urlBin.statusCode === 'OK') {
        shiftCanvas(-urlBin.body.x * clusterSizeX, urlBin.body.y * clusterSizeY)
        canvas.relativePan(new fabric.Point(0, 0))
        showBin(urlBin.body)
    } else if (urlBin.statusCode === 'NOT_FOUND') {
        toastr.error("Bin was not found")
    }
}

const checkBinToCreate = () => {
    if (binToCreate !== null && binToCreate !== "") {
        let bin = atob(binToCreate.substring(1, binToCreate.length - 1));
        let binObject = JSON.parse(bin)
        ws.createBin(binObject)
    }
}

const copyUrl = async (id) => {
    let url = new URL(location.href)
    url.searchParams.set('id', id)
    await navigator.clipboard.writeText(url.toString())

    toastr.success("Copied to clipboard")
}

function redirectTo(path, ...params) {
    if (params.length % 2 !== 0) return
    let url = new URL(location.protocol + location.host + path)

    for (let i = 0; i < params.length;) {
        const parName = params[i++]
        const parValue = params[i++]

        if (parName && parValue) {
            url.searchParams.set(parName, parValue)
        }
    }

    document.location.href = url.toString()
}