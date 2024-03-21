let mousePressed = false
let clusterSizeX = window.innerWidth / 7
let clusterSizeY = window.innerWidth / 7

const amountOfCellsX = 100
const amountOfCellsY = 100
field = new Field(amountOfCellsX, amountOfCellsY)
let ws = new WS()

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
        createBin(bin)
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

const deleteSelectedBin = id => {
    if (ws.connected) {
        deleteBin(id)
        closePopup('#pop-up')
    } else {
        toastr.error('Server is not responding')
    }
}

const getAndShowBin = (id) => {
    getBin(id).then(data => showBin(data))
}

if (urlBin !== null) {
    if (urlBin.statusCode === 'OK') {
        shiftCanvas(-urlBin.body.x * clusterSizeX, urlBin.body.y * clusterSizeY)
        canvas.relativePan(new fabric.Point(0, 0))
        showBin(urlBin.body)
    } else if (urlBin.statusCode === 'NOT_FOUND') {
        toastr.error('Bin was not found')
    }
}

const copyUrl = async (id) => {
    let url = new URL(location.href)
    url.searchParams.set('id', id)
    await navigator.clipboard.writeText(url.toString())

    toastr.success('Copied to clipboard')
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

const sendBinToCreate = () => {
    const binToCreate = getCookie('Bin-to-create')
    if (binToCreate) {
        createBin(JSON.parse(atob(binToCreate)), false)
    }
}